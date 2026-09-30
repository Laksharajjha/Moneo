package com.moneo.app.data.mapper

import com.moneo.app.data.local.entity.TransactionEntity
import com.moneo.app.domain.model.Category
import com.moneo.app.domain.model.Transaction
import com.moneo.app.domain.model.TransactionSource
import com.moneo.app.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    amountInPaise = amountInPaise,
    currency = currency,
    type = TransactionType.valueOf(type),
    category = Category.valueOf(category),
    merchant = merchant,
    description = description,
    date = LocalDate.parse(date),
    timestamp = LocalDateTime.parse(timestamp),
    paymentMethod = paymentMethod,
    person = person,
    account = account,
    toAccount = toAccount,
    isRecurring = isRecurring,
    billingCycle = billingCycle,
    notes = notes,
    source = TransactionSource.valueOf(source),
    confidence = confidence,
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt)
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    amountInPaise = amountInPaise,
    currency = currency,
    type = type.name,
    category = category.name,
    merchant = merchant,
    description = description,
    date = date.toString(),
    timestamp = timestamp.toString(),
    paymentMethod = paymentMethod,
    person = person,
    account = account,
    toAccount = toAccount,
    isRecurring = isRecurring,
    billingCycle = billingCycle,
    notes = notes,
    source = source.name,
    confidence = confidence,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
