package com.elderephemera.podshell.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val guid: String,
    val feedId: Int,
)