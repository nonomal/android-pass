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

package proton.android.pass.data.impl.local.shares

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.shares.ShareMember

interface LocalShareMembersDataSource {

    fun getShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    ): ShareMember?

    fun upsertShareMember(
        userId: UserId,
        shareId: ShareId,
        shareMember: ShareMember
    )

    fun deleteShareMember(
        userId: UserId,
        shareId: ShareId,
        memberShareId: ShareId
    )

    fun observeShareMembers(userId: UserId, shareId: ShareId): Flow<List<ShareMember>>

    fun upsertShareMembers(
        userId: UserId,
        shareId: ShareId,
        shareMembers: List<ShareMember>
    )

}
