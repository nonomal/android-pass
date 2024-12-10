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

package proton.android.pass.featureitemcreate.impl.alias.bottomsheet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Some
import proton.android.pass.commonrust.api.AliasPrefixValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.featureitemcreate.impl.alias.AliasDraftSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItemFormState
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
import proton.android.pass.featureitemcreate.impl.alias.IsEditAliasNavArg
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CreateAliasBottomSheetViewModel @Inject constructor(
    accountManager: AccountManager,
    createAlias: CreateAlias,
    snackbarDispatcher: SnackbarDispatcher,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandleProvider: SavedStateHandleProvider,
    telemetryManager: TelemetryManager,
    draftRepository: DraftRepository,
    inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    encryptionContextProvider: EncryptionContextProvider,
    aliasPrefixValidator: AliasPrefixValidator,
    observeDefaultVault: ObserveDefaultVault,
    clearAttachments: ClearAttachments,
    uploadAttachment: UploadAttachment,
    draftAttachmentRepository: DraftAttachmentRepository,
    metadataResolver: MetadataResolver,
    linkAttachmentsToItem: LinkAttachmentsToItem,
    featureFlagsRepository: FeatureFlagsPreferencesRepository
) : CreateAliasViewModel(
    accountManager = accountManager,
    createAlias = createAlias,
    snackbarDispatcher = snackbarDispatcher,
    telemetryManager = telemetryManager,
    draftRepository = draftRepository,
    observeUpgradeInfo = observeUpgradeInfo,
    observeAliasOptions = observeAliasOptions,
    observeVaults = observeVaults,
    savedStateHandleProvider = savedStateHandleProvider,
    inAppReviewTriggerMetrics = inAppReviewTriggerMetrics,
    encryptionContextProvider = encryptionContextProvider,
    aliasPrefixValidator = aliasPrefixValidator,
    observeDefaultVault = observeDefaultVault,
    clearAttachments = clearAttachments,
    uploadAttachment = uploadAttachment,
    draftAttachmentRepository = draftAttachmentRepository,
    metadataResolver = metadataResolver,
    linkAttachmentsToItem = linkAttachmentsToItem,
    featureFlagsRepository = featureFlagsRepository
) {

    private val isEditMode: Boolean = savedStateHandleProvider.get()
        .get<Boolean>(IsEditAliasNavArg.key)
        ?: false

    init {
        isDraft = true
    }

    fun setInitialState(title: String) = viewModelScope.launch {
        if (isEditMode) {
            resetWithDraft()
        } else {
            resetWithTitle(title)
        }
    }

    fun resetAliasDraftSavedState() {
        isAliasDraftSavedState.update { AliasDraftSavedState.Unknown }
    }

    private suspend fun resetWithDraft() {
        val draft = draftRepository.get<AliasItemFormState>(KEY_DRAFT_ALIAS).firstOrNull()
        if (draft == null || draft.isEmpty()) {
            resetWithTitle("")
            return
        }

        val draftContent = draft as Some<AliasItemFormState>
        aliasItemFormMutableState = draftContent.value
    }

    private fun resetWithTitle(title: String) {
        when (val draft = draftRepository.delete<AliasItemFormState>(KEY_DRAFT_ALIAS)) {
            is Some -> {
                aliasItemFormMutableState = draft.value
            }

            else -> {
                if (aliasItemFormMutableState.prefix.isBlank()) {
                    if (title.isBlank()) {
                        onPrefixChange(randomPrefix())
                    } else {
                        titlePrefixInSync = true
                        onTitleChange(title)
                    }
                }
            }
        }
    }

    private fun randomPrefix(): String {
        val dict = "abcdefghijklmnopqrstuvwxyz0123456789"
        var res = ""
        while (res.length < PREFIX_LENGTH) {
            res += dict.random()
        }
        return res
    }

    companion object {
        private const val PREFIX_LENGTH = 6
    }
}
