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

package proton.android.pass.featureitemcreate.impl.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.datamodels.api.toContent
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.InitError
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.ItemUpdateError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import proton.pass.domain.CustomField
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
class UpdateLoginViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val createAlias: CreateAlias,
    accountManager: AccountManager,
    clipboardManager: ClipboardManager,
    private val totpManager: TotpManager,
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandleProvider,
    draftRepository: DraftRepository,
) : BaseLoginViewModel(
    accountManager = accountManager,
    snackbarDispatcher = snackbarDispatcher,
    clipboardManager = clipboardManager,
    totpManager = totpManager,
    observeCurrentUser = observeCurrentUser,
    observeUpgradeInfo = observeUpgradeInfo,
    draftRepository = draftRepository,
    encryptionContextProvider = encryptionContextProvider
) {
    private val navShareId: ShareId =
        ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))
    private val navItemId: ItemId =
        ItemId(savedStateHandle.get().require(CommonNavArgId.ItemId.key))
    private val navShareIdState: MutableStateFlow<ShareId> = MutableStateFlow(navShareId)
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private var _item: Item? = null

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (_item != null) return@launch

            isLoadingState.update { IsLoadingState.Loading }
            runCatching { getItemById.invoke(navShareId, navItemId).first() }
                .onSuccess { item ->
                    _item = item
                    onItemReceived(item)
                }
                .onFailure {
                    PassLogger.i(TAG, it, "Get by id error")
                    snackbarDispatcher(InitError)
                }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    val updateLoginUiState: StateFlow<UpdateLoginUiState> = combine(
        navShareIdState,
        baseLoginUiState,
        ::UpdateLoginUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = encryptionContextProvider.withEncryptionContext {
            UpdateLoginUiState.create(
                password = HiddenState.Empty(encrypt("")),
                primaryTotp = HiddenState.Empty(encrypt("")),
            )
        }
    )

    fun setAliasItem(aliasItem: AliasItem) {
        canUpdateUsernameState.update { false }
        aliasLocalItemState.update { aliasItem.toOption() }
        itemContentState.update {
            it.copy(
                username = aliasItem.aliasToBeCreated ?: it.username
            )
        }
    }

    fun setTotp(navTotpUri: String?, navTotpIndex: Int?) {
        onUserEditedContent()
        val currentValue = itemContentState.value
        val primaryTotp = updatePrimaryTotpIfNeeded(navTotpUri, navTotpIndex, currentValue)
        val customFields = updateCustomFieldsIfNeeded(navTotpUri, navTotpIndex ?: -1, currentValue)
        itemContentState.update {
            it.copy(
                primaryTotp = primaryTotp,
                customFields = customFields
            )
        }
    }

    fun updateItem(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val currentItem = _item
        requireNotNull(currentItem)
        val shouldUpdate = validateItem()
        if (!shouldUpdate) return@launch

        isLoadingState.update { IsLoadingState.Loading }
        val loginItem = itemContentState.value
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null) {
            val aliasItemOption = aliasLocalItemState.value
            if (aliasItemOption is Some) {
                performCreateAlias(userId, shareId, aliasItemOption.value)
                    .map { performUpdateItem(userId, shareId, currentItem, loginItem) }
            } else {
                performUpdateItem(userId, shareId, currentItem, loginItem)
            }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun onItemReceived(item: Item) {
        val itemContents = item.itemType as ItemType.Login

        val websites = if (itemContents.websites.isEmpty()) {
            persistentListOf("")
        } else {
            itemContents.websites.toImmutableList()
        }

        val loginItem = encryptionContextProvider.withEncryptionContext {
            val decryptedTotp = handleTotp(
                encryptionContext = this@withEncryptionContext,
                primaryTotp = itemContents.primaryTotp
            )
            val isPasswordEmpty = decrypt(itemContents.password.toEncryptedByteArray())
                .isEmpty()
            val passwordHiddenState = if (isPasswordEmpty) {
                HiddenState.Empty(itemContents.password)
            } else {
                HiddenState.Concealed(itemContents.password)
            }

            ItemContents.Login(
                title = decrypt(item.title),
                username = itemContents.username,
                password = passwordHiddenState,
                urls = websites,
                note = decrypt(item.note),
                packageInfoSet = item.packageInfoSet,
                primaryTotp = HiddenState.Revealed(encrypt(decryptedTotp), decryptedTotp),
                customFields = itemContents.customFields.mapNotNull {
                    convertCustomField(it, this@withEncryptionContext)
                }.toImmutableList()
            )
        }

        itemContentState.update { loginItem }
    }

    private suspend fun performCreateAlias(
        userId: UserId,
        shareId: ShareId,
        aliasItem: AliasItem
    ): Result<Item> =
        if (aliasItem.selectedSuffix != null) {
            runCatching {
                createAlias(
                    userId = userId,
                    shareId = shareId,
                    newAlias = NewAlias(
                        title = aliasItem.title,
                        note = aliasItem.note,
                        prefix = aliasItem.prefix,
                        suffix = aliasItem.selectedSuffix.toDomain(),
                        mailboxes = aliasItem.mailboxes
                            .filter { it.selected }
                            .map { it.model }
                            .map(AliasMailboxUiModel::toDomain)
                    )
                )
            }.onFailure {
                PassLogger.e(TAG, it, "Error creating alias")
            }
        } else {
            val message = "Empty suffix on create alias"
            PassLogger.i(TAG, message)
            snackbarDispatcher(AliasSnackbarMessage.ItemCreationError)
            Result.failure(Exception(message))
        }

    private suspend fun performUpdateItem(
        userId: UserId,
        shareId: ShareId,
        currentItem: Item,
        contents: ItemContents.Login
    ) {
        runCatching {
            updateItem(userId, shareId, currentItem, contents)
        }.onSuccess { item ->
            isItemSavedState.update {
                encryptionContextProvider.withEncryptionContext {
                    ItemSavedState.Success(
                        item.id,
                        item.toUiModel(this@withEncryptionContext)
                    )
                }
            }
            telemetryManager.sendEvent(ItemUpdate(EventItemType.Login))
        }.onFailure {
            PassLogger.e(TAG, it, "Update item error")
            snackbarDispatcher(ItemUpdateError)
        }
    }

    private fun convertCustomField(
        customField: CustomField,
        encryptionContext: EncryptionContext
    ): CustomFieldContent? {
        val isConcealed = when (customField) {
            is CustomField.Hidden -> true
            else -> false
        }
        val mapped = customField.toContent(encryptionContext, isConcealed = isConcealed)
        return if (mapped is CustomFieldContent.Totp) {
            val uri = when (val value = mapped.value) {
                is HiddenState.Concealed -> encryptionContext.decrypt(value.encrypted)
                is HiddenState.Empty -> ""
                is HiddenState.Revealed -> value.clearText
            }

            val totp = getDisplayTotp(uri)
            CustomFieldContent.Totp(
                label = mapped.label,
                value = HiddenState.Revealed(
                    encrypted = encryptionContext.encrypt(totp),
                    clearText = totp
                )
            )
        } else {
            mapped
        }
    }

    private fun handleTotp(
        encryptionContext: EncryptionContext,
        primaryTotp: EncryptedString
    ): String {
        val totp = encryptionContext.decrypt(primaryTotp)
        if (totp.isBlank()) return totp

        itemHadTotpState.update { true }
        return getDisplayTotp(totp)
    }

    private fun getDisplayTotp(totp: String): String {
        if (totp.isBlank()) return totp

        return totpManager.parse(totp)
            .fold(
                onSuccess = { spec ->
                    if (spec.isUsingDefaultParameters()) {
                        spec.secret
                    } else {
                        totp
                    }
                },
                onFailure = { totp }
            )
    }

    companion object {
        private const val TAG = "UpdateLoginViewModel"
    }
}
