package com.example.primarydetailcompose.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.primarydetailcompose.model.Post.Companion.TABLE_NAME
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a Post.
 *
 * This class serves as both the domain model and the database entity for the Posts table.
 * It is also serializable for network responses and parcelable for Android bundle storage.
 *
 * @property id The unique identifier for the post.
 * @property userId The ID of the user who created the post.
 * @property title The title of the post.
 * @property body The content body of the post.
 * @property read A flag indicating whether the post has been marked as read by the user.
 */
@Parcelize
@Serializable
@Entity(tableName = TABLE_NAME)
data class Post(
    @PrimaryKey
    @ColumnInfo(name = COLUMN_ID)
    @SerialName(value = "id")
    val id: Long,
    @ColumnInfo(name = COLUMN_USER_ID)
    @SerialName(value = "userId")
    val userId: Int,
    @ColumnInfo(name = COLUMN_TITLE)
    @SerialName(value = "title")
    val title: String,
    @ColumnInfo(name = COLUMN_BODY)
    @SerialName(value = "body")
    val body: String,
    @ColumnInfo(name = COLUMN_READ)
    var read: Boolean = false,
) : Parcelable {
    companion object {

        /**
         * The name of the database table for storing posts.
         */
        const val TABLE_NAME = "post"

        const val COLUMN_ID = "id"
        const val COLUMN_USER_ID = "userId"
        const val COLUMN_TITLE = "title"
        const val COLUMN_BODY = "body"
        const val COLUMN_READ = "read"
    }
}
