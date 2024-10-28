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

package proton.android.pass.domain.organizations

data class OrganizationPasswordPolicy(
    val randomPasswordAllowed: Boolean?,
    val randomPasswordMinLength: Int?,
    val randomPasswordMaxLength: Int?,
    val randomPasswordIncludeNumbers: Boolean?,
    val randomPasswordIncludeSymbols: Boolean?,
    val randomPasswordIncludeUppercase: Boolean?,
    val memorablePasswordAllowed: Boolean?,
    val memorablePasswordMinWords: Int?,
    val memorablePasswordMaxWords: Int?,
    val memorablePasswordCapitalize: Boolean?,
    val memorablePasswordIncludeNumbers: Boolean?
) {

    val canToggleRandomPasswordSymbols: Boolean = randomPasswordIncludeSymbols == null

    val canToggleRandomPasswordNumbers: Boolean = randomPasswordIncludeNumbers == null

    val canToggleRandomPasswordUppercase: Boolean = randomPasswordIncludeUppercase == null

    val canToggleMemorablePasswordNumbers: Boolean = memorablePasswordIncludeNumbers == null

    val canToggleMemorablePasswordCapitalize: Boolean = memorablePasswordCapitalize == null

}
