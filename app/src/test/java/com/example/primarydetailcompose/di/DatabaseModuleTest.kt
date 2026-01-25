package com.example.primarydetailcompose.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class DatabaseModuleTest {

    @Test
    fun `verify database provider`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = DatabaseModule.provideDatabase(context)
        assertNotNull(database)
        database.close()
    }

    @Test
    fun `verify dao provider`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = DatabaseModule.provideDatabase(context)
        val dao = DatabaseModule.provideDao(database)
        assertNotNull(dao)
        database.close()
    }
}
