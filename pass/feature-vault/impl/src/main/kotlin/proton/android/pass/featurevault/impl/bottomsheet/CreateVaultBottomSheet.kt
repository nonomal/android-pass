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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.featurevault.impl.VaultNavigation

@Composable
fun CreateVaultBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (VaultNavigation) -> Unit,
    viewModel: CreateVaultViewModel = hiltViewModel()
) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val state = createState.base

    BackHandler {
        onNavigate(VaultNavigation.Close)
    }

    LaunchedEffect(state.isVaultCreatedEvent) {
        if (state.isVaultCreatedEvent == IsVaultCreatedEvent.Created) {
            onNavigate(VaultNavigation.Close)
        }
    }

    VaultBottomSheetContent(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        state = state,
        showUpgradeUi = createState.displayNeedUpgrade,
        buttonText = stringResource(R.string.bottomsheet_create_vault_button),
        onNameChange = { viewModel.onNameChange(it) },
        onIconChange = { viewModel.onIconChange(it) },
        onColorChange = { viewModel.onColorChange(it) },
        onClose = { onNavigate(VaultNavigation.Close) },
        onButtonClick = { viewModel.onCreateClick() },
        onUpgradeClick = { onNavigate(VaultNavigation.Upgrade) }
    )
}
