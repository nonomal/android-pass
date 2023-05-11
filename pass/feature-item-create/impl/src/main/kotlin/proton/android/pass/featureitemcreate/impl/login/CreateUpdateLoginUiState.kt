package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.runtime.Immutable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.OpenScanState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.pass.domain.VaultWithItemCount

@Immutable
data class CreateUpdateLoginUiState(
    val vaultList: List<VaultWithItemCount>,
    val selectedVault: VaultWithItemCount?,
    val loginItem: LoginItem,
    val aliasItem: AliasItem?,
    val validationErrors: Set<LoginItemValidationErrors>,
    val isLoadingState: IsLoadingState,
    val isItemSaved: ItemSavedState,
    val openScanState: OpenScanState,
    val focusLastWebsite: Boolean,
    val canUpdateUsername: Boolean,
    val primaryEmail: String?,
    val showVaultSelector: Boolean,
    val hasUserEditedContent: Boolean,
    val hasReachedAliasLimit: Boolean,
    val totpUiState: TotpUiState
) {
    companion object {
        val Initial = CreateUpdateLoginUiState(
            vaultList = emptyList(),
            selectedVault = null,
            aliasItem = null,
            isLoadingState = IsLoadingState.NotLoading,
            loginItem = LoginItem.Empty,
            validationErrors = emptySet(),
            isItemSaved = ItemSavedState.Unknown,
            openScanState = OpenScanState.Unknown,
            focusLastWebsite = false,
            canUpdateUsername = true,
            primaryEmail = null,
            showVaultSelector = false,
            hasUserEditedContent = false,
            hasReachedAliasLimit = false,
            totpUiState = TotpUiState.NotInitialised,
        )
    }
}

sealed interface TotpUiState {
    @Immutable
    object NotInitialised : TotpUiState

    @Immutable
    object Loading : TotpUiState

    @Immutable
    object Error : TotpUiState

    @Immutable
    object Success : TotpUiState

    @Immutable
    data class Limited(val isEdit: Boolean) : TotpUiState
}
