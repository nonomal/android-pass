package me.proton.pass.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.pass.data.api.PasswordManagerApi
import me.proton.pass.data.requests.UpdateAliasMailboxesRequest
import me.proton.pass.data.responses.AliasDetails
import me.proton.pass.data.responses.AliasOptionsResponse
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteAliasDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteAliasDataSource {
    override fun getAliasOptions(
        userId: UserId,
        shareId: ShareId
    ): Flow<AliasOptionsResponse> = flow {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { getAliasOptions(shareId.id) }
            .valueOrThrow
            .options
        emit(res)
    }

    override fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails> = flow {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { getAliasDetails(shareId.id, itemId.id) }
            .valueOrThrow
            .alias
        emit(res)
    }

    override fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: UpdateAliasMailboxesRequest
    ): Flow<AliasDetails> = flow {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke { updateAliasMailboxes(shareId.id, itemId.id, mailboxes) }
            .valueOrThrow
            .alias
        emit(res)
    }
}
