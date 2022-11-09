package me.proton.pass.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.biometry.BiometryAuthError
import me.proton.android.pass.biometry.BiometryManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.HasAuthenticated
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.autofill.AutofillManager
import me.proton.pass.domain.autofill.AutofillSupportedStatus
import me.proton.pass.presentation.uievents.IsButtonEnabled
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferenceRepository,
    private val biometryManager: BiometryManager,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val autofillManager: AutofillManager
) : ViewModel() {

    private val biometricLockState: Flow<BiometricLockState> = preferencesRepository
        .getBiometricLockState()
        .asResultWithoutLoading()
        .map { getFingerprintSection(it) }
        .distinctUntilChanged()

    private val themeState: Flow<ThemePreference> = preferencesRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getTheme(it) }
        .distinctUntilChanged()

    private val autofillState: Flow<AutofillSupportedStatus> = autofillManager
        .getAutofillStatus()
        .distinctUntilChanged()

    val state: StateFlow<SettingsUiState> = combine(
        biometricLockState,
        themeState,
        autofillState
    ) { biometricLock, theme, autofill ->
        val fingerprintSection = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.NotEnrolled -> FingerprintSectionState.NoFingerprintRegistered
            BiometryStatus.NotAvailable -> FingerprintSectionState.NotAvailable
            BiometryStatus.CanAuthenticate -> {
                val available = when (biometricLock) {
                    BiometricLockState.Enabled -> IsButtonEnabled.Enabled
                    BiometricLockState.Disabled -> IsButtonEnabled.Disabled
                }
                FingerprintSectionState.Available(available)
            }
        }
        SettingsUiState(
            fingerprintSection = fingerprintSection,
            themePreference = theme,
            autofillStatus = autofill
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SettingsUiState.Initial
    )

    fun onFingerPrintLockChange(contextHolder: ContextHolder, state: IsButtonEnabled) =
        viewModelScope.launch {
            biometryManager.launch(contextHolder)
                .map { result ->
                    when (result) {
                        BiometryResult.Success -> {
                            preferencesRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
                                .asResultWithoutLoading()
                                .collect { prefResult ->
                                    prefResult.onError {
                                        val message = "Could not save HasAuthenticated preference"
                                        PassLogger.e(TAG, it ?: RuntimeException(message))
                                    }
                                }
                            performFingerprintLockChange(state)
                        }
                        is BiometryResult.Error -> {
                            when (result.cause) {
                                // If the user has cancelled it, do nothing
                                BiometryAuthError.Canceled -> {}
                                BiometryAuthError.UserCanceled -> {}

                                else ->
                                    snackbarMessageRepository
                                        .emitSnackbarMessage(SettingsSnackbarMessage.BiometryFailedToAuthenticateError)
                            }
                        }

                        // User can retry
                        BiometryResult.Failed -> {}
                        is BiometryResult.FailedToStart ->
                            snackbarMessageRepository
                                .emitSnackbarMessage(SettingsSnackbarMessage.BiometryFailedToStartError)
                    }
                    PassLogger.i(TAG, "Biometry result: $result")
                }
                .collect { }
        }

    fun onThemePreferenceChange(theme: ThemePreference) = viewModelScope.launch {
        PassLogger.d(TAG, "Changing theme to $theme")
        preferencesRepository.setThemePreference(theme).asResultWithoutLoading().first()
            .onError {
                val errMessage = "Error setting ThemePreference"
                PassLogger.e(TAG, it ?: Exception(errMessage), errMessage)
                snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingOperation)
            }
    }

    fun onToggleAutofill(value: Boolean) {
        if (value) {
            autofillManager.openAutofillSelector()
        } else {
            autofillManager.disableAutofill()
        }
    }

    private suspend fun performFingerprintLockChange(state: IsButtonEnabled) {
        val (lockState, message) = when (state) {
            IsButtonEnabled.Enabled -> BiometricLockState.Enabled to SettingsSnackbarMessage.FingerprintLockEnabled
            IsButtonEnabled.Disabled -> BiometricLockState.Disabled to SettingsSnackbarMessage.FingerprintLockDisabled
        }

        PassLogger.d(TAG, "Changing BiometricLock to $lockState")
        preferencesRepository.setBiometricLockState(lockState).asResultWithoutLoading().first()
            .onSuccess {
                snackbarMessageRepository.emitSnackbarMessage(message)
            }
            .onError {
                val errMessage = "Error setting BiometricLockState"
                PassLogger.e(TAG, it ?: Exception(errMessage), errMessage)
                snackbarMessageRepository.emitSnackbarMessage(SettingsSnackbarMessage.ErrorPerformingOperation)
            }
    }

    private fun getFingerprintSection(biometricLock: Result<BiometricLockState>): BiometricLockState =
        when (biometricLock) {
            Result.Loading -> BiometricLockState.Disabled
            is Result.Success -> biometricLock.data
            is Result.Error -> {
                val message = "Error getting BiometricLock preference"
                PassLogger.e(TAG, biometricLock.exception ?: Exception(message), message)
                BiometricLockState.Disabled
            }
        }

    private fun getTheme(theme: Result<ThemePreference>): ThemePreference =
        when (theme) {
            Result.Loading -> ThemePreference.System
            is Result.Success -> theme.data
            is Result.Error -> {
                val message = "Error getting ThemePreference"
                PassLogger.e(TAG, theme.exception ?: Exception(message), message)
                ThemePreference.System
            }
        }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
