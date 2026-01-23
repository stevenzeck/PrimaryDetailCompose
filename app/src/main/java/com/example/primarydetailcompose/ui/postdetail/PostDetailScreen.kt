package com.example.primarydetailcompose.ui.postdetail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.primarydetailcompose.R
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.postlist.LoadingView

/**
 * The main screen that displays the details of a selected post.
 *
 * @param postId The ID of the post to display.
 * @param showBackButton Whether to show the back button (hidden on expanded screens).
 * @param onBack Callback triggered when the back button is pressed.
 * @param onDeleteConfirmed Callback triggered after a post is successfully deleted.
 */
@Composable
fun PostDetailScreen(
    postId: Long,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    showBackButton: Boolean,
    onBack: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
    viewModel: PostDetailViewModel = hiltViewModel(
        creationCallback = { factory: PostDetailViewModel.Factory ->
            factory.create(postId)
        },
    ),
) {
    val uiState by viewModel.postDetailUiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(value = false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(id = R.string.delete_post)) },
            text = { Text(text = stringResource(id = R.string.confirm_delete_post)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePost()
                        showDeleteDialog = false
                        onDeleteConfirmed()
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

    when (val currentState = uiState) {
        is PostDetailUiState.Success -> PostDetailContent(
            post = currentState.post,
            showBackButton = showBackButton,
            onBack = onBack,
            onDeleteClicked = { showDeleteDialog = true },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )

        is PostDetailUiState.Failed -> LoadingView()
        is PostDetailUiState.Loading -> LoadingView()
    }
}

/**
 * Displays the content of the post detail screen.
 *
 * @param post The [Post] object to display.
 * @param showBackButton Whether to show the navigation back icon.
 * @param onBack Callback for back navigation.
 * @param onDeleteClicked Callback for the delete action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailContent(
    post: Post,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onDeleteClicked: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.title_post_detail)) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back),
                                contentDescription = stringResource(id = R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteClicked) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = stringResource(id = R.string.delete_post_action),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { padding ->
        with(sharedTransitionScope) {
            Column(
                modifier = Modifier
                    .padding(paddingValues = padding)
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "post-${post.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
            ) {
                Row(modifier = Modifier.padding(all = 20.dp)) {
                    SelectionContainer {
                        Text(text = post.title, fontSize = 30.sp)
                    }
                }
                Row(modifier = Modifier.padding(all = 20.dp)) {
                    SelectionContainer {
                        Text(text = post.body, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
