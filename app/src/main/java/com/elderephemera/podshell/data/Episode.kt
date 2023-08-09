package com.elderephemera.podshell.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.Duration.Companion.milliseconds

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
    @ColumnInfo(index = true)
    val feedId: Long,
    val source: String,
    val inPlaylist: Boolean = false,
    val new: Boolean = false,
    val position: Long? = null,
    val length: Long? = null,
    val logo: String?,
    val title: String,
    val url: String,
    val pubDate: String,
    val description: String,
) {
    val pubDateTime: LocalDateTime? get() =
        try {
            LocalDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
        } catch (e: DateTimeParseException) {
            null
        }

    val pubDateDisplay: String get() = pubDateTime?.toLocalDate()?.toString() ?: pubDate

    val lengthDisplay: String get() = length
        ?.milliseconds
        ?.toComponents { hours, minutes, _, _ ->
            (if (hours != 0L) "${hours}h" else "") + "${minutes}m"
        }
        ?: ""
}