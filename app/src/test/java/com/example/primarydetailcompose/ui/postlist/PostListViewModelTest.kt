package com.example.primarydetailcompose.ui.postlist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import com.example.primarydetailcompose.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PostListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: PostRepository
    private lateinit var viewModel: PostListViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)

        coEvery { repository.getServerPosts() } returns Result.success(value = Unit)
    }

    @Test
    fun `initial load success from DB`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "Title", body = "Body"))
        coEvery { repository.getPosts() } returns flowOf(value = posts)

        viewModel = PostListViewModel(repository = repository)

        val state = viewModel.postListUiState.value
        assertTrue(state is PostListUiState.Success)
        assertEquals(posts, (state as PostListUiState.Success).posts)
    }

    @Test
    fun `initial load empty DB fetches from server`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        coEvery { repository.getServerPosts() } returns Result.success(value = Unit)

        viewModel = PostListViewModel(repository = repository)

        // It should attempt to fetch from server
        coVerify { repository.getServerPosts() }
    }

    @Test
    fun `initial load empty DB server fetch failure`() = runTest {
        val exception = RuntimeException("Network Error")
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        coEvery { repository.getServerPosts() } returns Result.failure(exception = exception)

        viewModel = PostListViewModel(repository = repository)

        val state = viewModel.postListUiState.value
        assertTrue("State was $state", state is PostListUiState.Failed)
        assertEquals(exception, (state as PostListUiState.Failed).error)
    }

    @Test
    fun `initial load DB failure`() = runTest {
        val exception = RuntimeException("DB Error")
        coEvery { repository.getPosts() } returns flow { throw exception }

        viewModel = PostListViewModel(repository = repository)

        val state = viewModel.postListUiState.value
        assertTrue("State was $state", state is PostListUiState.Failed)
        assertEquals(exception, (state as PostListUiState.Failed).error)
    }

    @Test
    fun `force server refresh success`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "Title", body = "Body"))
        coEvery { repository.getPosts() } returns flowOf(value = posts)
        coEvery { repository.getServerPosts() } returns Result.success(value = Unit)

        viewModel = PostListViewModel(repository = repository)

        // Initial state
        val state = viewModel.postListUiState.value
        assertTrue(state is PostListUiState.Success)
        assertEquals(posts, (state as PostListUiState.Success).posts)

        // Force refresh
        viewModel.loadPosts(forceServerRefresh = true)

        coVerify { repository.getServerPosts() }
        // State becomes Loading waiting for DB update
        assertTrue(viewModel.postListUiState.value is PostListUiState.Loading)
    }

    @Test
    fun `force server refresh failure`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "Title", body = "Body"))
        coEvery { repository.getPosts() } returns flowOf(value = posts)
        val exception = RuntimeException("Network Error")
        coEvery { repository.getServerPosts() } returns Result.failure(exception = exception)

        viewModel = PostListViewModel(repository = repository)

        // Force refresh
        viewModel.loadPosts(forceServerRefresh = true)

        coVerify { repository.getServerPosts() }
        // Should fallback to existing data
        val state = viewModel.postListUiState.value
        assertTrue(state is PostListUiState.Success)
        assertEquals(posts, (state as PostListUiState.Success).posts)
    }

    @Test
    fun `toggle selection`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository = repository)

        viewModel.toggleSelection(postId = 1L)
        assertEquals(setOf(1L), viewModel.selectedPostIds.value)

        viewModel.toggleSelection(postId = 2L)
        assertEquals(setOf(1L, 2L), viewModel.selectedPostIds.value)

        viewModel.toggleSelection(postId = 1L)
        assertEquals(setOf(2L), viewModel.selectedPostIds.value)
    }

    @Test
    fun `clear selection`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository = repository)

        viewModel.toggleSelection(postId = 1L)
        viewModel.clearSelection()
        assertTrue(viewModel.selectedPostIds.value.isEmpty())
    }

    @Test
    fun `delete posts`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository = repository)

        viewModel.toggleSelection(postId = 1L)
        viewModel.deletePosts()

        coVerify { repository.deletePosts(postIds = listOf(1L)) }
        assertTrue(viewModel.selectedPostIds.value.isEmpty())
    }

    @Test
    fun `mark read selected posts`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository = repository)

        viewModel.toggleSelection(postId = 1L)
        viewModel.markRead()

        coVerify { repository.markRead(postIds = listOf(1L)) }
        assertTrue(viewModel.selectedPostIds.value.isEmpty())
    }

    @Test
    fun `mark read single post`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository = repository)

        viewModel.markRead(postId = 1L)

        coVerify { repository.markRead(postId = 1L) }
    }
}
