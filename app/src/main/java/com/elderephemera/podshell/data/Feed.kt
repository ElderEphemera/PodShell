package com.elderephemera.podshell.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeds")
data class Feed(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val url: String,
)