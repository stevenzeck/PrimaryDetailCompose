package com.example.primarydetailcompose.di

import com.example.primarydetailcompose.ui.DefaultPostRepository
import com.example.primarydetailcompose.ui.PostRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, manifest = Config.NONE)
class AppModuleTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: PostRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `verify PostRepository is bound to DefaultPostRepository`() {
        assertTrue(
            "Repository should be an instance of DefaultPostRepository",
            repository is DefaultPostRepository,
        )
    }
}
