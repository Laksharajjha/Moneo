package com.moneo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inbox_events")
data class InboxEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amountInPaise: Long,
    val currency: String,
    val type: String,
    val category: String,
    val merchant: String?,
    val date: String,
    val sourcePackage: String,
    val confidence: Float,
    val rawText: String, // Temporarily kept to allow the user to see what matched
    val timestamp: Long
)
