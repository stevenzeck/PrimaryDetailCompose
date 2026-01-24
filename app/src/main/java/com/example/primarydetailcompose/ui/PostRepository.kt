package com.example.primarydetailcompose.ui

import com.example.primarydetailcompose.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Interface for Post Repository.
 */
interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    suspend fun getServerPosts(): Result<Unit>
    fun postById(postId: Long): Flow<Post>
    suspend fun markRead(postIds: List<Long>)
    suspend fun markRead(postId: Long)
    suspend fun deletePosts(postIds: List<Long>)
    suspend fun deletePost(postId: Long)
}
