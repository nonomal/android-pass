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

package proton.android.pass.features.upsell.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.upsell.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Stable
internal data class UpsellState(private val paidFeature: PaidFeature) {

    @DrawableRes
    internal val logo: Int = when (paidFeature) {
        PaidFeature.DarkWebMonitoring,
        PaidFeature.Sentinel -> R.drawable.logo_feature_pass_plus
    }

    @StringRes
    internal val title: Int = when (paidFeature) {
        PaidFeature.DarkWebMonitoring,
        PaidFeature.Sentinel -> R.string.upsell_monitor_title
    }

    @StringRes
    internal val subtitle: Int = when (paidFeature) {
        PaidFeature.DarkWebMonitoring -> R.string.upsell_dark_web_monitoring_subtitle
        PaidFeature.Sentinel -> R.string.upsell_sentinel_subtitle
    }

    internal val features: ImmutableList<Pair<Int, Int>> = persistentListOf(
        CompR.drawable.ic_shield_union to R.string.upsell_paid_feature_dark_web_monitoring,
        CoreR.drawable.ic_proton_user to R.string.upsell_paid_feature_sentinel,
        CoreR.drawable.ic_proton_lock to R.string.upsell_paid_feature_authenticator,
        CoreR.drawable.ic_proton_alias to R.string.upsell_paid_feature_unlimited_aliases,
        CoreR.drawable.ic_proton_users_plus to R.string.upsell_paid_feature_vault_sharing
    )

}
