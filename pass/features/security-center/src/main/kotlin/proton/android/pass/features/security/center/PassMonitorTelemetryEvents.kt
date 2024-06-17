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

package proton.android.pass.features.security.center

import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent

data object PassMonitorDisplayHome :
    DeferredTelemetryEvent("pass_monitor.display_home")

data object PassMonitorDisplayWeakPasswords :
    DeferredTelemetryEvent("pass_monitor.display_weak_passwords")

data object PassMonitorDisplayReusedPasswords :
    DeferredTelemetryEvent("pass_monitor.display_reused_passwords")

data object PassMonitorDisplayMissing2FA :
    DeferredTelemetryEvent("pass_monitor.display_missing_2fa")

data object PassMonitorDisplayExcludedItems :
    DeferredTelemetryEvent("pass_monitor.display_excluded_items")

data object PassMonitorDisplayDarkWebMonitoring :
    DeferredTelemetryEvent("pass_monitor.display_dark_web_monitoring")

data object PassMonitorDisplayMonitoringProtonAddresses :
    DeferredTelemetryEvent("pass_monitor.display_monitoring_proton_addresses")

data object PassMonitorDisplayMonitoringEmailAliases :
    DeferredTelemetryEvent("pass_monitor.display_monitoring_email_aliases")

data object PassMonitorAddCustomEmailFromSuggestion :
    DeferredTelemetryEvent("pass_monitor.add_custom_email_from_suggestion")

