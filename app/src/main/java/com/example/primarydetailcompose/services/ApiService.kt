package com.example.primarydetailcompose.services

import com.example.primarydetailcompose.model.Post
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface defining the API endpoints for fetching posts.
 */
interface ApiService {

    /**
     * Get all posts from the server.
     *
     * @return A list of [Post] objects.
     */
    @GET("/posts")
    suspend fun getAllPosts(): List<Post>

    /**
     * Get a single post by its ID.
     *
     * @param postId The ID of the post to retrieve.
     * @return The requested [Post].
     */
    @GET("/posts/{id}")
    suspend fun getPostById(@Path(value = "id") postId: Int): Post
}
