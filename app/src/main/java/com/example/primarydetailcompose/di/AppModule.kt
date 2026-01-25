package com.example.primarydetailcompose.di

import com.example.primarydetailcompose.ui.DefaultPostRepository
import com.example.primarydetailcompose.ui.PostRepository
import dagger.Binds
import dagger.Module
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
abstract class AppModule {

    /**
     * Binds the [DefaultPostRepository] implementation to the [PostRepository] interface.
     *
     * @param impl The implementation of the repository.
     * @return The repository interface.
     */
    @Binds
    @Singleton
    abstract fun bindRepository(impl: DefaultPostRepository): PostRepository
}
