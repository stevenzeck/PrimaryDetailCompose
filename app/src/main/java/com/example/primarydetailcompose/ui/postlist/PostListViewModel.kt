package com.example.primarydetailcompose.ui.postlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Post List screen.
 *
 * Manages the UI state, data fetching, and user actions like selection and deletion.
 *
 * @property repository The [PostRepository] for data access.
 */
@HiltViewModel
class PostListViewModel @Inject constructor(private val repository: PostRepository) : ViewModel() {

    /**
     * The current state of the UI (Loading, Success, or Failed).
     */
    val postListUiState: StateFlow<PostListUiState> field = MutableStateFlow<PostListUiState>(value = PostListUiState.Loading)

    /**
     * A set of IDs representing the currently selected posts.
     */
    val selectedPostIds: StateFlow<Set<Long>> field = MutableStateFlow<Set<Long>>(value = emptySet())

    // Flag to track if we've successfully fetched data or attempted to
    private var initialFetchAttemptedOrSucceeded = false

    init {
        loadPosts(isInitialLoad = true)
    }

    /**
     * Loads posts from the repository.
     *
     * It observes the database flow and handles scenarios for initial load, empty database,
     * or forced server refresh.
     *
     * @param isInitialLoad Whether this is the first load on ViewModel creation.
     * @param forceServerRefresh Whether to force a fetch from the server regardless of local data.
     */
    fun loadPosts(isInitialLoad: Boolean = false, forceServerRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isInitialLoad || forceServerRefresh || postListUiState.value is PostListUiState.Loading) {
                postListUiState.value = PostListUiState.Loading
            }

            repository.getPosts().catch { e ->
                Log.e("PostListViewModel", "Error in posts data flow from repository", e)
                postListUiState.value = PostListUiState.Failed(error = e as Exception)
            }.collect { posts ->
                val currentlyEmpty = posts.isEmpty()

                // Check if we need to fetch from network:
                // 1. It's initial load, we haven't tried yet, and DB is empty.
                // 2. A forced refresh is requested and DB is empty.
                if ((isInitialLoad && !initialFetchAttemptedOrSucceeded && currentlyEmpty) || (forceServerRefresh && currentlyEmpty)) {
                    Log.d(
                        "PostListViewModel",
                        "Local database is empty or refresh forced. Fetching from server...",
                    )
                    performServerFetch(posts)
                } else if (forceServerRefresh) {
                    // DB has data, but user forced a refresh
                    Log.d("PostListViewModel", "DB not empty, but forcing server refresh...")
                    performServerRefresh(posts)
                } else {
                    // Normal case: emit success with data from DB
                    postListUiState.value = PostListUiState.Success(posts)
                    if (posts.isNotEmpty()) {
                        initialFetchAttemptedOrSucceeded = true
                    }
                }
            }
        }
    }

    /**
     * Helper function to handle the native Result for an initial/empty fetch.
     */
    private suspend fun performServerFetch(posts: List<Post>) {
        postListUiState.value = PostListUiState.Loading

        repository.getServerPosts().onSuccess {
            Log.d(
                "PostListViewModel",
                "Server fetch successful. Waiting for DB update.",
            )
            initialFetchAttemptedOrSucceeded = true
            // If successful, the flow will emit again with new data.
            // We update state only if it's currently not failed.
            if (postListUiState.value !is PostListUiState.Failed) {
                postListUiState.value = PostListUiState.Success(posts)
            }
        }.onFailure { exception ->
            Log.e(
                "PostListViewModel",
                "Failed to fetch posts from server",
                exception,
            )
            initialFetchAttemptedOrSucceeded = true
            postListUiState.value = PostListUiState.Failed(error = exception as Exception)
        }
    }

    /**
     * Helper function to handle the native Result for a forced refresh.
     */
    private suspend fun performServerRefresh(posts: List<Post>) {
        postListUiState.value = PostListUiState.Loading

        repository.getServerPosts().onSuccess {
            Log.d(
                "PostListViewModel",
                "Server refresh successful. Waiting for DB update.",
            )
            initialFetchAttemptedOrSucceeded = true
        }.onFailure { exception ->
            Log.e(
                "PostListViewModel",
                "Failed to refresh posts from server",
                exception,
            )
            initialFetchAttemptedOrSucceeded = true
            // Fallback to showing existing local data
            postListUiState.value = PostListUiState.Success(posts)
            // TODO: Consider a way to show a non-critical error message to the user (e.g., Snackbar)
        }
    }

    /**
     * Marks all currently selected posts as read.
     */
    fun markRead() = viewModelScope.launch {
        val ids = selectedPostIds.value.toList()
        if (ids.isNotEmpty()) {
            repository.markRead(postIds = ids)
            clearSelection()
        }
    }

    /**
     * Marks a single post as read.
     *
     * @param postId The ID of the post to mark as read.
     */
    fun markRead(postId: Long) = viewModelScope.launch {
        repository.markRead(postId = postId)
    }

    /**
     * Deletes all currently selected posts.
     */
    fun deletePosts() = viewModelScope.launch {
        val ids = selectedPostIds.value.toList()
        if (ids.isNotEmpty()) {
            repository.deletePosts(postIds = ids)
            clearSelection()
        }
    }

    /**
     * Toggles the selection state of a specific post.
     *
     * @param postId The ID of the post to toggle.
     */
    fun toggleSelection(postId: Long) {
        val currentSelection = selectedPostIds.value
        if (currentSelection.contains(postId)) {
            selectedPostIds.value = currentSelection - postId
        } else {
            selectedPostIds.value = currentSelection + postId
        }
    }

    /**
     * Clears the current selection of posts.
     */
    fun clearSelection() {
        selectedPostIds.value = emptySet()
    }
}
