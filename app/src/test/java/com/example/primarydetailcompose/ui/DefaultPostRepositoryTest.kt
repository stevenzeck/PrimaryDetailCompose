package com.example.primarydetailcompose.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.services.ApiService
import com.example.primarydetailcompose.services.PostsDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultPostRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var postsDao: PostsDao
    private lateinit var repository: DefaultPostRepository

    @Before
    fun setup() {
        apiService = mockk(relaxed = true)
        postsDao = mockk(relaxed = true)
        repository = DefaultPostRepository(apiService, postsDao)
    }

    @Test
    fun `getPosts delegates to DAO`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "T", body = "B"))
        coEvery { postsDao.getAllPosts() } returns flowOf(value = posts)

        val result = repository.getPosts().first()

        assertEquals(posts, result)
    }

    @Test
    fun `getServerPosts success`() = runTest {
        val posts = listOf(Post(id = 1, userId = 1, title = "T", body = "B"))
        coEvery { apiService.getAllPosts() } returns posts
        coEvery { postsDao.insertPosts(posts) } returns listOf(1L)

        val result = repository.getServerPosts()

        assertTrue(result.isSuccess)
        coVerify { apiService.getAllPosts() }
        coVerify { postsDao.insertPosts(posts) }
    }

    @Test
    fun `getServerPosts failure`() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { apiService.getAllPosts() } throws exception

        val result = repository.getServerPosts()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 0) { postsDao.insertPosts(posts = any()) }
    }

    @Test
    fun `postById delegates to DAO`() = runTest {
        val post = Post(id = 1, userId = 1, title = "T", body = "B")
        coEvery { postsDao.postById(postId = 1L) } returns flowOf(value = post)

        val result = repository.postById(postId = 1L).first()

        assertEquals(post, result)
    }

    @Test
    fun `markRead delegates to DAO`() = runTest {
        repository.markRead(postId = 1L)
        coVerify { postsDao.markRead(postId = 1L) }

        val list = listOf(1L, 2L)
        repository.markRead(postIds = list)
        coVerify { postsDao.markRead(postIds = list) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        repository.deletePost(postId = 1L)
        coVerify { postsDao.deletePost(postId = 1L) }

        val list = listOf(1L, 2L)
        repository.deletePosts(postIds = list)
        coVerify { postsDao.deletePosts(postIds = list) }
    }
}
