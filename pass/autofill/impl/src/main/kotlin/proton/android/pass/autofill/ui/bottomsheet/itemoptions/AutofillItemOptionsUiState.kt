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

package proton.android.pass.autofill.ui.bottomsheet.itemoptions

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface AutofillItemOptionsEvent {
    object Unknown : AutofillItemOptionsEvent
    object Close : AutofillItemOptionsEvent
}

@Stable
data class AutofillItemOptionsUiState(
    val isLoading: IsLoadingState,
    val event: AutofillItemOptionsEvent
) {
    companion object {
        val Initial = AutofillItemOptionsUiState(
            isLoading = IsLoadingState.NotLoading,
            event = AutofillItemOptionsEvent.Unknown
        )
    }
}
