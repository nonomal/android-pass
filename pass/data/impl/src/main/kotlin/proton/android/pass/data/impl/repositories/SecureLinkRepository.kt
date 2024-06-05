/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.data.api.usecases.publiclink.SecureLinkOptions
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteSecureLinkDataSource
import proton.android.pass.data.impl.requests.CreateSecureLinkRequest
import proton.android.pass.data.impl.responses.GetSecureLinkResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import javax.inject.Inject

interface SecureLinkRepository {

    suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): String

    fun observeSecureLinks(userId: UserId): Flow<List<SecureLink>>

}

class SecureLinkRepositoryImpl @Inject constructor(
    private val localItemDataSource: LocalItemDataSource,
    private val localShareKeyDataSource: LocalShareKeyDataSource,
    private val remoteSecureLinkDataSource: RemoteSecureLinkDataSource,
    private val encryptionContextProvider: EncryptionContextProvider
) : SecureLinkRepository {

    override suspend fun createSecureLink(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        options: SecureLinkOptions
    ): String {
        val item = localItemDataSource.getById(shareId, itemId) ?: throw IllegalStateException(
            "Item not found [shareId=${shareId.id}] [itemId=${itemId.id}]"
        )

        val key = item.encryptedKey ?: throw IllegalStateException(
            "Item does not have an itemKey [shareId=${shareId.id}] [itemId=${itemId.id}]"
        )

        val decryptedKey = encryptionContextProvider.withEncryptionContext { decrypt(key) }

        val linkKey = EncryptionKey.generate()
        val encryptedItemKey = encryptionContextProvider.withEncryptionContext(linkKey.clone()) {
            encrypt(decryptedKey, EncryptionTag.ItemKey)
        }
        val encodedEncryptedItemKey = Base64.encodeBase64String(encryptedItemKey.array)

        val shareKeyInstance = localShareKeyDataSource.getLatestKeyForShare(shareId).firstOrNull()
            ?: throw IllegalStateException("No share key found for share [shareId=${shareId.id}]")

        val shareKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKeyInstance.symmetricallyEncryptedKey))
        }

        val encryptedLinkKey = encryptionContextProvider.withEncryptionContext(shareKey) {
            encrypt(linkKey.value(), EncryptionTag.LinkKey)
        }

        val encodedEncryptedLinkKey = Base64.encodeBase64String(encryptedLinkKey.array)

        val request = CreateSecureLinkRequest(
            revision = item.revision,
            expirationTime = options.expirationTime.inWholeSeconds,
            maxReadCount = options.maxReadCount,
            encryptedItemKey = encodedEncryptedItemKey,
            encryptedLinkKey = encodedEncryptedLinkKey,
            linkKeyShareKeyRotation = shareKeyInstance.rotation
        )

        val response = remoteSecureLinkDataSource.createSecureLink(
            userId = userId,
            shareId = shareId,
            itemId = itemId,
            request = request
        )

        val encodedLinkKey = Base64.encodeBase64String(linkKey.value(), Base64.Mode.UrlSafe)
        val concatenated = "${response.url}#$encodedLinkKey"

        return concatenated
    }

    override fun observeSecureLinks(userId: UserId): Flow<List<SecureLink>> = oneShot {
        // To be changed to observing the local database when we implement the local data source
        fetchSecureLinksFromRemote(userId)
    }

    private suspend fun fetchSecureLinksFromRemote(userId: UserId): List<SecureLink> {
        val remoteLinks = remoteSecureLinkDataSource.getAllSecureLinks(userId)

        // Retrieve all the ShareKeys we'll need to decrypt the SecureLinks
        val shareKeys = getAllShareKeysForShares(userId, remoteLinks)

        val mapped = remoteLinks.mapNotNull { link ->
            val shareKey = shareKeys[ShareId(link.shareId)]
                ?: return@mapNotNull null

            val linkKey = encryptionContextProvider.withEncryptionContext(shareKey.clone()) {
                val encryptedLinkKey = Base64.decodeBase64(link.encryptedLinkKey)
                decrypt(EncryptedByteArray(encryptedLinkKey), EncryptionTag.LinkKey)
            }

            val encodedLinkKey = Base64.encodeBase64String(linkKey, Base64.Mode.UrlSafe)
            val fullUrl = "${link.linkUrl}#$encodedLinkKey"

            SecureLink(
                id = SecureLinkId(link.linkId),
                shareId = ShareId(link.shareId),
                itemId = ItemId(link.itemId),
                expiration = Instant.fromEpochSeconds(link.expirationTime),
                maxReadCount = link.maxReadCount,
                readCount = link.readCount,
                url = fullUrl
            )
        }

        // Clear all encryption keys from memory
        shareKeys.values.forEach { it.clear() }

        return mapped
    }

    private suspend fun getAllShareKeysForShares(
        userId: UserId,
        secureLinks: List<GetSecureLinkResponse>
    ): Map<ShareId, EncryptionKey> {
        val shareKeyRequests = secureLinks.map { ShareKeyForLink.fromResponse(it) }.distinct()

        val res: Map<ShareId, EncryptionKey?> = encryptionContextProvider
            .withEncryptionContextSuspendable {
                coroutineScope {
                    shareKeyRequests.map { request ->
                        async {
                            val shareKey = localShareKeyDataSource
                                .getForShareAndRotation(
                                    userId = userId,
                                    shareId = request.shareId,
                                    rotation = request.rotation
                                )
                                .firstOrNull()
                                ?.let { shareKeyEntity ->
                                    EncryptionKey(decrypt(shareKeyEntity.symmetricallyEncryptedKey))
                                }
                            request.shareId to shareKey
                        }
                    }.awaitAll().toMap()
                }
            }

        return res.filterValues { it != null }.mapValues { it.value!! }
    }

    private data class ShareKeyForLink(
        val shareId: ShareId,
        val rotation: Long
    ) {
        companion object {
            fun fromResponse(response: GetSecureLinkResponse) = ShareKeyForLink(
                ShareId(response.shareId),
                response.linkKeyShareKeyRotation
            )
        }
    }
}