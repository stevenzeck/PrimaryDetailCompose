package com.example.primarydetailcompose.ui.postdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_isDisplayed() {
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(value = PostDetailUiState.Loading)

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun failedState_isDisplayed() {
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Failed(error = Exception("Error")),
        )

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        // According to current implementation, Failed state shows LoadingView()
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun postDetails_areDisplayed() {
        val post = Post(id = 1, userId = 1, title = "Detail Title", body = "Detail Body")
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Success(
                post,
            ),
        )

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
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

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
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

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
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

        var deleteConfirmed = false

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = { deleteConfirmed = true },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        // Click delete icon directly
        composeTestRule.onNodeWithContentDescription(label = "Delete Post").performClick()

        // Confirm deletion in dialog
        composeTestRule.onNodeWithText(text = "Delete").performClick()

        // Verify the mock call
        io.mockk.verify { viewModel.deletePost() }

        // Verify callback
        assert(deleteConfirmed)
    }

    @Test
    fun deleteAction_cancel_doesNotTriggerDelete() {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        val viewModel = mockk<PostDetailViewModel>(relaxed = true)
        every { viewModel.postDetailUiState } returns MutableStateFlow(
            value = PostDetailUiState.Success(post),
        )

        var deleteConfirmed = false

        composeTestRule.setContent {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    PostDetailScreen(
                        postId = 1L,
                        viewModel = viewModel,
                        showBackButton = true,
                        onBack = {},
                        onDeleteConfirmed = { deleteConfirmed = true },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }

        composeTestRule.onNodeWithContentDescription(label = "Delete Post").performClick()
        composeTestRule.onNodeWithText(text = "Cancel").performClick()

        io.mockk.verify(exactly = 0) { viewModel.deletePost() }
        assert(!deleteConfirmed)
    }
}
