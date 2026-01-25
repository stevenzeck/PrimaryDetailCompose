package com.example.primarydetailcompose

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingPolicies
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.di.AppModule
import com.example.primarydetailcompose.model.Post
import com.example.primarydetailcompose.ui.PostRepository
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit

@UninstallModules(AppModule::class)
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalCoroutinesApi::class,
)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>(
        effectContext = UnconfinedTestDispatcher(),
    )

    @BindValue
    @JvmField
    val repository: PostRepository = mockk(relaxed = true)

    private val postsFlow = MutableStateFlow<List<Post>>(emptyList())

    init {
        // Initialize mock behavior before the rules launch the Activity
        every { repository.getPosts() } returns postsFlow
        coEvery { repository.getServerPosts() } returns Result.success(Unit)
    }

    @Before
    fun setup() {
        // Disable animations to prevent infinite loops in Robolectric
        val context = ApplicationProvider.getApplicationContext<Context>()
        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            0f,
        )
        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            0f,
        )
        Settings.Global.putFloat(
            context.contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            0f,
        )

        // Adjust idling timeouts
        IdlingPolicies.setMasterPolicyTimeout(10, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(10, TimeUnit.SECONDS)
    }

    @Test
    fun navigation_fromListToDetail() {
        val post = Post(id = 1, userId = 1, title = "Navigation Test", body = "Body")
        postsFlow.value = listOf(post)
        every { repository.postById(1L) } returns flowOf(post)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Navigation Test").assertIsDisplayed()
        composeTestRule.onNodeWithText("Navigation Test").performClick()

        composeTestRule.onNodeWithText("Post Detail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Body").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp") // Simulate Phone
    fun deletionOnPhone_navigatesBackToList() {
        val post = Post(id = 1, userId = 1, title = "To Be Deleted", body = "Body")
        postsFlow.value = listOf(post)
        every { repository.postById(1L) } returns flowOf(post)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("To Be Deleted").performClick()
        composeTestRule.onNodeWithText("Post Detail").assertIsDisplayed()

        // Simulate deletion in repository
        postsFlow.value = emptyList()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Posts").assertIsDisplayed()
        composeTestRule.onNodeWithText("No posts to show").assertIsDisplayed()
    }
}
