package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm

@Composable
fun SimpleNoteSection(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .padding(start = 0.dp, top = 16.dp, end = 4.dp, bottom = 16.dp),
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_note_title)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_note_hint)) },
        editable = enabled,
        value = value,
        onChange = onChange,
        singleLine = false,
        moveToNextOnEnter = false,
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_note),
                contentDescription = null,
                tint = PassTheme.colors.textWeak
            )
        },
        trailingIcon = if (value.isNotBlank() && enabled) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}

class ThemedNoteInputPreviewProvider :
    ThemePairPreviewProvider<NoteInputPreviewParameter>(NoteInputPreviewProvider())

@Preview
@Composable
fun SimpleNoteInputPreview(
    @PreviewParameter(ThemedNoteInputPreviewProvider::class) input: Pair<Boolean, NoteInputPreviewParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SimpleNoteSection(
                value = input.second.value,
                enabled = input.second.enabled,
                onChange = {}
            )
        }
    }
}
