package com.elderephemera.podshell.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val logo: String?,
    val title: String,
    val url: String,
    val description: String,
    val refreshed: Long = Instant.now().epochSecond,
)