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

package proton.android.pass.featurevault.impl

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetCreateVaultGraph
import proton.android.pass.featurevault.impl.bottomsheet.bottomSheetEditVaultGraph
import proton.android.pass.featurevault.impl.bottomsheet.options.bottomSheetVaultOptionsGraph
import proton.android.pass.featurevault.impl.bottomsheet.select.selectVaultBottomsheetGraph
import proton.android.pass.featurevault.impl.delete.deleteVaultDialogGraph
import proton.pass.domain.ShareId

sealed interface VaultNavigation {
    object Upgrade : VaultNavigation
    object Close : VaultNavigation
    data class VaultSelected(val shareId: ShareId) : VaultNavigation
    data class VaultMigrate(val shareId: ShareId) : VaultNavigation
    data class VaultEdit(val shareId: ShareId) : VaultNavigation
    data class VaultRemove(val shareId: ShareId) : VaultNavigation
}

fun NavGraphBuilder.vaultGraph(
    onNavigate: (VaultNavigation) -> Unit,
) {
    bottomSheetCreateVaultGraph(onNavigate)
    bottomSheetEditVaultGraph(onNavigate)
    deleteVaultDialogGraph(onNavigate)
    selectVaultBottomsheetGraph(onNavigate)
    bottomSheetVaultOptionsGraph(onNavigate)
}
