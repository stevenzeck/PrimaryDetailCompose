package com.example.primarydetailcompose.ui.postlist

import com.example.primarydetailcompose.model.Post

/**
 * Represents the various UI states for the Post List screen.
 */
sealed interface PostListUiState {

    /**
     * State indicating that data is currently being loaded.
     */
    data object Loading : PostListUiState

    /**
     * State indicating that the post list was successfully retrieved.
     *
     * @property posts The list of [Post] objects to display.
     */
    data class Success(
        val posts: List<Post>,
    ) : PostListUiState

    /**
     * State indicating that an error occurred while fetching the data.
     *
     * @property error The exception causing the failure.
     */
    data class Failed(
        val error: Exception,
    ) : PostListUiState
}
