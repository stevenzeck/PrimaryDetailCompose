package com.example.primarydetailcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.primarydetailcompose.ui.postdetail.PostDetailScreen
import com.example.primarydetailcompose.ui.postdetail.PostDetailViewModel
import com.example.primarydetailcompose.ui.postlist.PostListScreen
import com.example.primarydetailcompose.ui.postlist.PostListUiState
import com.example.primarydetailcompose.ui.postlist.PostListViewModel
import com.example.primarydetailcompose.ui.theme.PrimaryDetailTheme
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import kotlinx.serialization.Serializable

/**
 * Navigation Key for the Post List screen.
 */
@Serializable
object PostList : NavKey

/**
 * Navigation Key for the Post Detail screen.
 *
 * @property postId The unique identifier of the post to display.
 */
@Serializable
data class PostDetail(val postId: Long) : NavKey

/**
 * The Main Activity of the application.
 *
 * This activity hosts the navigation graph and handles adaptive layout logic for
 * list-detail patterns.
 */
@AndroidEntryPoint
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    /**
     * Entry point for accessing the [PostDetailViewModel.Factory] from the Activity context.
     * This is required because we need to manually create the ViewModel factory for assisted injection.
     */
    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryEntryPoint {
        fun postDetailViewModelFactory(): PostDetailViewModel.Factory
    }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the ViewModel factory via the Hilt EntryPoint
        val postDetailViewModelFactory = EntryPointAccessors.fromActivity(
            this,
            ViewModelFactoryEntryPoint::class.java
        ).postDetailViewModelFactory()

        setContent {

            PrimaryDetailTheme {

                // State to manage the back stack of navigation keys
                val backStack = rememberNavBackStack(PostList)
                // Strategy for handling list-detail layout adaptations
                val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
                // ViewModel for the list screen, scoped to the navigation graph
                val postListViewModel: PostListViewModel = hiltViewModel()

                val density = LocalDensity.current
                val windowInfo = LocalWindowInfo.current
                // Determine if the layout is expanded (e.g. tablet) based on width
                val isExpanded = with(density) {
                    windowInfo.containerSize.width >= 840.dp.roundToPx()
                }

                // Observe the UI state of the list to react to data changes (like deletion)
                val postListUiState by postListViewModel.postListUiState.collectAsStateWithLifecycle()

                // Logic to handle deletion consequences
                // We observe the list state. If the currently displayed detail item is missing from the list, we react.
                // Note: This relies on the VM updating the list state after deletion.
                LaunchedEffect(postListUiState, isExpanded) {
                    val currentState = postListUiState
                    if (currentState is PostListUiState.Success) {
                        val currentPosts = currentState.posts
                        val currentDetailKey = backStack.lastOrNull() as? PostDetail

                        if (currentDetailKey != null) {
                            val detailExists = currentPosts.any { it.id == currentDetailKey.postId }
                            if (!detailExists) {
                                // The currently viewed post was deleted
                                if (isExpanded) {
                                    // Tablet: Select first available post
                                    val firstPost = currentPosts.firstOrNull()
                                    if (firstPost != null) {
                                        // Replace the current detail with the first post
                                        backStack.removeLastOrNull()
                                        backStack.add(PostDetail(firstPost.id))
                                    } else {
                                        // List is empty, just go back to clear detail pane (show placeholder)
                                        backStack.removeLastOrNull()
                                    }
                                } else {
                                    // Phone: Navigate back to list
                                    backStack.removeLastOrNull()
                                }
                            }
                        }
                    }
                }

                NavDisplay(
                    modifier = Modifier.fillMaxSize(),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    sceneStrategy = listDetailStrategy,
                    entryProvider = entryProvider {
                        entry<PostList>(
                            metadata = ListDetailSceneStrategy.listPane(
                                detailPlaceholder = {
                                    Text("Select something")
                                }
                            )
                        ) {
                            PostListScreen(
                                onPostSelected = { postId ->
                                    backStack.add(PostDetail(postId = postId))
                                },
                                viewModel = postListViewModel
                            )
                        }
                        entry<PostDetail>(
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { post ->
                            PostDetailScreen(
                                postId = post.postId,
                                viewModelFactory = postDetailViewModelFactory,
                                showBackButton = !isExpanded,
                                onBack = { backStack.removeLastOrNull() },
                                onDeleteConfirmed = {
                                    // This callback is triggered after VM deletes the post.
                                    // The LaunchedEffect above will handle the navigation/selection update
                                    // because the list state will change.
                                    // However, for immediate feedback or if the list update lags, we could do something here.
                                    // But relying on state observation is more robust.
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
