package me.proton.pass.presentation.create.alias

import androidx.compose.runtime.Immutable
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.AliasSuffix

@Immutable
data class AliasItem(
    val title: String = "",
    val alias: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptions = AliasOptions(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffix? = null,
    val mailboxes: List<AliasMailboxUiModel> = emptyList(),
    val isMailboxListApplicable: Boolean = false,
    val aliasToBeCreated: String? = null
) {

    fun validate(): Set<AliasItemValidationErrors> {
        val mutableSet = mutableSetOf<AliasItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankTitle)

        if (alias.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankAlias)

        if (alias.startsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (alias.endsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (alias.contains("..")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (!areAllAliasCharactersValid()) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (mailboxes.count { it.selected } == 0) mutableSet.add(AliasItemValidationErrors.NoMailboxes)

        return mutableSet.toSet()
    }

    private fun areAllAliasCharactersValid(): Boolean {
        for (char in alias) {
            // If it's not a letter or a digit, check if it's one of the allowed symbols
            if (!char.isLetterOrDigit() && !ALLOWED_SPECIAL_CHARACTERS.contains(char)) return false

            // If it's a letter, must be lowercase
            if (char.isLetter() && char.isUpperCase()) return false
        }
        return true
    }


    companion object {
        val Empty = AliasItem()
        private val ALLOWED_SPECIAL_CHARACTERS: List<Char> = listOf('_', '-', '.')
    }
}

sealed interface AliasItemValidationErrors {
    object BlankTitle : AliasItemValidationErrors
    object BlankAlias : AliasItemValidationErrors
    object InvalidAliasContent : AliasItemValidationErrors
    object NoMailboxes : AliasItemValidationErrors
}
