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

package proton.android.pass.featureauth.impl

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.form.ProtonTextField

@Composable
fun PinInput(
    modifier: Modifier = Modifier,
    state: EnterPinUiState,
    onPinChanged: (String) -> Unit,
    onPinSubmit: () -> Unit
) {
    val data = state as? EnterPinUiState.Data
    val error = data?.pinError?.value()
    val errorMessage = when (error) {
        is PinError.PinEmpty -> stringResource(R.string.auth_error_pin_cannot_be_empty)
        else -> ""
    }
    val focusRequester = remember { FocusRequester() }
    ProtonTextField(
        modifier = modifier.focusRequester(focusRequester),
        editable = data?.isLoadingState?.value() != true,
        value = (state as? EnterPinUiState.Data)?.pin.orEmpty(),
        textStyle = PassTypography.hero.copy(textAlign = TextAlign.Center),
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        isError = error is PinError.PinEmpty,
        errorMessage = errorMessage,
        visualTransformation = PasswordVisualTransformation(),
        onChange = onPinChanged,
        onDoneClick = onPinSubmit
    )
    RequestFocusLaunchedEffect(focusRequester)
}
