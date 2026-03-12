package com.example.primarydetailcompose.services

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.primarydetailcompose.model.Post

/**
 * The Room Database class for the application.
 *
 * Defines the database configuration and serves as the main access point to the persisted data.
 */
@Database(entities = [Post::class], version = 1, exportSchema = true)
//TODO See if the RoomDatabase "error" goes away in future releases
abstract class PostsDatabase : RoomDatabase() {

    /**
     * Provides access to the [PostsDao].
     *
     * @return The [PostsDao] implementation.
     */
    abstract fun postsDao(): PostsDao
}
