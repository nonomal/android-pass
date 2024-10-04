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

package proton.android.pass.data.impl.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import proton.android.pass.data.impl.db.entities.AssetLinkEntity
import proton.android.pass.data.impl.responses.AssetLinkResponse
import proton.android.pass.domain.assetlink.AssetLink
import java.util.Date


fun AssetLinkEntity.toDomain(): AssetLink = AssetLink(
    website = website,
    packageNames = packageNames
)

fun AssetLink.toEntity(): AssetLinkEntity = AssetLinkEntity(
    website = website,
    packageNames = packageNames,
    createdAt = Date.from(Clock.System.now().toJavaInstant())
)

fun List<AssetLinkResponse>.toDomain(url: String): AssetLink = AssetLink(
    website = url,
    packageNames = flatMap { listOfNotNull(it.target.packageName) }
)