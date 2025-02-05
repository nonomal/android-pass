/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.account.fakes.TestPublicAddressRepository
import proton.android.pass.account.fakes.TestUserAddressRepository
import proton.android.pass.crypto.fakes.usecases.TestEncryptInviteKeys
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.data.impl.fakes.TestRemoteInviteDataSource
import proton.android.pass.data.impl.fakes.TestShareKeyRepository
import proton.pass.domain.ShareId

class InviteToVaultImplTest {

    private lateinit var instance: InviteToVaultImpl

    private lateinit var remoteDataSource: TestRemoteInviteDataSource
    private lateinit var accountManager: TestAccountManager
    private lateinit var publicAddressRepository: TestPublicAddressRepository
    private lateinit var userAddressRepository: TestUserAddressRepository

    @Before
    fun setup() {
        remoteDataSource = TestRemoteInviteDataSource()
        publicAddressRepository = TestPublicAddressRepository()
        accountManager = TestAccountManager()
        userAddressRepository = TestUserAddressRepository()

        instance = InviteToVaultImpl(
            publicAddressRepository = publicAddressRepository,
            userAddressRepository = userAddressRepository,
            accountManager = accountManager,
            encryptInviteKeys = TestEncryptInviteKeys(),
            shareKeyRepository = TestShareKeyRepository().apply {
                emitGetShareKeys(listOf(TestUtils.createShareKey().first))
            },
            remoteInviteDataSource = remoteDataSource,
        )
    }

    @Test
    fun `invite to vault does not go kaboom`() = runTest {
        setupAccountManager()
        setupPublicAddress()
        setupUserAddress()

        val shareId = ShareId("shareId123")
        val res = instance.invoke(targetEmail = INVITED_ADDRESS, shareId = shareId)
        assertThat(res.isSuccess).isTrue()

        val memory = remoteDataSource.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryValue = memory.first()
        assertThat(memoryValue.userId).isEqualTo(UserId(USER_ID))
        assertThat(memoryValue.shareId).isEqualTo(shareId)
        assertThat(memoryValue.request.email).isEqualTo(INVITED_ADDRESS)
    }

    @Test
    fun `invite to vault returns failure if there is no current user`() = runTest {
        setupAccountManager(null)
        setupPublicAddress()
        setupUserAddress()

        val res = instance.invoke(targetEmail = INVITED_ADDRESS, shareId = ShareId("shareId123"))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if there is no public address for target user`() = runTest {
        setupAccountManager()
        setupUserAddress()

        val res = instance.invoke(targetEmail = INVITED_ADDRESS, shareId = ShareId("shareId123"))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `invite to vault returns failure if there is no user address for current user`() = runTest {
        setupAccountManager()
        setupPublicAddress()

        val res = instance.invoke(targetEmail = INVITED_ADDRESS, shareId = ShareId("shareId123"))
        assertThat(res.isFailure).isTrue()
    }

    private fun setupAccountManager(userId: UserId? = UserId(USER_ID)) {
        accountManager.sendPrimaryUserId(userId)
    }

    private fun setupPublicAddress() {
        val key = PublicKey(
            key = "InvitedKey",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
        publicAddressRepository.setAddress(INVITED_ADDRESS, key)
    }

    private fun setupUserAddress() {
        val addressId = AddressId("AddressId123")
        val key = UserAddressKey(
            addressId = addressId,
            version = 1,
            flags = 0,
            active = true,
            keyId = KeyId("KeyId123"),
            privateKey = PrivateKey(
                key = "key",
                isPrimary = true,
                passphrase = null
            )
        )
        val userAddress = UserAddress(
            userId = UserId(USER_ID),
            addressId = addressId,
            email = INVITER_ADDRESS,
            canSend = true,
            canReceive = true,
            enabled = true,
            keys = listOf(key),
            signedKeyList = null,
            order = 1
        )
        userAddressRepository.setAddresses(listOf(userAddress))
    }

    companion object {
        private const val INVITER_ADDRESS = "inviter@local"
        private const val INVITED_ADDRESS = "invited@remote"
        private const val USER_ID = "InviteToVaultImplTest-UserId"
    }
}
