package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateItem
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class CreateItemImpl @Inject constructor(
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository
) : CreateItem {

    override suspend operator fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemContents: ItemContents
    ): LoadingResult<Item> = when (val shareResult = shareRepository.getById(userId, shareId)) {
        is LoadingResult.Error -> LoadingResult.Error(shareResult.exception)
        LoadingResult.Loading -> LoadingResult.Loading
        is LoadingResult.Success -> {
            val share: Share? = shareResult.data
            if (share != null) {
                itemRepository.createItem(userId, share, itemContents)
            } else {
                LoadingResult.Error(IllegalStateException("CreateItem has invalid share"))
            }
        }
    }
}

