package com.example.primarydetailcompose.services

import android.content.Context
import androidx.room.Room
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
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class PostsDaoTest {
    private lateinit var postsDao: PostsDao
    private lateinit var db: PostsDatabase

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
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetPosts() = runBlocking {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        postsDao.insertPosts(listOf(post))

        val posts = postsDao.getAllPosts().first()
        assertEquals(1, posts.size)
        assertEquals(post, posts[0])
    }

    @Test
    fun deletePost() = runBlocking {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        postsDao.insertPosts(posts = listOf(post))

        postsDao.deletePost(postId = 1)
        val posts = postsDao.getAllPosts().first()
        assertTrue(posts.isEmpty())
    }

    @Test
    fun deletePosts() = runBlocking {
        val posts = listOf(
            Post(id = 1, userId = 1, title = "T1", body = "B1"),
            Post(id = 2, userId = 1, title = "T2", body = "B2"),
            Post(id = 3, userId = 1, title = "T3", body = "B3"),
        )
        postsDao.insertPosts(posts)

        postsDao.deletePosts(listOf(1L, 2L))
        val remainingPosts = postsDao.getAllPosts().first()
        assertEquals(1, remainingPosts.size)
        assertEquals(3L, remainingPosts[0].id)
    }

    @Test
    fun markRead() = runBlocking {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body", read = false)
        postsDao.insertPosts(posts = listOf(post))

        postsDao.markRead(postId = 1)

        val loaded = postsDao.postById(postId = 1).first()
        assertTrue(loaded.read)
    }

    @Test
    fun markReadMultiple() = runBlocking {
        val posts = listOf(
            Post(id = 1, userId = 1, title = "T1", body = "B1", read = false),
            Post(id = 2, userId = 1, title = "T2", body = "B2", read = false),
        )
        postsDao.insertPosts(posts)

        postsDao.markRead(listOf(1L, 2L))

        val loaded = postsDao.getAllPosts().first()
        assertTrue(loaded.all { it.read })
    }

    @Test
    fun getPostsCount() = runBlocking {
        val posts = listOf(
            Post(id = 1, userId = 1, title = "T1", body = "B1"),
            Post(id = 2, userId = 1, title = "T2", body = "B2"),
        )
        postsDao.insertPosts(posts)

        val count = postsDao.getPostsCount()
        assertEquals(2, count)
    }
}
