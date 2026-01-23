package com.example.primarydetailcompose.di

import com.example.primarydetailcompose.services.ApiService
import com.example.primarydetailcompose.services.PostsDao
import com.example.primarydetailcompose.ui.DefaultPostRepository
import com.example.primarydetailcompose.ui.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module that provides application-level dependencies.
 *
 * This module is installed in the [SingletonComponent] to ensure that dependencies
 * provided here live for the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a singleton instance of [PostRepository].
     *
     * @param client The [ApiService] for network operations.
     * @param postsDao The [PostsDao] for local database operations.
     * @return A new instance of [PostRepository].
     */
    @Provides
    @Singleton
    fun provideRepository(
        client: ApiService,
        postsDao: PostsDao,
    ): PostRepository = DefaultPostRepository(client = client, postsDao = postsDao)
}
