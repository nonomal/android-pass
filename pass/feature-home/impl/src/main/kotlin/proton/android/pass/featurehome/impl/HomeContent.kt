package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
import proton.android.pass.composecomponents.impl.dialogs.ConfirmItemDeletionDialog
import proton.android.pass.composecomponents.impl.icon.AllVaultsIcon
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.ArrowBackIconButton
import proton.android.pass.featurehome.impl.icon.ActiveVaultIcon
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTips
import proton.pass.domain.ShareId

@Suppress("LongParameterList")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    shouldScrollToTop: Boolean,
    homeScreenNavigation: HomeScreenNavigation,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearch: () -> Unit,
    sendItemToTrash: (ItemUiModel) -> Unit,
    onDrawerIconClick: () -> Unit,
    onSortingOptionsClick: () -> Unit,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onScrollToTop: () -> Unit,
    onProfileClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                placeholderText = stringResource(R.string.search_topbar_placeholder_all_vaults),
                onEnterSearch = onEnterSearch,
                onStopSearch = onStopSearch,
                onSearchQueryChange = onSearchQueryChange,
                drawerIcon = {
                    if (!uiState.searchUiState.inSearchMode) {
                        if (uiState.homeListUiState.selectedShare is None) {
                            AllVaultsIcon(
                                onClick = onDrawerIconClick
                            )
                        } else {
                            ActiveVaultIcon(
                                modifier = Modifier.size(48.dp),
                                onClick = onDrawerIconClick
                            )
                        }
                    } else {
                        ArrowBackIconButton { onStopSearch() }
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Home,
                onListClick = {},
                onCreateClick = { onAddItemClick(uiState.homeListUiState.selectedShare) },
                onProfileClick = onProfileClick
            )
        }
    ) { contentPadding ->
        var itemToDelete by rememberSaveable(stateSaver = ItemUiModelSaver) {
            mutableStateOf(null)
        }
        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            ItemTypeFilterList(
                selected = HomeItemTypeSelection.AllItems,
                loginCount = 1,
                aliasCount = 2,
                noteCount = 3,
                onItemTypeClick = {}
            )
            ItemListHeader(
                sortingType = uiState.homeListUiState.sortingType,
                showSearchResults = uiState.searchUiState.inSearchMode &&
                    uiState.searchUiState.searchQuery.isNotEmpty(),
                isProcessingSearch = uiState.searchUiState.isProcessingSearch.value(),
                itemCount = uiState.homeListUiState.items.values.flatten().count()
                    .takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                onSortingOptionsClick = onSortingOptionsClick
            )
            ItemsList(
                items = uiState.homeListUiState.items,
                shouldScrollToTop = shouldScrollToTop,
                highlight = uiState.searchUiState.searchQuery,
                onItemClick = { item ->
                    keyboardController?.hide()
                    homeScreenNavigation.toItemDetail(item.shareId, item.id)
                },
                onItemMenuClick = onItemMenuClick,
                isLoading = uiState.homeListUiState.isLoading,
                isProcessingSearch = uiState.searchUiState.isProcessingSearch,
                isRefreshing = uiState.homeListUiState.isRefreshing,
                onRefresh = onRefresh,
                onScrollToTop = onScrollToTop,
                emptyContent = {
                    if (uiState.searchUiState.inSearchMode) {
                        EmptySearchResults()
                    } else {
                        HomeEmptyList(
                            onCreateItemClick = {
                                onAddItemClick(uiState.homeListUiState.selectedShare)
                            }
                        )
                    }
                },
                header = { item { OnBoardingTips() } },
                footer = { item { Spacer(Modifier.height(64.dp)) } }
            )
        }
        ConfirmItemDeletionDialog(
            state = itemToDelete,
            onDismiss = { itemToDelete = null },
            title = R.string.alert_confirm_item_send_to_trash_title,
            message = R.string.alert_confirm_item_send_to_trash_message,
            onConfirm = sendItemToTrash
        )
    }
}
