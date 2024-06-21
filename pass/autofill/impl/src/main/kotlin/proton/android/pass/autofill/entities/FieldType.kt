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

package proton.android.pass.autofill.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FieldType : Parcelable {
    Username,
    Email,
    Password,
    Totp,
    Other,
    Unknown,

    SubmitButton,

    FullName,

    // Credit Card
    CardNumber,
    CardholderFirstName,
    CardholderLastName,
    CardExpirationMMYY,
    CardExpirationMM,
    CardExpirationYY,
    CardExpirationYYYY,
    CardCvv,

    // Identity
    Address,
    PostalCode,
    Phone
    ;

    fun isCreditCardField(): Boolean = when (this) {
        CardNumber,
        CardholderFirstName,
        CardholderLastName,
        CardExpirationMMYY,
        CardExpirationMM,
        CardExpirationYY,
        CardExpirationYYYY,
        CardCvv -> true
        else -> false
    }

    fun isIdentityField(): Boolean = when (this) {
        Address,
        PostalCode,
        Phone -> true
        else -> false
    }

    companion object {
        fun from(value: String): FieldType = try {
            FieldType.valueOf(value)
        } catch (_: Exception) {
            Unknown
        }
    }
}
