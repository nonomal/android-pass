package me.proton.pass.presentation.home

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.android.pass.ui.shared.HamburgerIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IdleHomeTopBar(
    modifier: Modifier = Modifier,
    homeFilter: HomeFilterMode,
    startSearchMode: () -> Unit,
    onDrawerIconClick: () -> Unit,
    onMoreOptionsClick: () -> Unit
) {
    val title = when (homeFilter) {
        HomeFilterMode.AllItems -> R.string.title_all_items
        HomeFilterMode.Logins -> R.string.title_all_logins
        HomeFilterMode.Aliases -> R.string.title_all_aliases
        HomeFilterMode.Notes -> R.string.title_all_notes
    }
    ProtonTopAppBar(
        modifier = modifier,
        title = {
            TopBarTitleView(title = stringResource(id = title))
        },
        navigationIcon = {
            HamburgerIcon(onClick = onDrawerIconClick)
        },
        actions = {
            IconButton(onClick = {
                startSearchMode()
            }) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_magnifier),
                    contentDescription = stringResource(me.proton.pass.presentation.R.string.action_search),
                    tint = ProtonTheme.colors.iconNorm
                )
            }
            IconButton(onClick = onMoreOptionsClick) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        }
    )
}
