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

package proton.android.pass.features.alias.contacts.create.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.aliascontact.CreateAliasContact
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.ContactCreateError
import proton.android.pass.features.alias.contacts.AliasContactsSnackbarMessage.ContactCreateSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class CreateAliasContactViewModel @Inject constructor(
    private val createAliasContact: CreateAliasContact,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    @OptIn(SavedStateHandleSaveableApi::class)
    var emailAddress: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val detailAliasContactEventFlow: MutableStateFlow<CreateAliasContactEvent> =
        MutableStateFlow(CreateAliasContactEvent.Idle)

    val state = detailAliasContactEventFlow
        .map { CreateAliasContactUIState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = CreateAliasContactUIState(CreateAliasContactEvent.Idle)
        )

    fun onEventConsumed(event: CreateAliasContactEvent) {
        detailAliasContactEventFlow.compareAndSet(event, CreateAliasContactEvent.Idle)
    }

    fun onCreate() {
        viewModelScope.launch {
            runCatching {
                createAliasContact(shareId, itemId, emailAddress)
            }.onSuccess {
                PassLogger.i(TAG, "Alias contact created")
                snackbarDispatcher(ContactCreateSuccess)
            }.onFailure {
                PassLogger.w(TAG, "Alias contact creation failed")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ContactCreateError)
            }
        }
    }

    companion object {
        private const val TAG = "CreateAliasContactViewModel"
    }
}

sealed interface CreateAliasContactEvent {
    data object Idle : CreateAliasContactEvent
}
