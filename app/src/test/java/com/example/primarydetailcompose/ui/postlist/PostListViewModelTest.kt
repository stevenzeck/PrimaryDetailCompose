package com.example.primarydetailcompose.ui.postlist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import com.example.primarydetailcompose.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PostListViewModelTest {

    private lateinit var repository: PostRepository
    private lateinit var viewModel: PostListViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `initial load success from DB`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "Title", body = "Body"))
        coEvery { repository.getPosts() } returns flowOf(value = posts)

        viewModel = PostListViewModel(repository)

        val state = viewModel.postListUiState.value
        assertTrue(state is PostListUiState.Success)
        assertEquals(posts, (state as PostListUiState.Success).posts)
    }

    @Test
    fun `initial load empty DB fetches from server`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        coEvery { repository.getServerPosts() } returns Result.Success(data = Unit)

        viewModel = PostListViewModel(repository)

        // It should attempt to fetch from server
        coVerify { repository.getServerPosts() }
    }

    @Test
    fun `force server refresh`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "Title", body = "Body"))
        coEvery { repository.getPosts() } returns flowOf(value = posts)
        coEvery { repository.getServerPosts() } returns Result.Success(data = Unit)

        viewModel = PostListViewModel(repository)

        // Initial state
        assertEquals(posts, (viewModel.postListUiState.value as PostListUiState.Success).posts)

        // Force refresh
        viewModel.loadPosts(forceServerRefresh = true)

        coVerify { repository.getServerPosts() }
    }

    @Test
    fun `toggle selection`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository)

        viewModel.toggleSelection(postId = 1L)
        assertEquals(setOf(1L), viewModel.selectedPostIds.value)

        viewModel.toggleSelection(postId = 2L)
        assertEquals(setOf(1L, 2L), viewModel.selectedPostIds.value)

        viewModel.toggleSelection(postId = 1L)
        assertEquals(setOf(2L), viewModel.selectedPostIds.value)
    }

    @Test
    fun `delete posts`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository)

        viewModel.toggleSelection(postId = 1L)
        viewModel.deletePosts()

        coVerify { repository.deletePosts(postIds = listOf(1L)) }
        assertTrue(viewModel.selectedPostIds.value.isEmpty())
    }

    @Test
    fun `mark read`() = runTest {
        coEvery { repository.getPosts() } returns flowOf(value = emptyList())
        viewModel = PostListViewModel(repository)

        viewModel.toggleSelection(postId = 1L)
        viewModel.markRead()

        coVerify { repository.markRead(postIds = listOf(1L)) }
        assertTrue(viewModel.selectedPostIds.value.isEmpty())
    }
}
