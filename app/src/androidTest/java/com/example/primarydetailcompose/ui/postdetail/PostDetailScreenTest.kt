package com.example.primarydetailcompose.ui.postdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.primarydetailcompose.model.Post
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PostDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun postDetails_areDisplayed() {
        val post = Post(id = 1, userId = 1, title = "Detail Title", body = "Detail Body")
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Success(
                post,
            ),
        )

        val factory = mockk<PostDetailViewModel.Factory>()
        every { factory.create(1L) } returns viewModel

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModelFactory = factory,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(text = "Detail Title").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Detail Body").assertIsDisplayed()
    }

    @Test
    fun backButton_isDisplayed_whenEnabled() {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Success(
                post,
            ),
        )
        val factory = mockk<PostDetailViewModel.Factory>()
        every { factory.create(any()) } returns viewModel

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModelFactory = factory,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription(label = "Back").assertIsDisplayed()
    }

    @Test
    fun backButton_isHidden_whenDisabled() {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Success(
                post,
            ),
        )
        val factory = mockk<PostDetailViewModel.Factory>()
        every { factory.create(any()) } returns viewModel

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModelFactory = factory,
                        showBackButton = false,
                        onBack = {},
                        onDeleteConfirmed = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription(label = "Back").assertIsNotDisplayed()
    }

    @Test
    fun deleteAction_triggersViewModelAndCallback() {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Success(
                post,
            ),
        )
        val factory = mockk<PostDetailViewModel.Factory>()
        every { factory.create(any()) } returns viewModel

        var deleteConfirmed = false

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModelFactory = factory,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = { deleteConfirmed = true },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        // Click delete icon directly (no overflow menu)
        composeTestRule.onNodeWithContentDescription(label = "Delete Post").performClick()

        // Confirm deletion in dialog
        composeTestRule.onNodeWithText(text = "Delete").performClick()

        // Verify the mock call
        io.mockk.verify { viewModel.deletePost() }

        // Wait/Verify callback (if implementation invokes it immediately)
        assert(deleteConfirmed)
    }
}
