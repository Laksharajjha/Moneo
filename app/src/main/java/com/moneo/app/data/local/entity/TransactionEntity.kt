package com.moneo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amountInPaise: Long,
    val currency: String,
    val type: String,           // TransactionType.name
    val category: String,       // Category.name
    val merchant: String?,
    val description: String?,
    val date: String,           // ISO-8601 local date: yyyy-MM-dd
    val timestamp: String,      // ISO-8601 local datetime: yyyy-MM-ddTHH:mm:ss
    val paymentMethod: String?,
    val person: String?,
    val account: String?,
    val toAccount: String?,
    val isRecurring: Boolean,
    val billingCycle: String?,
    val notes: String?,
    val source: String,         // TransactionSource.name
    val confidence: Float,
    val createdAt: String,
    val updatedAt: String
)
