package com.elderephemera.podshell.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = Feed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
)
data class Episode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val guid: String,
    val feedId: Long,
    val source: String,
    val inPlaylist: Boolean = false,
    val position: Long? = null,
    val logo: String?,
    val title: String,
    val url: String,
    val pubDate: String,
    val description: String,
)