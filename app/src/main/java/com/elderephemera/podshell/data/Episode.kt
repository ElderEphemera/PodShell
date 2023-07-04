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
    val id: Int = 0,
    val guid: String,
    val feedId: Int,
)