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

package proton.android.pass.commonpresentation.impl.items.details.handlers

import kotlinx.coroutines.flow.Flow
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandler
import proton.android.pass.commonpresentation.api.items.details.handlers.ItemDetailsHandlerObserver
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.items.ItemCategory
import javax.inject.Inject

class ItemDetailsHandlerImpl @Inject constructor(
    private val observers: Map<ItemCategory, @JvmSuppressWildcards ItemDetailsHandlerObserver>,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
) : ItemDetailsHandler {

    override fun observeItemDetails(
        item: Item,
    ): Flow<ItemDetailState> = getItemDetailsObserver(item.itemType.category)
        .observe(item)

    override fun onItemDetailsFieldClicked(text: String) {
        clipboardManager.copyToClipboard(text = text, isSecure = false)
    }

    override fun onItemDetailsHiddenFieldClicked(hiddenState: HiddenState) {
        val text = when (hiddenState) {
            is HiddenState.Empty -> ""
            is HiddenState.Revealed -> hiddenState.clearText
            is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                decrypt(hiddenState.encrypted)
            }
        }

        clipboardManager.copyToClipboard(text = text, isSecure = true)
    }

    override fun onItemDetailsHiddenFieldToggled(
        isVisible: Boolean,
        hiddenState: HiddenState,
        itemCategory: ItemCategory,
    ) {
        encryptionContextProvider.withEncryptionContext {
            when {
                isVisible -> HiddenState.Revealed(
                    encrypted = hiddenState.encrypted,
                    clearText = decrypt(hiddenState.encrypted),
                )

                decrypt(hiddenState.encrypted.toEncryptedByteArray()).isEmpty() -> HiddenState.Empty(
                    encrypt("")
                )

                else -> HiddenState.Concealed(encrypted = hiddenState.encrypted)
            }
        }.let { toggledHiddenState ->
            getItemDetailsObserver(itemCategory).updateHiddenState(toggledHiddenState)
        }
    }

    private fun getItemDetailsObserver(itemCategory: ItemCategory) = observers[itemCategory]
        ?: throw IllegalStateException("Unsupported item category: $itemCategory")
}
