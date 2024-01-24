package com.ndhunju.relay.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.SearchTextField
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messages.MessageListItem
import com.ndhunju.relay.ui.theme.LocalDimens
import kotlinx.coroutines.launch

@Preview
@Composable
fun MainScreenPreview() {
    // Fixme: Preview
    //MainScreen(viewModel = MainViewModel())
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { MainDrawerContent(navigationItems) {
            viewModel.onClickNavItem(it)
            coroutineScope.launch { drawerState.close() }
        }}
    ) {
        MainContent(viewModel = viewModel, onClickMenuIcon = {
            coroutineScope.launch { drawerState.open() }
        })
    }
}

@Composable
fun MainDrawerContent(
    navigationItems: List<NavItem>,
    onClickNavItem: (NavItem) -> Unit
) {
    ModalDrawerSheet(modifier = Modifier
        .fillMaxWidth(0.7f)
    ) {
        // Show big app icon
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            contentDescription = stringResource(id = R.string.image_description_app_logo),
            modifier = Modifier
                .size(112.dp)
                .align(Alignment.CenterHorizontally)
        )

        Divider()

        // Show each items in navigationItems
        navigationItems.forEach { item ->
            NavigationDrawerItem(
                label = {
                    Row {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(id = item.drawableRes),
                                contentDescription = stringResource(item.contentDescriptionStrRes)
                            )
                        }
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = stringResource(id = item.labelStrRes)
                        )
                    }
                },
                selected = item.selected,
                onClick = { onClickNavItem(item) }
            )
            Divider()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    viewModel: MainViewModel,
    onClickMenuIcon: () -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val composeCoroutineScope = rememberCoroutineScope()
    val state = rememberLazyListState()
    // Use derivedStateOf to avoid recomposition everytime state changes.
    // That is everytime [state.firstVisibleItemIndex] changes instead
    // of the specific condition we are interested in.
    val showScrollToTopButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
    Scaffold(
        topBar = {
            MainScreenAppBar(
                viewState.showSearchTextField,
                // Put all callbacks inside lambda so that recomposition
                // is not triggered when reference to those callback changes?
                { viewModel.onClickSearchIcon() },
                { viewModel.onSearchTextChanged(it) },
                onClickMenuIcon
            )
        },
        floatingActionButton = {
            if (showScrollToTopButton) {
                FloatingActionButton(onClick = {
                    composeCoroutineScope.launch {
                        state.animateScrollToItem(0, 0)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(
                            R.string.content_description_scroll_to_top
                        )
                    )
                }
            }
        }

    ) { innerPadding ->
        if (viewState.showErrorMessageForPermissionDenied) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = LocalDimens.current.contentPaddingHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.permission_rationale_sms_read_send),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.onClickGrantPermission() },
                    modifier = Modifier.padding(16.dp)
                    ) {
                    Text(text = stringResource(R.string.grant_permissions))
                }
            }

        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                state = state,
                content = {
                    itemsIndexed(
                        viewState.lastMessageList,
                        // Pass key for better performance like setHasStableIds
                        key = { _, item -> item.threadId },
                    ) { _: Int, message: Message ->
                        MessageListItem(
                            Modifier.animateItemPlacement(tween(durationMillis = 250)),
                            message,
                            viewModel.onClickMessage
                        )
                    }
                })
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAppBar(
    showSearchTextField: Boolean = false,
    onClickSearchIcon: () -> Unit = {},
    onSearchTextChanged: (String) -> Unit = {},
    onClickMenuIcon: () -> Unit = {}
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        navigationIcon = {
            IconButton(onClick = onClickMenuIcon) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(
                        androidx.compose.ui.R.string.navigation_menu
                    )
                )
            }
        },
        title = {
            Row (verticalAlignment = Alignment.CenterVertically) {
                if (showSearchTextField) {
                    SearchTextField(onSearchTextChanged = onSearchTextChanged)
                } else {
                    Text(text = stringResource(id = R.string.app_name))
                }
            }
        },
        actions = {
            IconButton(onClick = onClickSearchIcon ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(id = R.string.image_description_search)
                )
            }
        }
    )
}

val navigationItems = listOf(
    NavItem.AccountNavItem,
    NavItem.PairNavItem
)

/**
 * Date class that represents items in side navigation drawer
 */
sealed class NavItem(
    val drawableRes: Int,
    val contentDescriptionStrRes: Int,
    val labelStrRes: Int,
    val selected: Boolean = false
) {
    data object AccountNavItem: NavItem(
        R.drawable.baseline_account_circle_24,
        R.string.nav_item_pair,
        R.string.image_description_account
    )

    data object PairNavItem: NavItem(
        R.drawable.baseline_pair_parent_24,
        R.string.nav_item_pair,
        R.string.image_description_pair
    )
}
