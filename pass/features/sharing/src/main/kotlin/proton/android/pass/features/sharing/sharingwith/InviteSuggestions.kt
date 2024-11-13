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

package proton.android.pass.features.sharing.sharingwith

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.CircleTextIcon
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.sharing.R

@Composable
internal fun InviteSuggestions(
    modifier: Modifier = Modifier,
    state: SuggestionsUIState,
    onItemClicked: (String, Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Text.Body2Medium(
            text = stringResource(id = R.string.share_with_suggestions_title),
            color = PassTheme.colors.textWeak
        )

        when (state) {
            SuggestionsUIState.Initial -> Unit

            SuggestionsUIState.Loading -> PassFullScreenLoading()

            is SuggestionsUIState.Content -> {
                var selectedIndex by remember { mutableIntStateOf(0) }
                if (state.planEmails.isNotEmpty()) {
                    TabRow(
                        modifier = Modifier.clip(CircleShape),
                        selectedTabIndex = selectedIndex,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        indicator = {},
                        divider = {}
                    ) {
                        InviteSuggestionTabs.entries.forEachIndexed { index, tab ->
                            val selected = selectedIndex == index
                            Tab(
                                modifier = Modifier
                                    .padding(Spacing.extraSmall)
                                    .clip(CircleShape)
                                    .applyIf(
                                        condition = selected,
                                        ifTrue = { background(PassTheme.colors.interactionNorm) }
                                    ),
                                content = {
                                    when (tab) {
                                        InviteSuggestionTabs.Recent -> {
                                            stringResource(id = R.string.share_with_recents_title)
                                        }

                                        InviteSuggestionTabs.GroupSuggestions -> {
                                            state.groupDisplayName
                                        }
                                    }.also { title ->
                                        Text.Body3Regular(
                                            modifier = Modifier.padding(vertical = Spacing.small),
                                            text = title
                                        )
                                    }
                                },
                                selected = selected,
                                onClick = { selectedIndex = index }
                            )
                        }
                    }
                }

                when (InviteSuggestionTabs.entries[selectedIndex]) {
                    InviteSuggestionTabs.Recent -> state.recentEmails
                    InviteSuggestionTabs.GroupSuggestions -> state.planEmails
                }.also { suggestionItems ->
                    InviteSuggestionList(
                        modifier = Modifier.weight(1f),
                        items = suggestionItems,
                        onItemClicked = onItemClicked
                    )
                }
            }
        }
    }
}

@Composable
internal fun InviteSuggestionList(
    modifier: Modifier = Modifier,
    items: List<Pair<String, Boolean>>,
    onItemClicked: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        content = {
            items(items = items, key = { it.first }) { (email, isChecked) ->
                Row(
                    modifier = Modifier.clickable { onItemClicked(email, isChecked) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    CircleTextIcon(
                        text = email,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        textColor = PassTheme.colors.interactionNormMajor2,
                        shape = PassTheme.shapes.squircleMediumShape
                    )

                    Text.Body2Regular(
                        modifier = Modifier.weight(1f),
                        text = email,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Checkbox(
                        checked = isChecked,
                        colors = CheckboxDefaults.colors(
                            checkedColor = PassTheme.colors.interactionNormMajor2
                        ),
                        onCheckedChange = { onItemClicked(email, isChecked) }
                    )
                }
            }
        }
    )
}

private enum class InviteSuggestionTabs {
    Recent,
    GroupSuggestions
}

@[Preview Composable]
internal fun InviteSuggestionsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            InviteSuggestions(
                state = SuggestionsUIState.Content(
                    groupDisplayName = "Group",
                    recentEmails = persistentListOf(
                        "test1@proton.me" to true,
                        "test2@proton.me" to false
                    ),
                    planEmails = persistentListOf(
                        "test1@proton.me" to true
                    )
                ),
                onItemClicked = { _, _ -> }
            )
        }
    }
}
