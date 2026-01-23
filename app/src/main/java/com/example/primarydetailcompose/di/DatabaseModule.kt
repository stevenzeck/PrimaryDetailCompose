package com.example.primarydetailcompose.di

import android.content.Context
import androidx.room.Room
import com.example.primarydetailcompose.services.PostsDao
import com.example.primarydetailcompose.services.PostsDatabase
import com.example.primarydetailcompose.util.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for Database related dependencies.
 *
 * Provides the [PostsDatabase] instance and its associated DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Creates and provides the Room database instance.
     *
     * @param appContext The application context.
     * @return The singleton [PostsDatabase] instance.
     */
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context): PostsDatabase {
        return Room.databaseBuilder(
            context = appContext,
            klass = PostsDatabase::class.java,
            name = DATABASE_NAME,
        ).build()
    }

    /**
     * Provides the [PostsDao] instance from the [PostsDatabase].
     *
     * @param postsDatabase The database instance.
     * @return The [PostsDao] interface implementation.
     */
    @Singleton
    @Provides
    fun provideDao(postsDatabase: PostsDatabase): PostsDao {
        return postsDatabase.postsDao()
    }
}
