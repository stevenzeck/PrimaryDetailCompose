package com.example.primarydetailcompose.ui.postdetail

import com.example.primarydetailcompose.model.Post

/**
 * Represents the various UI states for the Post Detail screen.
 */
sealed interface PostDetailUiState {

    /**
     * State indicating that the post details are currently loading.
     */
    data object Loading : PostDetailUiState

    /**
     * State indicating that the post details were successfully retrieved.
     *
     * @property post The [Post] object to display.
     */
    data class Success(
        val post: Post,
    ) : PostDetailUiState

    /**
     * State indicating that an error occurred while fetching the post details.
     *
     * @property error The exception causing the failure.
     */
    data class Failed(
        val error: Exception,
    ) : PostDetailUiState
}
