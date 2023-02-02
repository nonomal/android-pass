package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.publicKeyRing
import me.proton.core.key.domain.publicKey
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.crypto.api.error.InvalidAddressSignature
import proton.android.pass.crypto.api.error.KeyNotFound
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.ReadKey
import proton.android.pass.data.api.errors.CannotDeleteCurrentVaultError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.VaultItemKeyList
import proton.android.pass.data.api.repositories.VaultKeyRepository
import proton.android.pass.data.impl.crypto.ReencryptShareEntityContents
import proton.android.pass.data.impl.crypto.ShareEntityToShare
import proton.android.pass.data.impl.crypto.ShareResponseToEntity
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.extensions.toVaultItemKeyList
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.responses.ShareResponse
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class ShareRepositoryImpl @Inject constructor(
    private val database: PassDatabase,
    private val userAddressRepository: UserAddressRepository,
    private val remoteShareDataSource: RemoteShareDataSource,
    private val localShareDataSource: LocalShareDataSource,
    private val keyRepository: PublicAddressRepository,
    private val vaultKeyRepository: VaultKeyRepository,
    private val cryptoContext: CryptoContext,
    private val shareEntityToShare: ShareEntityToShare,
    private val reencryptShareEntityContents: ReencryptShareEntityContents,
    private val readKey: ReadKey,
    private val shareResponseToEntity: ShareResponseToEntity,
    private val createVault: CreateVault
) : ShareRepository {

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override suspend fun createVault(
        userId: SessionUserId,
        vault: NewVault
    ): LoadingResult<Share> = withContext(Dispatchers.IO) {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

        val (request, keyList) = try {
            createVaultRequest(vault, userAddress)
        } catch (e: RuntimeException) {
            PassLogger.w(TAG, e, "Error in CreateVaultRequest")
            return@withContext LoadingResult.Error(e)
        }
        val createVaultResult = remoteShareDataSource.createVault(userAddress.userId, request)
        when (createVaultResult) {
            is LoadingResult.Error -> return@withContext LoadingResult.Error(createVaultResult.exception)
            LoadingResult.Loading -> return@withContext LoadingResult.Loading
            is LoadingResult.Success -> Unit
        }

        // We replace manually the vaultKey.rotationId so it has the right value for performing the validation
        val rotationId = createVaultResult.data.contentRotationId
            ?: throw IllegalStateException("ContentRotationID cannot be null")
        val vaultKey = keyList.vaultKeyList.first().copy(rotationId = rotationId)

        // Replace the temporary rotationId we placed on the vaultKey with the actual rotationId
        val responseAsEntity =
            shareResponseToEntity(userAddress, createVaultResult.data, listOf(vaultKey))
        val entityResult: LoadingResult<ShareEntity> = database.inTransaction {

            localShareDataSource.upsertShares(listOf(responseAsEntity))

            val reencryptedEntityResult: LoadingResult<ShareEntity> =
                reencryptShareEntityContents(userAddress, createVaultResult.data, responseAsEntity)
            when (reencryptedEntityResult) {
                is LoadingResult.Error -> return@inTransaction LoadingResult.Error(reencryptedEntityResult.exception)
                LoadingResult.Loading -> return@inTransaction LoadingResult.Loading
                is LoadingResult.Success -> Unit
            }
            localShareDataSource.upsertShares(listOf(reencryptedEntityResult.data))
            reencryptedEntityResult
        }
        when (entityResult) {
            is LoadingResult.Error -> return@withContext LoadingResult.Error(entityResult.exception)
            LoadingResult.Loading -> return@withContext LoadingResult.Loading
            is LoadingResult.Success -> Unit
        }

        val publicKeys = userAddress.keys.map { it.privateKey.publicKey(cryptoContext) }
        return@withContext shareEntityToShare(userAddress, publicKeys, entityResult.data)
    }

    override suspend fun deleteVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            database.inTransaction {
                val currentSelectedShare: ShareEntity =
                    localShareDataSource.getSelectedSharesForUser(userId = userId).first()
                        .first()
                if (currentSelectedShare.id == shareId.id)
                    return@inTransaction LoadingResult.Error(CannotDeleteCurrentVaultError())
                remoteShareDataSource.deleteVault(userId, shareId)
                    .map { localShareDataSource.deleteShare(shareId) }
                    .map { }
            }
        }

    override suspend fun selectVault(userId: UserId, shareId: ShareId): LoadingResult<Unit> =
        withContext(Dispatchers.IO) {
            localShareDataSource.updateSelectedShare(shareId)
            LoadingResult.Success(Unit)
        }

    override fun observeAllShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>> =
        localShareDataSource.getAllSharesForUser(userId).toShare(userId)

    override fun observeSelectedShares(userId: SessionUserId): Flow<LoadingResult<List<Share>>> =
        localShareDataSource.getSelectedSharesForUser(userId).toShare(userId)

    private fun Flow<List<ShareEntity>>.toShare(userId: SessionUserId): Flow<LoadingResult<List<Share>>> =
        this.map { LoadingResult.Success(it) }
            .mapLatest { sharesResult ->
                if (sharesResult.data.isEmpty()) return@mapLatest LoadingResult.Success(emptyList<Share>())
                shareEntitiesToShares(userId, sharesResult.data)
            }
            .flowOn(Dispatchers.IO)

    override suspend fun refreshShares(userId: UserId): LoadingResult<List<Share>> =
        withContext(Dispatchers.IO) {
            return@withContext when (val sharesResult = performShareRefresh(userId)) {
                is LoadingResult.Error -> LoadingResult.Error(sharesResult.exception)
                LoadingResult.Loading -> LoadingResult.Loading
                is LoadingResult.Success -> shareEntitiesToShares(userId, sharesResult.data)
            }
        }


    @Suppress("ReturnCount")
    override suspend fun getById(userId: UserId, shareId: ShareId): LoadingResult<Share?> =
        withContext(Dispatchers.IO) {
            val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())

            // Check local
            var share: ShareEntity? = localShareDataSource.getById(userId, shareId)
            if (share == null) {
                // Check remote
                val getShareResult = remoteShareDataSource.getShareById(userId, shareId)
                when (getShareResult) {
                    is LoadingResult.Error -> return@withContext LoadingResult.Error(getShareResult.exception)
                    LoadingResult.Loading -> return@withContext LoadingResult.Loading
                    is LoadingResult.Success -> Unit
                }
                val shareResponse = getShareResult.data
                    ?: return@withContext LoadingResult.Error(IllegalStateException("Share Response is null"))

                val storeShareResult: LoadingResult<List<ShareEntity>> =
                    storeShares(userAddress, listOf(shareResponse))
                when (storeShareResult) {
                    is LoadingResult.Error -> return@withContext LoadingResult.Error(storeShareResult.exception)
                    LoadingResult.Loading -> return@withContext LoadingResult.Loading
                    is LoadingResult.Success -> Unit
                }
                share = storeShareResult.data[0]
            }

            return@withContext try {
                shareEntityToShare(userAddress, share, Source.LocalIfAvailable)
            } catch (e: InvalidAddressSignature) {
                PassLogger.i(
                    TAG,
                    e,
                    "Received InvalidAddressSignature. Retrying re-fetching the keys"
                )
                shareEntityToShare(userAddress, share, Source.RemoteNoCache)
            }
        }

    private suspend fun shareEntityToShare(
        userAddress: UserAddress,
        share: ShareEntity,
        source: Source
    ): LoadingResult<Share?> {
        val addressKeys = keyRepository.getPublicAddress(
            userAddress.userId,
            share.inviterEmail,
            source = source
        )
        return shareEntityToShare(userAddress, addressKeys.keys.publicKeyRing().keys, share)
    }

    @Suppress("ReturnCount")
    private suspend fun performShareRefresh(userId: UserId): LoadingResult<List<ShareEntity>> {
        val userAddress = userAddressRepository.getAddresses(userId).primary()
        if (userAddress == null) {
            val e = IllegalStateException("Could not find PrimaryAddress")
            PassLogger.w(TAG, e, "Error in performShareRefresh")
            return LoadingResult.Error(e)
        }

        val sharesResult = remoteShareDataSource.getShares(userAddress.userId)
        when (sharesResult) {
            is LoadingResult.Error -> return LoadingResult.Error(sharesResult.exception)
            LoadingResult.Loading -> return LoadingResult.Loading
            is LoadingResult.Success -> Unit
        }
        return storeShares(userAddress, sharesResult.data)
    }

    @Suppress("ReturnCount")
    private suspend fun shareEntitiesToShares(
        userId: UserId,
        entities: List<ShareEntity>
    ): LoadingResult<List<Share>> {
        val userKeys = entities
            .map { it.inviterEmail }
            .distinct()
            .associateWith {
                keyRepository.getPublicAddress(
                    sessionUserId = userId,
                    email = it,
                    source = Source.LocalIfAvailable
                )
            }

        val shareList: List<Share> = entities.map { entity ->
            val userAddress = userAddressRepository.getAddresses(userId).primary()
            if (userAddress == null) {
                val e = IllegalStateException("Could not find PrimaryAddress")
                PassLogger.w(TAG, e, "Error in performShareRefresh")
                return LoadingResult.Error(e)
            }

            val keys = userKeys[entity.inviterEmail]?.keys?.map { it.publicKey }
            if (keys == null) {
                val e = KeyNotFound("UserKey for inviterEmail")
                PassLogger.w(
                    TAG,
                    e,
                    "Could not find UserKey for inviterEmail [email=${entity.inviterEmail}]" +
                        "[shareId=${entity.id}]"
                )
                return LoadingResult.Error(e)
            }

            when (
                val result: LoadingResult<Share> =
                    shareEntityToShare(userAddress, keys, entity)
            ) {
                is LoadingResult.Error -> return LoadingResult.Error(result.exception)
                LoadingResult.Loading -> return LoadingResult.Loading
                is LoadingResult.Success -> result.data
            }
        }
        return LoadingResult.Success(shareList)
    }

    private suspend fun storeShares(
        userAddress: UserAddress,
        shares: List<ShareResponse>
    ): LoadingResult<List<ShareEntity>> {
        // The ShareEntity will still not contain the reencrypted contents and signing key passphrase,
        // because for doing so we need the vault keys, and as the share has not yet been persisted to
        // the database, the vaultKey fetching would store them into the database and the shareId FK
        // would fail, so we first store the share without the reencryption, fetch the vaultKeys, and
        // then we reencrypt the data
        val entities: List<ShareResponseEntity> = shares.map { shareResponse ->
            val signingKey = SigningKey(readKey(shareResponse.signingKey, isPrimary = true))
            val vaultKeysResult = vaultKeyRepository.getVaultKeys(
                userAddress = userAddress,
                shareId = ShareId(shareResponse.shareId),
                signingKey = signingKey,
                shouldStoreLocally = false
            )
            when (vaultKeysResult) {
                is LoadingResult.Error -> return LoadingResult.Error(vaultKeysResult.exception)
                LoadingResult.Loading -> return LoadingResult.Loading
                is LoadingResult.Success -> Unit
            }
            ShareResponseEntity(
                response = shareResponse,
                entity = shareResponseToEntity(userAddress, shareResponse, vaultKeysResult.data)
            )
        }

        return database.inTransaction {
            localShareDataSource.upsertShares(entities.map { it.entity })

            // We have now inserted the shares without the reencrypted content
            // Now we fetch the vaultKeys for each share, reencrypt the contents and prepare the entities
            // with reencrypted contents
            val updatedEntities: List<ShareEntity> = entities
                .map { reencryptShareEntityContents(userAddress, it.response, it.entity) }
                .map {
                    when (it) {
                        is LoadingResult.Error -> return@inTransaction LoadingResult.Error(it.exception)
                        LoadingResult.Loading -> return@inTransaction LoadingResult.Loading
                        is LoadingResult.Success -> it.data
                    }
                }


            // Persist the updates into the database
            localShareDataSource.upsertShares(updatedEntities)
            LoadingResult.Success(updatedEntities)
        }
    }

    private suspend fun shareResponseToEntity(
        userAddress: UserAddress,
        shareResponse: ShareResponse,
        vaultKeys: List<VaultKey>
    ): ShareEntity =
        try {
            innerShareResponseToEntity(
                userAddress = userAddress,
                shareResponse = shareResponse,
                vaultKeys = vaultKeys,
                keyAddressSource = Source.LocalIfAvailable
            )
        } catch (e: InvalidAddressSignature) {
            PassLogger.i(TAG, e, "Received InvalidAddressSignature. Retrying re-fetching the keys")
            innerShareResponseToEntity(
                userAddress = userAddress,
                shareResponse = shareResponse,
                vaultKeys = vaultKeys,
                keyAddressSource = Source.RemoteNoCache
            )
        }

    private suspend fun innerShareResponseToEntity(
        userAddress: UserAddress,
        shareResponse: ShareResponse,
        vaultKeys: List<VaultKey>,
        keyAddressSource: Source
    ): ShareEntity {
        val inviterKeys = keyRepository.getPublicAddress(
            userAddress.userId,
            shareResponse.inviterEmail,
            source = keyAddressSource
        ).keys.publicKeyRing().keys
        val contentSignatureKeys = if (shareResponse.contentSignatureEmail != null) {
            keyRepository.getPublicAddress(
                userAddress.userId,
                shareResponse.contentSignatureEmail,
                source = keyAddressSource
            ).keys.publicKeyRing().keys
        } else {
            emptyList()
        }
        return shareResponseToEntity(
            shareResponse,
            userAddress,
            inviterKeys,
            contentSignatureKeys,
            vaultKeys
        )
    }

    private fun createVaultRequest(
        vault: NewVault,
        userAddress: UserAddress
    ): Pair<CreateVaultRequest, VaultItemKeyList> {
        val metadata = VaultV1.Vault.newBuilder()
            .setName(vault.name.decrypt(cryptoContext.keyStoreCrypto))
            .setDescription(vault.description.decrypt(cryptoContext.keyStoreCrypto))
            .build()
        val (request, keys) = createVault.createVaultRequest(metadata, userAddress)
        return request.toRequest() to keys.toVaultItemKeyList()
    }

    internal data class ShareResponseEntity(
        val response: ShareResponse,
        val entity: ShareEntity
    )

    companion object {
        private const val TAG = "ShareRepositoryImpl"
    }
}
