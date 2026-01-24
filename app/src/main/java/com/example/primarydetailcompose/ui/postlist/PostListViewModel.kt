package com.example.primarydetailcompose.ui.postlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Backing property for UI state
    private val _uiState = MutableStateFlow<PostListUiState>(value = PostListUiState.Loading)

    /**
     * The current state of the UI (Loading, Success, or Failed).
     */
    val postListUiState: StateFlow<PostListUiState> = _uiState.asStateFlow()

    // Backing property for selected post IDs
    private val _selectedPostIds = MutableStateFlow<Set<Long>>(value = emptySet())

    /**
     * A set of IDs representing the currently selected posts.
     */
    val selectedPostIds: StateFlow<Set<Long>> = _selectedPostIds.asStateFlow()

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
            if (isInitialLoad || forceServerRefresh || _uiState.value is PostListUiState.Loading) {
                _uiState.value = PostListUiState.Loading
            }

            repository.getPosts().catch { e ->
                    Log.e("PostListViewModel", "Error in posts data flow from repository", e)
                    _uiState.value = PostListUiState.Failed(error = e as Exception)
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
                        _uiState.value = PostListUiState.Success(posts)
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
        _uiState.value = PostListUiState.Loading

        repository.getServerPosts().onSuccess {
                Log.d(
                    "PostListViewModel",
                    "Server fetch successful. Waiting for DB update.",
                )
                initialFetchAttemptedOrSucceeded = true
                // If successful, the flow will emit again with new data.
                // We update state only if it's currently not failed.
                if (_uiState.value !is PostListUiState.Failed) {
                    _uiState.value = PostListUiState.Success(posts)
                }
            }.onFailure { exception ->
                Log.e(
                    "PostListViewModel",
                    "Failed to fetch posts from server",
                    exception,
                )
                initialFetchAttemptedOrSucceeded = true
                _uiState.value = PostListUiState.Failed(error = exception as Exception)
            }
    }

    /**
     * Helper function to handle the native Result for a forced refresh.
     */
    private suspend fun performServerRefresh(posts: List<Post>) {
        _uiState.value = PostListUiState.Loading

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
                _uiState.value = PostListUiState.Success(posts)
                // TODO: Consider a way to show a non-critical error message to the user (e.g., Snackbar)
            }
    }

    /**
     * Marks all currently selected posts as read.
     */
    fun markRead() = viewModelScope.launch {
        val ids = _selectedPostIds.value.toList()
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
        val ids = _selectedPostIds.value.toList()
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
        val currentSelection = _selectedPostIds.value
        if (currentSelection.contains(postId)) {
            _selectedPostIds.value = currentSelection - postId
        } else {
            _selectedPostIds.value = currentSelection + postId
        }
    }

    /**
     * Clears the current selection of posts.
     */
    fun clearSelection() {
        _selectedPostIds.value = emptySet()
    }
}
