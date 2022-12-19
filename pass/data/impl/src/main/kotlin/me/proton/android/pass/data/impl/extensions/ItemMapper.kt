package me.proton.android.pass.data.impl.extensions

import me.proton.android.pass.data.api.crypto.EncryptionContext
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.pass.domain.ItemType
import proton_pass_item_v1.ItemV1

@Suppress("TooGenericExceptionThrown")
fun ItemType.Companion.fromParsed(
    context: EncryptionContext,
    parsed: ItemV1.Item,
    aliasEmail: String? = null
): ItemType {
    return when (parsed.content.contentCase) {
        ItemV1.Content.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.content.login.username,
            password = context.encrypt(parsed.content.login.password),
            websites = parsed.content.login.urlsList
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> {
            requireNotNull(aliasEmail)
            ItemType.Alias(aliasEmail = aliasEmail)
        }
        else -> throw Exception("Unknown ItemType")
    }
}

fun ItemEntity.itemType(context: EncryptionContext): ItemType {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)
    return ItemType.fromParsed(context, parsed, aliasEmail)
}

fun ItemEntity.allowedApps(context: EncryptionContext): List<String> {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)
    return parsed.platformSpecific.android.allowedAppsList.map { it.packageName }
}
