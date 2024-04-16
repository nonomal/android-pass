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

package proton.android.pass.features.security.center.breachdetail.navigation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachCustomEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.features.security.center.shared.navigation.BreachEmailIdArgId
import proton.android.pass.features.security.center.shared.navigation.BreachIdArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavItemType

object SecurityCenterCustomEmailBreachDetailNavItem : NavItem(
    baseRoute = "security/center/customemailbreachdetail",
    navItemType = NavItemType.Bottomsheet,
    navArgIds = listOf(BreachIdArgId, BreachEmailIdArgId)
) {
    fun createNavRoute(breachId: BreachId, customEmailId: BreachCustomEmailId): String =
        "$baseRoute/${breachId.id}/${customEmailId.id}"
}

object SecurityCenterAliasBreachDetailNavItem : NavItem(
    baseRoute = "security/center/aliasbreachdetail",
    navItemType = NavItemType.Bottomsheet,
    navArgIds = listOf(
        BreachIdArgId,
        CommonNavArgId.ShareId,
        CommonNavArgId.ItemId
    )
) {
    fun createNavRoute(
        breachId: BreachId,
        shareId: ShareId,
        itemId: ItemId
    ): String = "$baseRoute/${breachId.id}/${shareId.id}/${itemId.id}"
}
