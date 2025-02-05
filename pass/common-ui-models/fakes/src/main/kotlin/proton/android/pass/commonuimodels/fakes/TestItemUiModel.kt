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

package proton.android.pass.commonuimodels.fakes

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

object TestItemUiModel {

    fun create(
        title: String = "item-title",
        note: String = "item-note",
        itemContents: ItemContents = ItemContents.Note(title, note),
        createTime: Instant = Clock.System.now(),
        modificationTime: Instant = Clock.System.now(),
        lastAutofillTime: Instant? = null
    ): ItemUiModel {
        return ItemUiModel(
            id = ItemId(id = "item-id"),
            shareId = ShareId(id = "share-id"),
            contents = itemContents,
            createTime = createTime,
            state = 0,
            modificationTime = modificationTime,
            lastAutofillTime = lastAutofillTime,
        )
    }
}
