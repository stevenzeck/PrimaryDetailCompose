package com.example.primarydetailcompose.ui.postlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primarydetailcompose.R
import com.example.primarydetailcompose.model.Post

/**
 * The main screen that displays a list of posts.
 *
 * Handles loading, error, and success states. Supports multi-selection for deleting or marking posts as read.
 *
 * @param onPostSelected Callback triggered when a post is clicked for navigation. Passes the post ID.
 * @param viewModel The [PostListViewModel] that manages the state of this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalFoundationApi
@Composable
fun PostListScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPostSelected: (Long) -> Unit,
    viewModel: PostListViewModel = hiltViewModel(),
) {
    val listState = rememberLazyListState()
    val uiState by viewModel.postListUiState.collectAsStateWithLifecycle()
    val selectedPostIds by viewModel.selectedPostIds.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val isSelectionMode = selectedPostIds.isNotEmpty()

    // Handle system back button to clear selection if in selection mode
    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(id = R.string.delete_selected_posts)) },
            text = { Text(text = stringResource(id = R.string.confirm_delete_posts)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePosts()
                        showDeleteDialog = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.selected_items_count,
                                count = selectedPostIds.size,
                                selectedPostIds.size,
                            ),
                        )
                    } else {
                        Text(text = stringResource(id = R.string.title_post_list))
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = stringResource(id = R.string.clear_selected),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { viewModel.markRead() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.check),
                                contentDescription = stringResource(id = R.string.markRead),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = stringResource(id = R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(paddingValues = padding)

        when (val currentState = uiState) {
            is PostListUiState.Success -> {
                if (currentState.posts.isEmpty()) {
                    EmptyContentView(
                        message = stringResource(R.string.no_posts_available), modifier = modifier,
                    )
                } else {
                    PostList(
                        listState = listState,
                        posts = currentState.posts,
                        selectedPostIds = selectedPostIds,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onPostSelected = { postId ->
                            if (isSelectionMode) {
                                viewModel.toggleSelection(postId)
                            } else {
                                viewModel.markRead(postId)
                                onPostSelected(postId)
                            }
                        },
                        onPostLongPressed = { postId ->
                            viewModel.toggleSelection(postId)
                        },
                        modifier = modifier,
                    )
                }
            }

            is PostListUiState.Failed -> {
                ErrorStateView(
                    errorMessage = currentState.error.localizedMessage
                        ?: stringResource(R.string.error_prefix_text),
                    onRetry = { viewModel.loadPosts(forceServerRefresh = true) },
                    modifier = modifier,
                )
            }

            is PostListUiState.Loading -> {
                LoadingView() // LoadingView is usually full screen, but we can respect scaffold padding if we want
            }
        }
    }
}

/**
 * Displays the actual list of posts using a [LazyColumn].
 *
 * @param listState The state of the lazy list (scroll position, etc.).
 * @param posts The list of [Post] objects to display.
 * @param selectedPostIds A set of IDs for currently selected posts.
 * @param onPostSelected Callback for when a post is tapped.
 * @param onPostLongPressed Callback for when a post is long-pressed.
 * @param modifier The modifier to apply to the list.
 */
@ExperimentalFoundationApi
@Composable
fun PostList(
    listState: LazyListState,
    posts: List<Post>,
    selectedPostIds: Set<Long>,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPostSelected: (Long) -> Unit,
    onPostLongPressed: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState, modifier = modifier,
    ) {
        items(
            items = posts,
            key = { post ->
                post.id
            },
        ) { post ->
            with(sharedTransitionScope) {
                PostListItem(
                    post = post,
                    selected = selectedPostIds.contains(post.id),
                    onPostSelected = onPostSelected,
                    onPostLongPressed = onPostLongPressed,
                    modifier = Modifier
                        .animateItem()
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "post-${post.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}

/**
 * A centered loading indicator.
 */
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Displays an error message and a retry button.
 *
 * @param errorMessage The error message to display.
 * @param onRetry Callback triggered when the retry button is clicked.
 * @param modifier The modifier to apply to the container.
 */
@Composable
fun ErrorStateView(
    errorMessage: String, onRetry: () -> Unit, modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.error_prefix_text, errorMessage),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(height = 16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry_button_text))
        }
    }
}

/**
 * Displays a message when the content list is empty.
 *
 * @param message The message to display.
 * @param modifier The modifier to apply to the container.
 */
@Composable
fun EmptyContentView(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.headlineSmall)
    }
}
