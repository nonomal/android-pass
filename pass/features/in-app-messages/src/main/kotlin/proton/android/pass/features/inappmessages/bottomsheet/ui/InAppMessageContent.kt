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

package proton.android.pass.features.inappmessages.bottomsheet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTARoute
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageMode

@Composable
fun InAppMessageContent(
    modifier: Modifier = Modifier,
    inAppMessage: InAppMessage,
    onCTAClick: (InAppMessageCTARoute) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = modifier.background(PassTheme.colors.bottomSheetBackground),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InAppMessageHeader(
            imageUrl = inAppMessage.imageUrl.value(),
            onClose = onClose
        )
        InAppMessageBody(
            modifier = Modifier.padding(Spacing.medium),
            title = inAppMessage.title,
            message = inAppMessage.message.value()
        )
        val ctaText = inAppMessage.ctaText
        val ctaRoute = inAppMessage.ctaRoute
        if (ctaText is Some && ctaRoute is Some) {
            InAppMessageFooter(
                modifier = Modifier.padding(Spacing.medium),
                ctaText = ctaText.value,
                ctaRoute = ctaRoute.value,
                onCTAClick = onCTAClick
            )
        }
    }
}

@Preview
@Composable
fun InAppMessageContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            InAppMessageContent(
                inAppMessage = InAppMessage(
                    title = "Upgrade to Pass Plus",
                    message = "Get access to all features".some(),
                    ctaText = "Upgrade".some(),
                    ctaRoute = "pass://upgrade".some().map(::InAppMessageCTARoute),
                    imageUrl = "url".some(),
                    mode = InAppMessageMode.Modal,
                    id = InAppMessageId("q")
                ),
                onCTAClick = {},
                onClose = {}
            )
        }
    }
}
