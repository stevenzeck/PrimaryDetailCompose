package com.example.primarydetailcompose.services

import android.content.Context
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.uuid.Uuid

@RunWith(AndroidJUnit4::class)
class PostsDaoTest {
    private lateinit var postsDao: PostsDao
    private lateinit var db: PostsDatabase

    private fun generateMockPost(
        title: String,
        body: String,
        id: Long = Uuid.random().hashCode().toLong(),
        read: Boolean = false,
    ): Post {
        return Post(id = id, userId = 1, title = title, body = body, read = read)
    }

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context = context, klass = PostsDatabase::class.java,
        )
            .allowMainThreadQueries() // Allow queries on main thread for tests
            .build()
        postsDao = db.postsDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetPosts() = runBlocking {
        val post = generateMockPost(title = "Title", body = "Body")
        postsDao.insertPosts(listOf(post))

        val posts = postsDao.getAllPosts().first()
        assertEquals(1, posts.size)
        assertEquals(post, posts[0])
    }

    @Test
    fun deletePost() = runBlocking {
        val post = generateMockPost(title = "Title", body = "Body")
        postsDao.insertPosts(posts = listOf(post))

        postsDao.deletePost(postId = post.id)
        val posts = postsDao.getAllPosts().first()
        assertTrue(posts.isEmpty())
    }

    @Test
    fun deletePosts() = runBlocking {
        val p1 = generateMockPost(title = "T1", body = "B1")
        val p2 = generateMockPost(title = "T2", body = "B2")
        val p3 = generateMockPost(title = "T3", body = "B3")
        postsDao.insertPosts(listOf(p1, p2, p3))

        postsDao.deletePosts(listOf(p1.id, p2.id))
        val remainingPosts = postsDao.getAllPosts().first()
        assertEquals(1, remainingPosts.size)
        assertEquals(p3.id, remainingPosts[0].id)
    }

    @Test
    fun markRead() = runBlocking {
        val post = generateMockPost(title = "Title", body = "Body", read = false)
        postsDao.insertPosts(posts = listOf(post))

        postsDao.markRead(postId = post.id)

        val loaded = postsDao.postById(postId = post.id).first()
        assertTrue(loaded.read)
    }

    @Test
    fun markReadMultiple() = runBlocking {
        val p1 = generateMockPost(title = "T1", body = "B1", read = false)
        val p2 = generateMockPost(title = "T2", body = "B2", read = false)
        postsDao.insertPosts(listOf(p1, p2))

        postsDao.markRead(listOf(p1.id, p2.id))

        val loaded = postsDao.getAllPosts().first()
        assertTrue(loaded.all { it.read })
    }

    @Test
    fun getPostsCount() = runBlocking {
        val posts = listOf(
            generateMockPost(title = "T1", body = "B1"),
            generateMockPost(title = "T2", body = "B2"),
        )
        postsDao.insertPosts(posts)

        val count = postsDao.getPostsCount()
        assertEquals(2, count)
    }

    @Test
    fun getAllPosts_returnsPostsSortedByIdDescending() = runBlocking {
        val posts = listOf(
            generateMockPost(title = "T10", body = "B10", id = 10L),
            generateMockPost(title = "T30", body = "B30", id = 30L),
            generateMockPost(title = "T20", body = "B20", id = 20L),
        )
        postsDao.insertPosts(posts)

        val retrievedPosts = postsDao.getAllPosts().first()
        assertTrue(retrievedPosts.isSortedByDescending { it.id })
    }
}
