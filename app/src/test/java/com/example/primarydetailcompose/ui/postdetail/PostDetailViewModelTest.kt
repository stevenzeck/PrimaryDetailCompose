package com.example.primarydetailcompose.ui.postdetail

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import com.example.primarydetailcompose.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PostDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: PostRepository
    private lateinit var viewModel: PostDetailViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `load post success`() = runTest {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        coEvery { repository.postById(postId = 1L) } returns flowOf(value = post)

        viewModel = PostDetailViewModel(repository = repository, postId = 1L)

        // Start collecting the state flow to trigger the upstream flow
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.postDetailUiState.collect()
        }

        val state = viewModel.postDetailUiState.value
        assertTrue(state is PostDetailUiState.Success)
        assertEquals(post, (state as PostDetailUiState.Success).post)
    }

    @Test
    fun `load post failure`() = runTest {
        val exception = RuntimeException("Error")
        coEvery { repository.postById(postId = 1L) } returns flow { throw exception }

        viewModel = PostDetailViewModel(repository = repository, postId = 1L)

        // Start collecting to trigger flow
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.postDetailUiState.collect()
        }

        val state = viewModel.postDetailUiState.value
        assertTrue(state is PostDetailUiState.Failed)
        assertEquals(exception, (state as PostDetailUiState.Failed).error)
    }

    @Test
    fun `mark read`() = runTest {
        coEvery { repository.postById(postId = 1L) } returns flowOf(
            value = Post(
                id = 1,
                userId = 1,
                title = "T",
                body = "B",
            ),
        )
        viewModel = PostDetailViewModel(repository = repository, postId = 1L)

        viewModel.markRead(postId = 1L)

        coVerify { repository.markRead(postId = 1L) }
    }

    @Test
    fun `delete post`() = runTest {
        coEvery { repository.postById(postId = 1L) } returns flowOf(
            value = Post(
                id = 1,
                userId = 1,
                title = "T",
                body = "B",
            ),
        )
        viewModel = PostDetailViewModel(repository = repository, postId = 1L)

        viewModel.deletePost()

        coVerify { repository.deletePost(postId = 1L) }
    }
}
