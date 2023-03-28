package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.AllVaultsIcon
import proton.android.pass.composecomponents.impl.icon.TrashVaultIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.ArrowBackIconButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTips
import proton.android.pass.featurehome.impl.trash.EmptyTrashContent
import proton.pass.domain.ShareId

@Suppress("LongParameterList", "ComplexMethod")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    shouldScrollToTop: Boolean,
    onItemClick: (ItemUiModel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearch: () -> Unit,
    onDrawerIconClick: () -> Unit,
    onSortingOptionsClick: () -> Unit,
    onClearRecentSearchClick: () -> Unit,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onScrollToTop: () -> Unit,
    onProfileClick: () -> Unit,
    onItemTypeSelected: (HomeItemTypeSelection) -> Unit,
    onTrashActionsClick: () -> Unit
) {
    val isTrashMode = uiState.homeListUiState.homeVaultSelection == HomeVaultSelection.Trash
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                placeholderText = when (uiState.homeListUiState.homeVaultSelection) {
                    HomeVaultSelection.AllVaults -> stringResource(R.string.search_topbar_placeholder_all_vaults)
                    HomeVaultSelection.Trash -> stringResource(R.string.search_topbar_placeholder_trash)
                    is HomeVaultSelection.Vault -> stringResource(
                        R.string.search_topbar_placeholder_vault,
                        uiState.homeListUiState.selectedShare.value()?.name ?: ""
                    )
                },
                onEnterSearch = onEnterSearch,
                onStopSearch = onStopSearch,
                onSearchQueryChange = onSearchQueryChange,
                drawerIcon = {
                    HomeDrawerIcon(
                        uiState = uiState,
                        onDrawerIconClick = onDrawerIconClick,
                        onStopSearch = onStopSearch
                    )
                },
                actions = if (isTrashMode) {
                    {
                        IconButton(onClick = onTrashActionsClick) {
                            Icon(
                                painter = painterResource(
                                    id = me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical
                                ),
                                contentDescription = null,
                                tint = PassTheme.colors.textWeak
                            )
                        }
                    }
                } else {
                    null
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Home,
                onListClick = {},
                onCreateClick = {
                    val shareId = uiState.homeListUiState.selectedShare.map { it.id }
                    onAddItemClick(shareId)
                },
                onProfileClick = onProfileClick
            )
        }
    ) { contentPadding ->
        val keyboardController = LocalSoftwareKeyboardController.current
        val scrollableState = rememberLazyListState()

        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            if (uiState.searchUiState.inSearchMode) {
                ItemTypeFilterList(
                    selected = uiState.homeListUiState.homeItemTypeSelection,
                    loginCount = uiState.searchUiState.itemTypeCount.loginCount,
                    aliasCount = uiState.searchUiState.itemTypeCount.aliasCount,
                    noteCount = uiState.searchUiState.itemTypeCount.noteCount,
                    onItemTypeClick = onItemTypeSelected
                )
            }

            if (shouldShowItemListHeader(uiState)) {
                ItemListHeader(
                    sortingType = uiState.homeListUiState.sortingType,
                    showSearchResults = uiState.searchUiState.inSearchMode &&
                        uiState.searchUiState.searchQuery.isNotEmpty(),
                    itemCount = uiState.homeListUiState.items.map { it.items }.flatten().count()
                        .takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                    onSortingOptionsClick = onSortingOptionsClick
                )
            }

            if (shouldShowRecentSearchHeader(uiState)) {
                RecentSearchListHeader(
                    itemCount = uiState.homeListUiState.items.map { it.items }.flatten().count(),
                    onClearRecentSearchClick = onClearRecentSearchClick
                )
            }

            ItemsList(
                items = uiState.homeListUiState.items,
                shouldScrollToTop = shouldScrollToTop,
                scrollableState = scrollableState,
                highlight = uiState.searchUiState.searchQuery,
                onItemClick = { item ->
                    keyboardController?.hide()
                    onItemClick(item)
                },
                onItemMenuClick = onItemMenuClick,
                isLoading = uiState.homeListUiState.isLoading,
                isProcessingSearch = uiState.searchUiState.isProcessingSearch,
                isRefreshing = uiState.homeListUiState.isRefreshing,
                onRefresh = onRefresh,
                onScrollToTop = onScrollToTop,
                emptyContent = {
                    if (isTrashMode) {
                        EmptyTrashContent()
                    } else if (uiState.searchUiState.inSearchMode) {
                        EmptySearchResults()
                    } else {
                        HomeEmptyList(
                            onCreateItemClick = {
                                val shareId = uiState.homeListUiState.selectedShare.map { it.id }
                                onAddItemClick(shareId)
                            }
                        )
                    }
                },
                header = { item { OnBoardingTips() } },
                footer = { item { Spacer(Modifier.height(64.dp)) } }
            )
        }
    }
}

private fun shouldShowRecentSearchHeader(uiState: HomeUiState) =
    uiState.homeListUiState.items.isNotEmpty() &&
        uiState.searchUiState.inSearchMode &&
        uiState.searchUiState.isInSuggestionsMode

private fun shouldShowItemListHeader(uiState: HomeUiState) =
    uiState.homeListUiState.items.isNotEmpty() &&
        uiState.homeListUiState.isLoading == IsLoadingState.NotLoading &&
        !uiState.searchUiState.isProcessingSearch.value() &&
        !uiState.searchUiState.isInSuggestionsMode

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeDrawerIcon(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onDrawerIconClick: () -> Unit,
    onStopSearch: () -> Unit
) {
    if (!uiState.searchUiState.inSearchMode) {
        when (val share = uiState.homeListUiState.selectedShare) {
            None -> {
                when (uiState.homeListUiState.homeVaultSelection) {
                    HomeVaultSelection.AllVaults -> {
                        AllVaultsIcon(
                            modifier = modifier,
                            size = 48,
                            iconSize = 28,
                            onClick = onDrawerIconClick
                        )
                    }
                    HomeVaultSelection.Trash -> {
                        TrashVaultIcon(
                            modifier = modifier,
                            size = 48,
                            iconSize = 28,
                            onClick = onDrawerIconClick
                        )
                    }
                    else -> {} // This combination is not possible
                }
            }
            is Some -> {
                VaultIcon(
                    modifier = modifier.size(48.dp),
                    backgroundColor = share.value.color.toColor(true),
                    iconColor = share.value.color.toColor(),
                    icon = share.value.icon.toResource(),
                    onClick = onDrawerIconClick
                )
            }
        }
    } else {
        ArrowBackIconButton(modifier) { onStopSearch() }
    }
}
