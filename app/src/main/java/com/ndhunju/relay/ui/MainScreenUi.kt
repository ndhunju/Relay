package com.ndhunju.relay.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.CenteredMessageWithButton
import com.ndhunju.relay.ui.custom.SearchTextField
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messages.MessageListItem
import com.ndhunju.relay.util.composibles.DynamicLauncherIconImage
import kotlinx.coroutines.launch

@Preview
@Composable
fun MainContentPreview() {
    val mockedMessages = remember { mutableStateListOf<Message>().apply { addAll(mockMessages) } }
    MainContent(lastMessageList = mockedMessages)
}

@Composable
fun MainScreen(viewModel: MainViewModel?) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var onClickLauncherIconCount = remember { 0 }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainDrawerContent(
                navigationItems = navigationItems,
                onClickNavItem = {
                    viewModel?.onClickNavItem?.invoke(it)
                    coroutineScope.launch { drawerState.close() }
                },
                onClickLauncherIcon = {
                    onClickLauncherIconCount++
                    if (onClickLauncherIconCount > 3) {
                        onClickLauncherIconCount = 0
                        viewModel?.doOpenDebugFragment?.invoke()
                        coroutineScope.launch { drawerState.close() }
                    }
                }
            )

        }
    ) {
        MainContent(
            viewModel = viewModel,
            onClickMenuOrUpIcon = { coroutineScope.launch { drawerState.open() } }
        )
    }
}

@Composable
fun MainDrawerContent(
    navigationItems: List<NavItem>,
    onClickNavItem: (NavItem) -> Unit,
    onClickLauncherIcon: () -> Unit,
) {
    ModalDrawerSheet(modifier = Modifier
        .fillMaxWidth(0.7f)
    ) {
        // Show big app icon
        DynamicLauncherIconImage(modifier = Modifier
            .size(112.dp)
            .align(Alignment.CenterHorizontally)
            .clickable { onClickLauncherIcon() }
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

@Composable
fun MainContent(
    viewModel: MainViewModel? = null,
    onClickMenuOrUpIcon: () -> Unit
) {
    MainContent(
        viewModel?.title,
        viewModel?.showUpIcon,
        viewModel?.showSearchTextField,
        viewModel?.showErrorMessageForPermissionDenied,
        viewModel?.lastMessageForEachThread,
        viewModel?.onClickSearchIcon,
        viewModel?.onSearchTextChanged,
        viewModel?.onClickGrantPermission,
        viewModel?.onClickMessage,
        onClickMenuOrUpIcon
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    title: State<String>? = null,
    showUpIcon: State<Boolean>? = null,
    showSearchTextField: State<Boolean>? = null,
    showErrorMessageForPermissionDenied: State<Boolean>? = null,
    lastMessageList: SnapshotStateList<Message>? = null,
    onClickSearchIcon: (() -> Unit)? = null,
    onSearchTextChanged: ((String) -> Unit)? = null,
    onClickGrantPermission: (() -> Unit)? = null,
    onClickMessage: ((Message) -> Unit)? = null,
    onClickMenuOrUpIcon: (() -> Unit)? = null
) {
    val composeCoroutineScope = rememberCoroutineScope()
    val state = rememberLazyListState()
    // Use derivedStateOf to avoid recomposition everytime state changes.
    // That is everytime [state.firstVisibleItemIndex] changes instead
    // of the specific condition we are interested in.
    val showScrollToTopButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
    Scaffold(
        topBar = {
            MainScreenAppBar(
                title,
                showUpIcon,
                showSearchTextField,
                // Put all callbacks inside lambda so that recomposition
                // is not triggered when reference to those callback changes?
                { onClickSearchIcon?.invoke() },
                { onSearchTextChanged?.invoke(it) },
                onClickMenuOrUpIcon
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
                            R.string.image_description_scroll_to_top
                        )
                    )
                }
            }
        }

    ) { innerPadding ->
        if (showErrorMessageForPermissionDenied?.value == true) {
            CenteredMessageWithButton(
                modifier = Modifier.padding(innerPadding),
                message = stringResource(id = R.string.permission_rationale_sms_read_send),
                buttonText = stringResource(R.string.grant_permissions),
                onClickButton = onClickGrantPermission
            )


        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                state = state,
                content = {
                    itemsIndexed(
                        lastMessageList?.toList() ?: emptyList(),
                        // Pass key for better performance like setHasStableIds
                        key = { _, item -> item.threadId },
                    ) { _: Int, message: Message ->
                        MessageListItem(
                            Modifier.animateItemPlacement(tween(durationMillis = 250)),
                            message,
                            onClickMessage
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAppBar(
    title: State<String>? = mutableStateOf(""),
    showUpIcon: State<Boolean>? = mutableStateOf(false),
    showSearchTextField: State<Boolean>? = mutableStateOf(false),
    onClickSearchIcon: (() -> Unit)? = {},
    onSearchTextChanged: ((String) -> Unit)? = {},
    onClickMenuOrUpIcon: (() -> Unit)? = {}
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        navigationIcon = {
            if (showUpIcon?.value == true) {
                IconButton(onClick = onClickMenuOrUpIcon ?: {}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.image_description_go_back)
                    )
                }
            } else {
                IconButton(onClick = onClickMenuOrUpIcon ?: {}) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(
                            androidx.compose.ui.R.string.navigation_menu
                        )
                    )
                }
            }
        },
        title = {
            Row (verticalAlignment = Alignment.CenterVertically) {
                if (showSearchTextField?.value == true) {
                    SearchTextField(onSearchTextChanged = onSearchTextChanged)
                } else {
                    Text(text = title?.value ?: "")
                }
            }
        },
        actions = {
            IconButton(onClick = onClickSearchIcon ?: {} ) {
                if (showSearchTextField?.value != true) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.image_description_search)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(id = R.string.image_description_go_back)
                    )
                }
            }
        }
    )
}

val navigationItems = listOf(
    NavItem.AccountNavItem,
    NavItem.PairWithParentNavItem,
    //NavItem.PairWithChildNavItem,
    NavItem.ChildUsersNavItem,
    NavItem.EncryptionKeyNavItem
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
        R.string.nav_item_pair_parent,
        R.string.image_description_account
    )

    data object PairWithParentNavItem: NavItem(
        R.drawable.baseline_pair_parent_24,
        R.string.nav_item_pair_parent,
        R.string.image_description_pair_parent
    )

    data object PairWithChildNavItem: NavItem(
        R.drawable.baseline_pair_parent_24,
        R.string.nav_item_pair_child,
        R.string.image_description_pair_child
    )

    data object ChildUsersNavItem: NavItem(
        R.drawable.baseline_child_users_24,
        R.string.nav_item_child_user,
        R.string.nav_item_child_user
    )

    data object EncryptionKeyNavItem: NavItem(
        R.drawable.baseline_key_24,
        R.string.nav_item_enc_key,
        R.string.nav_item_enc_key
    )
}

