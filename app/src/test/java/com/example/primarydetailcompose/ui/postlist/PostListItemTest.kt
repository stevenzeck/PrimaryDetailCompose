package com.example.primarydetailcompose.ui.postlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalFoundationApi::class)
class PostListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysPostTitle() {
        val post = Post(id = 1, userId = 1, title = "Test Post", body = "Body")

        composeTestRule.setContent {
            PostListItem(
                post = post,
                selected = false,
                onPostSelected = {},
                onPostLongPressed = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText(text = "Test Post").assertExists()
    }

    @Test
    fun clickingInvokesCallback() {
        val post = Post(id = 1, userId = 1, title = "Test Post", body = "Body")
        var clickedId: Long? = null

        composeTestRule.setContent {
            PostListItem(
                post = post,
                selected = false,
                onPostSelected = { clickedId = it },
                onPostLongPressed = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText(text = "Test Post").performClick()
        assertEquals(1L, clickedId)
    }

    @Test
    fun longPressInvokesCallback() {
        val post = Post(id = 1, userId = 1, title = "Test Post", body = "Body")
        var longPressedId: Long? = null

        composeTestRule.setContent {
            PostListItem(
                post = post,
                selected = false,
                onPostSelected = {},
                onPostLongPressed = { longPressedId = it },
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText(text = "Test Post").performTouchInput {
            longClick()
        }
        assertEquals(1L, longPressedId)
    }

    @Test
    fun rendersSelectedState() {
        // verifying it renders without crashing
        val post = Post(id = 1, userId = 1, title = "Test Post", body = "Body")

        composeTestRule.setContent {
            PostListItem(
                post = post,
                selected = true,
                onPostSelected = {},
                onPostLongPressed = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText(text = "Test Post").assertExists()
    }

    @Test
    fun rendersUnreadState() {
        // verifying it renders without crashing
        val post = Post(id = 1, userId = 1, title = "Test Post", body = "Body", read = false)

        composeTestRule.setContent {
            PostListItem(
                post = post,
                selected = false,
                onPostSelected = {},
                onPostLongPressed = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText(text = "Test Post").assertExists()
    }
}
