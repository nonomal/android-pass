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

package proton.android.pass.composecomponents.impl.attachments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.items.ItemCategory

@Composable
fun AttachmentSection(
    modifier: Modifier = Modifier,
    files: List<Attachment>,
    loadingFile: Option<Attachment>,
    isDetail: Boolean,
    colors: PassItemColors,
    onAttachmentOptions: (Attachment) -> Unit,
    onAttachmentOpen: (Attachment) -> Unit,
    onAddAttachment: () -> Unit,
    onTrashAll: () -> Unit
) {
    if (files.isEmpty() && isDetail) return
    Column(
        modifier = modifier
            .applyIf(
                condition = !isDetail,
                ifTrue = { roundedContainerNorm() },
                ifFalse = { roundedContainer(Color.Transparent, ProtonTheme.colors.separatorNorm) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        AttachmentHeader(
            modifier = Modifier.padding(
                start = Spacing.medium,
                top = Spacing.medium,
                end = Spacing.medium
            ),
            colors = colors,
            isEnabled = loadingFile is None,
            fileAmount = files.size,
            onTrashAll = onTrashAll.takeIf { !isDetail }
        )
        Column {
            files.forEachIndexed { index, file ->
                val isLoading = loadingFile.value()?.id == file.id
                AttachmentRow(
                    innerModifier = Modifier
                        .padding(horizontal = Spacing.medium)
                        .padding(top = Spacing.small)
                        .applyIf(
                            condition = files.lastIndex == index && isDetail,
                            ifTrue = { padding(bottom = Spacing.medium) },
                            ifFalse = { padding(bottom = Spacing.small) }
                        ),
                    filename = file.name,
                    attachmentType = file.type,
                    size = file.size,
                    createTime = file.createTime,
                    isEnabled = loadingFile is None,
                    isLoading = isLoading,
                    onOptionsClick = { onAttachmentOptions(file) },
                    onAttachmentOpen = { onAttachmentOpen(file) }
                )
                if (index < files.lastIndex) {
                    PassDivider()
                }
            }
        }
        if (!isDetail) {
            AddAttachmentButton(
                modifier = Modifier.padding(
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = Spacing.medium
                ),
                colors = colors,
                isEnabled = loadingFile is None,
                onClick = onAddAttachment
            )
        }
    }
}

class ThemeAttachmentSectionPreviewProvider :
    ThemePairPreviewProvider<AttachmentSectionInput>(AttachmentSectionPreviewProvider())

@Preview
@Composable
fun AttachmentSectionPreview(
    @PreviewParameter(ThemeAttachmentSectionPreviewProvider::class) input: Pair<Boolean, AttachmentSectionInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AttachmentSection(
                colors = passItemColors(itemCategory = ItemCategory.Login),
                files = input.second.files,
                loadingFile = input.second.loadingFile,
                isDetail = input.second.isDetail,
                onAttachmentOptions = {},
                onAttachmentOpen = {},
                onAddAttachment = {},
                onTrashAll = {}
            )
        }
    }
}
