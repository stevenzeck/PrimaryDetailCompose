package com.example.primarydetailcompose.ui.postlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.primarydetailcompose.model.Post

/**
 * A Composable that displays a single post item in the list.
 *
 * It supports both click and long-press interactions for selection and navigation.
 *
 * @param post The [Post] data to display.
 * @param selected Whether this item is currently selected (for multi-select mode).
 * @param onPostSelected Callback triggered when the item is clicked. Passes the post ID.
 * @param onPostLongPressed Callback triggered when the item is long-pressed. Passes the post ID.
 * @param modifier The modifier to apply to this item.
 */
@ExperimentalFoundationApi
@Composable
fun PostListItem(
    post: Post,
    selected: Boolean,
    onPostSelected: (Long) -> Unit,
    onPostLongPressed: (Long) -> Unit,
    modifier: Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.background
    }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    onPostSelected(post.id)
                },
                onLongClick = {
                    onPostLongPressed(post.id)
                }
            )
            .fillMaxWidth()
            .background(color = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = post.title,
                // Bold text for unread posts, normal for read posts
                fontWeight = if (post.read) FontWeight.Normal else FontWeight.Bold,
            )
        }
    }
}
