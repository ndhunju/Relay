package com.ndhunju.relay.ui.parent.messagesfromchild

import androidx.compose.runtime.Composable
import com.ndhunju.relay.ui.MainContent
import com.ndhunju.relay.ui.custom.LaunchedEffectOnce

@Composable
fun MessagesFromChildScreen(
    viewModel: MessagesFromChildViewModel,
    onUpPressed: (() -> Unit)? = null
) {
    LaunchedEffectOnce { viewModel.getLastSmsInfoOfEachChild() }
    MainContent(
        title = viewModel.title,
        isRefreshing = viewModel.isRefresh,
        showProgress = viewModel.showProgress,
        showSearchTextField = viewModel.showSearchTextField,
        lastMessageList = viewModel.lastMessageForEachThread,
        onRefreshByUser = viewModel.onRefreshByUser,
        onClickSearchIcon = viewModel.onClickSearchIcon,
        onSearchTextChanged = viewModel.onSearchTextChanged,
        onClickGrantPermission = {},
        onClickMessage = viewModel.onClickMessage,
        onClickMenuOrUpIcon = onUpPressed
    )
}