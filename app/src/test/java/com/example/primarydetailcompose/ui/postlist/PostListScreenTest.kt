package com.example.primarydetailcompose.ui.postlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.model.Post
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    fun failedState_isDisplayed() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        val errorMessage = "Error loading posts"
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Failed(error = Exception(errorMessage)),
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

        composeTestRule.onNodeWithText(text = "Error: $errorMessage").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Retry").assertIsDisplayed()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun emptyState_isDisplayed() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(posts = emptyList()),
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

        composeTestRule.onNodeWithText(text = "No posts to show").assertIsDisplayed()
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
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun clickingPost_triggersCallbackAndMarkRead() {
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
        verify { viewModel.markRead(postId = 1L) }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun longPressPost_togglesSelection() {
        val post = Post(id = 1, userId = 1, title = "Long Press Me", body = "Body")
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(posts = listOf(post)),
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

        composeTestRule.onNodeWithText(text = "Long Press Me").performTouchInput {
            longClick()
        }

        verify { viewModel.toggleSelection(postId = 1L) }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun selectionMode_uiElementsDisplayed() {
        val post = Post(id = 1, userId = 1, title = "Title", body = "Body")
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(posts = listOf(post)),
        )
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = setOf(1L))

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

        composeTestRule.onNodeWithText(text = "1 Selected").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = "Clear selected items")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = "Mark Read").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = "Delete").assertIsDisplayed()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun selectionMode_clearSelection_callsViewModel() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(posts = emptyList()),
        )
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = setOf(1L))

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

        composeTestRule.onNodeWithContentDescription(label = "Clear selected items").performClick()
        verify { viewModel.clearSelection() }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun selectionMode_markRead_callsViewModel() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(posts = emptyList()),
        )
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = setOf(1L))

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

        composeTestRule.onNodeWithContentDescription(label = "Mark Read").performClick()
        verify { viewModel.markRead() }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun selectionMode_deleteClick_showsDialog_confirmCallsViewModel() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Success(posts = emptyList()),
        )
        every { viewModel.selectedPostIds } returns MutableStateFlow(value = setOf(1L))

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

        composeTestRule.onNodeWithContentDescription(label = "Delete").performClick()

        // Verify dialog is shown
        composeTestRule.onNodeWithText(text = "Delete Selected Posts?").assertIsDisplayed()

        // Confirm delete
        composeTestRule.onNodeWithText(text = "Delete").performClick()

        verify { viewModel.deletePosts() }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun failedState_retry_callsLoadPosts() {
        val viewModel = mockk<PostListViewModel>(relaxed = true)
        every { viewModel.postListUiState } returns MutableStateFlow(
            value = PostListUiState.Failed(error = Exception("Error")),
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

        composeTestRule.onNodeWithText(text = "Retry").performClick()

        verify { viewModel.loadPosts(forceServerRefresh = true) }
    }
}
