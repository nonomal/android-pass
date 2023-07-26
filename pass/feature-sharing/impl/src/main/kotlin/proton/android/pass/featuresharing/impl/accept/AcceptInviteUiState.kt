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

package proton.android.pass.featuresharing.impl.accept

import androidx.compose.runtime.Stable
import proton.pass.domain.PendingInvite

sealed interface AcceptInviteEvent {
    object Unknown : AcceptInviteEvent
    object Close : AcceptInviteEvent
}

@Stable
sealed interface AcceptInviteUiState {
    @Stable
    object Loading : AcceptInviteUiState

    @Stable
    object Error : AcceptInviteUiState

    @Stable
    data class Content(
        val invite: PendingInvite,
        val isConfirmLoading: Boolean,
        val isRejectLoading: Boolean,
        val areButtonsEnabled: Boolean,
        val event: AcceptInviteEvent
    ) : AcceptInviteUiState
}