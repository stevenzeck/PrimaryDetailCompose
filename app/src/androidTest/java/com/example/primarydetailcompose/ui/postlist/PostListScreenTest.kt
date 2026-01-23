package com.example.primarydetailcompose.ui.postlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.primarydetailcompose.model.Post
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PostListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalFoundationApi::class)

    @Test
    fun loadingState_isDisplayed() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(value = PostListUiState.Loading)
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = emptySet())

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostListScreen(
                        onPostSelected = {},
                        viewModel = viewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule
            .onNode(
                matcher = hasProgressBarRangeInfo(
                    rangeInfo = ProgressBarRangeInfo.Indeterminate,
                ),
            )
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun postList_isDisplayed() {
        val post = Post(id = 1, userId = 1, title = "Test Title", body = "Test Body")
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(
                posts = listOf(
                    post,
                ),
            ),
        )
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = emptySet())

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostListScreen(
                        onPostSelected = {},
                        viewModel = viewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(text = "Test Title").assertIsDisplayed()
        // PostListItem only displays the title, not the body
        // composeTestRule.onNodeWithText("Test Body").assertIsDisplayed()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun clickingPost_triggersCallback() {
        val post = Post(id = 1, userId = 1, title = "Click Me", body = "Body")
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(
                posts = listOf(
                    post,
                ),
            ),
        )
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = emptySet())

        var clickedPostId: Long? = null

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostListScreen(
                        onPostSelected = { clickedPostId = it },
                        viewModel = viewModel,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(text = "Click Me").performClick()

        assert(clickedPostId == 1L)
    }
}
