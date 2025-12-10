package com.example.primarydetailcompose.services

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.primarydetailcompose.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the [Post] table.
 *
 * Provides methods for reading, writing, updating, and deleting posts in the local database.
 */
@Dao
interface PostsDao {

    /**
     * Retrieve all posts from the database, ordered by ID in descending order.
     *
     * @return A [Flow] emitting the list of [Post]s currently in the database.
     */
    @Query("SELECT * FROM ${Post.TABLE_NAME} ORDER BY ${Post.COLUMN_ID} desc")
    fun getAllPosts(): Flow<List<Post>>

    /**
     * Retrieve the count of posts in the database.
     *
     * @return The total number of posts.
     */
    @Query("SELECT COUNT(*) FROM ${Post.TABLE_NAME}")
    fun getPostsCount(): Int

    /**
     * Retrieve a specific post from the database by its ID.
     *
     * @param postId The ID of the post to find.
     * @return A [Flow] emitting the requested [Post].
     */
    @Query("SELECT * FROM ${Post.TABLE_NAME} WHERE ${Post.COLUMN_ID} = :postId")
    fun postById(postId: Long): Flow<Post>

    /**
     * Insert a list of posts into the database.
     *
     * If a post with the same ID already exists, it will be replaced.
     *
     * @param posts The list of [Post]s to insert.
     * @return A list of row IDs for the inserted posts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>): List<Long>

    /**
     * Mark multiple posts as read.
     *
     * @param postIds The list of IDs of the posts to mark as read.
     */
    @Query("UPDATE ${Post.TABLE_NAME} SET ${Post.COLUMN_READ} = 1 WHERE ${Post.COLUMN_ID} IN (:postIds)")
    suspend fun markRead(postIds: List<Long>)

    /**
     * Mark a single post as read.
     *
     * @param postId The ID of the post to mark as read.
     */
    @Query("UPDATE ${Post.TABLE_NAME} SET ${Post.COLUMN_READ} = 1 WHERE ${Post.COLUMN_ID} = :postId")
    suspend fun markRead(postId: Long)

    /**
     * Delete multiple posts from the database.
     *
     * @param postIds The list of IDs of the posts to delete.
     */
    @Query("DELETE FROM ${Post.TABLE_NAME} WHERE ${Post.COLUMN_ID} IN (:postIds)")
    suspend fun deletePosts(postIds: List<Long>)

    /**
     * Delete a single post from the database.
     *
     * @param postId The ID of the post to delete.
     */
    @Query("DELETE FROM ${Post.TABLE_NAME} WHERE ${Post.COLUMN_ID} = :postId")
    suspend fun deletePost(postId: Long)
}
