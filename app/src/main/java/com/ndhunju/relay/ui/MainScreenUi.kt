package com.ndhunju.relay.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ndhunju.relay.R
import com.ndhunju.relay.ui.custom.CenteredMessageWithButton
import com.ndhunju.relay.ui.custom.CriticalMessageBar
import com.ndhunju.relay.ui.custom.ScrollToTopLaunchedEffect
import com.ndhunju.relay.ui.custom.SearchTextField
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.messages.ThreadListItem
import com.ndhunju.relay.util.composibles.DynamicLauncherIconImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Preview
@Composable
fun MainContentPreview() {
    val dummyMessages = remember { mutableStateListOf<Message>().apply { addAll(fakeMessages) } }
    MainContent(
        lastMessageList = dummyMessages,
        showErrorMessageForPermissionDenied = MutableStateFlow(false).collectAsState()
    )
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
                        viewModel?.doOpenDebugScreen?.invoke()
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
    ModalDrawerSheet {
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
        viewModel?.isRefresh,
        viewModel?.showProgress,
        viewModel?.showUpIcon,
        viewModel?.showSearchTextField,
        viewModel?.showErrorMessageForPermissionDenied,
        viewModel?.lastMessageForEachThread,
        viewModel?.onRefreshByUser,
        viewModel?.onClickSearchIcon,
        viewModel?.onSearchTextChanged,
        viewModel?.onClickGrantPermission,
        viewModel?.onClickMessage,
        onClickMenuOrUpIcon
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainContent(
    title: State<String>? = null,
    isRefreshing: State<Boolean>? = null,
    showProgress: State<Boolean>? = null,
    showUpIcon: State<Boolean>? = mutableStateOf(true),
    showSearchTextField: State<Boolean>? = null,
    showErrorMessageForPermissionDenied: State<Boolean>? = null,
    lastMessageList: SnapshotStateList<Message>? = null,
    onRefreshByUser: (() -> Unit)? = null,
    onClickSearchIcon: (() -> Unit)? = null,
    onSearchTextChanged: ((String) -> Unit)? = null,
    onClickGrantPermission: (() -> Unit)? = null,
    onClickMessage: ((Message) -> Unit)? = null,
    onClickMenuOrUpIcon: (() -> Unit)? = null
) {
    val composeCoroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    // Use derivedStateOf to avoid recomposition everytime state changes.
    // That is everytime [state.firstVisibleItemIndex] changes instead
    // of the specific condition we are interested in.
    val showScrollToTopButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    ScrollToTopLaunchedEffect(lastMessageList, listState)

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
                        listState.animateScrollToItem(0, 0)
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
        AnimatedVisibility(
            visible = showErrorMessageForPermissionDenied?.value == true,
            exit = fadeOut()
        ) {
            CenteredMessageWithButton(
                modifier = Modifier.padding(innerPadding),
                message = stringResource(id = R.string.permission_rationale_sms_read_send),
                buttonText = stringResource(R.string.grant_permissions),
                onClickButton = onClickGrantPermission
            )
        }

        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing?.value ?: false,
            onRefresh = { onRefreshByUser?.invoke() }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            AnimatedVisibility(
                visible = !(showErrorMessageForPermissionDenied?.value ?: false),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ThreadList(
                    Modifier.padding(innerPadding),
                    listState,
                    lastMessageList,
                    onClickMessage
                )
            }

            if (showProgress?.value == true) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }

            PullRefreshIndicator(
                isRefreshing?.value ?: false,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
private fun ThreadList(
    modifier: Modifier,
    lazyListState: LazyListState,
    lastMessageList: SnapshotStateList<Message>?,
    onClickMessage: ((Message) -> Unit)?
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("threadList")
            // Add this to use threadList as id for UiAutomation
            // Search for "threadList" to find where
            .semantics { testTagsAsResourceId = true },
        state = lazyListState,
        content = {
            itemsIndexed(
                lastMessageList ?: emptyList(),
                // Pass key for better performance like setHasStableIds
                key = { _, item -> item.threadId },
            ) { _: Int, message: Message ->
                ThreadListItem(
                    Modifier.animateItemPlacement(tween(durationMillis = 250)),
                    message,
                    onClickMessage
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenAppBar(
    title: State<String>? = mutableStateOf(""),
    showUpIcon: State<Boolean>? = mutableStateOf(false),
    showSearchTextField: State<Boolean>? = mutableStateOf(false),
    onClickSearchIcon: (() -> Unit)? = null,
    onSearchTextChanged: ((String) -> Unit)? = null,
    onClickMenuOrUpIcon: (() -> Unit)? = null,
) {
    Column {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top),
            navigationIcon = {
                if (showUpIcon?.value == true) {
                    IconButton(onClick = onClickMenuOrUpIcon ?: {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showSearchTextField?.value == true) {
                        SearchTextField(onSearchTextChanged = onSearchTextChanged)
                    } else {
                        Text(text = title?.value ?: "")
                    }
                }
            },
            // Don't show search icon until we implement the searching feature
            //actions = {
            //    IconButton(onClick = onClickSearchIcon ?: {}) {
            //        if (showSearchTextField?.value != true) {
            //            Icon(
            //                imageVector = Icons.Rounded.Search,
            //                contentDescription = stringResource(id = R.string.image_description_search)
            //            )
            //        } else {
            //            Icon(
            //                imageVector = Icons.Rounded.Close,
            //                contentDescription = stringResource(id = R.string.image_description_go_back)
            //            )
            //        }
            //    }
            //}
        )
        CriticalMessageBar()
    }
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

