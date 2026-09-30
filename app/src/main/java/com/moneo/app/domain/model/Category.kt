package com.moneo.app.domain.model

enum class Category(val displayName: String, val emoji: String) {
    FOOD("Food", "\uD83C\uDF7D"),
    GROCERIES("Groceries", "\uD83D\uDED2"),
    TRANSPORT("Transport", "\uD83D\uDE97"),
    SHOPPING("Shopping", "\uD83D\uDED2"),
    ENTERTAINMENT("Entertainment", "\uD83C\uDFAC"),
    BILLS("Bills", "\uD83D\uDCC4"),
    HEALTH("Health", "\uD83D\uDC8A"),
    TRAVEL("Travel", "\u2708"),
    EDUCATION("Education", "\uD83D\uDCDA"),
    SUBSCRIPTIONS("Subscriptions", "\uD83D\uDCF1"),
    RENT("Rent", "\uD83C\uDFE0"),
    UTILITIES("Utilities", "\uD83D\uDCA1"),
    PERSONAL("Personal", "\uD83D\uDC64"),
    SALARY("Salary", "\uD83D\uDCB0"),
    OTHER("Other", "\uD83D\uDCE6")
}
