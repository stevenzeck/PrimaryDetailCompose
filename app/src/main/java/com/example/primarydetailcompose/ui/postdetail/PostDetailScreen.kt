package com.example.primarydetailcompose.ui.postdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.postlist.LoadingView

/**
 * A generic ViewModel factory that allows creating ViewModels via a lambda.
 *
 * Useful for assisted injection where arguments need to be passed at runtime.
 */
@Suppress("UNCHECKED_CAST")
class LambdaViewModelFactory<T : ViewModel>(
    private val create: () -> T
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create() as T
    }
}

/**
 * The main screen that displays the details of a selected post.
 *
 * @param postId The ID of the post to display.
 * @param viewModelFactory The factory to create the [PostDetailViewModel].
 * @param showBackButton Whether to show the back button (hidden on expanded screens).
 * @param onBack Callback triggered when the back button is pressed.
 * @param onDeleteConfirmed Callback triggered after a post is successfully deleted.
 */
@Composable
fun PostDetailScreen(
    postId: Long,
    viewModelFactory: PostDetailViewModel.Factory,
    showBackButton: Boolean,
    onBack: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {},
) {
    // Manually create the ViewModel using the factory to pass the postId
    val viewModel: PostDetailViewModel = viewModel(
        key = "post_detail_$postId",
        factory = LambdaViewModelFactory { viewModelFactory.create(postId) }
    )
    val uiState by viewModel.postDetailUiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Post?") },
            text = { Text(text = "Are you sure you want to delete this post? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePost()
                        showDeleteDialog = false
                        onDeleteConfirmed()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    when (val currentState = uiState) {
        is PostDetailUiState.Success -> PostDetailContent(
            post = currentState.post,
            showBackButton = showBackButton,
            onBack = onBack,
            onDeleteClicked = { showDeleteDialog = true }
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
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Post Detail") },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteClicked) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Post"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(modifier = Modifier.padding(20.dp)) {
                SelectionContainer {
                    Text(text = post.title, fontSize = 30.sp)
                }
            }
            Row(modifier = Modifier.padding(20.dp)) {
                SelectionContainer {
                    Text(text = post.body, fontSize = 18.sp)
                }
            }
        }
    }
}
