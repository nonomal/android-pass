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

package proton.android.pass.featureitemcreate.impl.note

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import java.net.URI

abstract class BaseNoteViewModel(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val uploadAttachment: UploadAttachment,
    private val draftAttachmentRepository: DraftAttachmentRepository,
    metadataResolver: MetadataResolver,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    init {
        draftAttachmentRepository.observeAll()
            .scan<Set<URI>, Set<URI>>(emptySet()) { previousUris, currentUris ->
                currentUris - previousUris
            }
            .onEach { newUris: Set<URI> ->
                newUris.forEach { uri ->
                    viewModelScope.launch {
                        isUploadingAttachment.update { it + uri }
                        runCatching { uploadAttachment(uri) }
                            .onError {
                                PassLogger.w(TAG, "Could not upload attachment: $uri")
                                PassLogger.w(TAG, it)
                            }
                        isUploadingAttachment.update { it - uri }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var noteItemFormMutableState: NoteItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(NoteItemFormState.Empty) }
    val noteItemFormState: NoteItemFormState get() = noteItemFormMutableState

    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isUploadingAttachment: MutableStateFlow<Set<URI>> = MutableStateFlow(emptySet())
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<NoteItemValidationErrors>> =
        MutableStateFlow(emptySet())
    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val draftAttachments = draftAttachmentRepository.observeAll()
        .map { uris -> uris.mapNotNull { metadataResolver.extractMetadata(it) } }

    private val attachmentsFlow = combine(
        isUploadingAttachment,
        draftAttachments
    ) { loadingAttachments, draftAttachmentsList ->
        AttachmentsUiState(
            loadingAttachments = loadingAttachments,
            draftAttachmentsList = draftAttachmentsList,
            attachmentsList = emptyList()
        )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val baseNoteUiState: StateFlow<BaseNoteUiState> = combineN(
        noteItemValidationErrorsState,
        isLoadingState,
        isItemSavedState,
        hasUserEditedContentFlow,
        attachmentsFlow,
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1)
    ) { noteItemValidationErrors, isLoading, isItemSaved, hasUserEditedContent, attachmentsState,
        isFileAttachmentsEnabled ->
        BaseNoteUiState(
            errorList = noteItemValidationErrors,
            isLoadingState = isLoading,
            itemSavedState = isItemSaved,
            hasUserEditedContent = hasUserEditedContent,
            attachmentsUiState = attachmentsState,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseNoteUiState.Initial
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        noteItemFormMutableState = noteItemFormMutableState.copy(title = value)
        noteItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(NoteItemValidationErrors.BlankTitle) }
        }
    }

    fun onNoteChange(value: String) {
        onUserEditedContent()
        noteItemFormMutableState = noteItemFormMutableState.copy(note = value)
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    fun onEmitSnackbarMessage(snackbarMessage: NoteSnackbarMessage) = viewModelScope.launch {
        snackbarDispatcher(snackbarMessage)
    }

    override fun onCleared() {
        super.onCleared()
        draftAttachmentRepository.clear()
    }

    companion object {
        private const val TAG = "BaseNoteViewModel"
    }
}
