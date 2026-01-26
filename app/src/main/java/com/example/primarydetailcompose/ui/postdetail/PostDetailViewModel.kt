package com.example.primarydetailcompose.ui.postdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Post Detail screen.
 *
 * Uses Assisted Injection to accept a runtime `postId`.
 *
 * @property repository The [PostRepository] to fetch data from.
 * @property postId The ID of the post being viewed.
 */
@HiltViewModel(assistedFactory = PostDetailViewModel.Factory::class)
class PostDetailViewModel @AssistedInject constructor(
    private val repository: PostRepository,
    @Assisted private val postId: Long,
) :
    ViewModel() {

    /**
     * The UI state flow for the post detail screen.
     *
     * It maps the database stream to a [PostDetailUiState], handling errors and loading states.
     */
    val postDetailUiState: StateFlow<PostDetailUiState> =
        repository.postById(postId = postId)
            .map<Post, PostDetailUiState> { post ->
                PostDetailUiState.Success(post = post)
            }
            .catch { e ->
                Log.e("PostDetailViewModel", "Error fetching post details for ID $postId", e)
                emit(value = PostDetailUiState.Failed(error = e as Exception))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = PostDetailUiState.Loading,
            )

    /**
     * Mark the current post as read via repository.
     *
     * @param postId The ID of the post to mark as read.
     */
    fun markRead(postId: Long) = viewModelScope.launch {
        repository.markRead(postId = postId)
    }

    /**
     * Deletes the current post via repository.
     */
    fun deletePost() = viewModelScope.launch {
        postId.let {
            repository.deletePost(postId = it)
        }
    }

    /**
     * Factory interface for creating [PostDetailViewModel] instances with assisted injection.
     */
    @AssistedFactory
    interface Factory {
        /**
         * Creates a [PostDetailViewModel].
         *
         * @param postId The ID of the post to detail.
         */
        fun create(postId: Long): PostDetailViewModel
    }
}
