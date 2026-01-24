package com.example.primarydetailcompose.ui

import android.util.Log
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.services.ApiService
import com.example.primarydetailcompose.services.PostsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository class responsible for managing data flow between the network and the local database.
 *
 * This class acts as a single source of truth for UI data.
 *
 * @property client The API service for network requests.
 * @property postsDao The DAO for local database operations.
 */
class DefaultPostRepository @Inject constructor(
    private val client: ApiService,
    private val postsDao: PostsDao,
) : PostRepository {

    /**
     * Retrieves a stream of all posts from the local database.
     *
     * @return A [Flow] emitting the list of posts.
     */
    override fun getPosts(): Flow<List<Post>> {
        return postsDao.getAllPosts()
    }

    /**
     * Fetches posts from the server and updates the local database.
     *
     * @return A [Result] indicating success or failure.
     */
    override suspend fun getServerPosts(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val postsFromServer = client.getAllPosts()
            insertPosts(postsFromServer)
            Result.success(value = Unit)
        } catch (e: Exception) {
            Log.e("DefaultPostRepository", "Error fetching or inserting server posts", e)
            Result.failure(exception = e)
        }
    }

    /**
     * Inserts a list of posts into the local database.
     *
     * @param posts A list of posts to insert.
     */
    private suspend fun insertPosts(posts: List<Post>) = postsDao.insertPosts(posts)

    /**
     * Retrieves a specific post from the local database by its ID.
     *
     * @param postId The ID of the post to retrieve.
     * @return A [Flow] emitting the requested [Post].
     */
    override fun postById(postId: Long): Flow<Post> {
        return postsDao.postById(postId = postId)
    }

    /**
     * Marks multiple posts as read in the local database.
     *
     * @param postIds A list of IDs of the posts to update.
     */
    override suspend fun markRead(postIds: List<Long>) = postsDao.markRead(postIds = postIds)

    /**
     * Marks a single post as read in the local database.
     *
     * @param postId The ID of the post to update.
     */
    override suspend fun markRead(postId: Long) = postsDao.markRead(postId = postId)

    /**
     * Deletes multiple posts from the local database.
     *
     * @param postIds A list of IDs of the posts to delete.
     */
    override suspend fun deletePosts(postIds: List<Long>) = postsDao.deletePosts(postIds = postIds)

    /**
     * Deletes a single post from the local database.
     *
     * @param postId The ID of the post to delete.
     */
    override suspend fun deletePost(postId: Long) = postsDao.deletePost(postId = postId)
}
