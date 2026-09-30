package com.moneo.app.ai.parser

import com.moneo.app.domain.model.Category

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rule-based classifier that maps merchant names and keywords to categories.
 * Ordered by specificity — more specific merchants first.
 */
@Singleton
class MerchantCategoryClassifier @Inject constructor() {

    private val merchantMap: Map<String, Category> = buildMap {
        // Transport
        put("uber", Category.TRANSPORT)
        put("ola", Category.TRANSPORT)
        put("rapido", Category.TRANSPORT)
        put("metro", Category.TRANSPORT)
        put("auto", Category.TRANSPORT)
        put("cab", Category.TRANSPORT)
        put("bus", Category.TRANSPORT)
        put("train", Category.TRANSPORT)
        put("petrol", Category.TRANSPORT)
        put("fuel", Category.TRANSPORT)
        put("irctc", Category.TRANSPORT)
        put("indigo", Category.TRAVEL)
        put("airindia", Category.TRAVEL)
        put("vistara", Category.TRAVEL)

        // Food / Dining
        put("starbucks", Category.FOOD)
        put("mcdonalds", Category.FOOD)
        put("mcdonald's", Category.FOOD)
        put("dominos", Category.FOOD)
        put("domino's", Category.FOOD)
        put("zomato", Category.FOOD)
        put("swiggy", Category.FOOD)
        put("dunzo", Category.FOOD)
        put("blinkit", Category.GROCERIES)
        put("zepto", Category.GROCERIES)
        put("instamart", Category.GROCERIES)
        put("lunch", Category.FOOD)
        put("dinner", Category.FOOD)
        put("breakfast", Category.FOOD)
        put("coffee", Category.FOOD)
        put("tea", Category.FOOD)
        put("chai", Category.FOOD)
        put("restaurant", Category.FOOD)
        put("cafe", Category.FOOD)
        put("pizza", Category.FOOD)
        put("burger", Category.FOOD)

        // Groceries
        put("bigbasket", Category.GROCERIES)
        put("jiomart", Category.GROCERIES)
        put("dmart", Category.GROCERIES)
        put("reliance", Category.GROCERIES)
        put("grocery", Category.GROCERIES)
        put("vegetables", Category.GROCERIES)
        put("fruits", Category.GROCERIES)
        put("supermarket", Category.GROCERIES)

        // Shopping
        put("amazon", Category.SHOPPING)
        put("flipkart", Category.SHOPPING)
        put("myntra", Category.SHOPPING)
        put("ajio", Category.SHOPPING)
        put("zara", Category.SHOPPING)
        put("h&m", Category.SHOPPING)
        put("nike", Category.SHOPPING)
        put("adidas", Category.SHOPPING)
        put("nykaa", Category.SHOPPING)
        put("meesho", Category.SHOPPING)
        put("shopping", Category.SHOPPING)

        // Bills & Utilities
        put("electricity", Category.UTILITIES)
        put("water", Category.UTILITIES)
        put("gas", Category.UTILITIES)
        put("internet", Category.BILLS)
        put("broadband", Category.BILLS)
        put("airtel", Category.BILLS)
        put("jio", Category.BILLS)
        put("vodafone", Category.BILLS)
        put("vi", Category.BILLS)
        put("bsnl", Category.BILLS)
        put("recharge", Category.BILLS)
        put("bill", Category.BILLS)

        // Subscriptions
        put("netflix", Category.SUBSCRIPTIONS)
        put("spotify", Category.SUBSCRIPTIONS)
        put("hotstar", Category.SUBSCRIPTIONS)
        put("disney", Category.SUBSCRIPTIONS)
        put("prime", Category.SUBSCRIPTIONS)
        put("youtube", Category.SUBSCRIPTIONS)
        put("apple", Category.SUBSCRIPTIONS)
        put("google", Category.SUBSCRIPTIONS)

        // Health
        put("pharmacy", Category.HEALTH)
        put("medicine", Category.HEALTH)
        put("doctor", Category.HEALTH)
        put("hospital", Category.HEALTH)
        put("clinic", Category.HEALTH)
        put("gym", Category.HEALTH)
        put("medical", Category.HEALTH)
        put("apollo", Category.HEALTH)
        put("medplus", Category.HEALTH)

        // Education
        put("school", Category.EDUCATION)
        put("college", Category.EDUCATION)
        put("university", Category.EDUCATION)
        put("tuition", Category.EDUCATION)
        put("course", Category.EDUCATION)
        put("udemy", Category.EDUCATION)
        put("coursera", Category.EDUCATION)
        put("book", Category.EDUCATION)
        put("books", Category.EDUCATION)

        // Rent
        put("rent", Category.RENT)
    }

    private val incomeKeywords = setOf(
        "salary", "salaries", "income", "payment", "received", "got",
        "earned", "bonus", "incentive", "commission", "refund",
        "cashback", "dividend", "interest", "freelance", "stipend"
    )

    private val expenseKeywords = setOf(
        "spent", "paid", "bought", "purchased", "spend",
        "expense", "bill", "fee", "charge"
    )

    /**
     * Returns the best-guess category for a given merchant or text.
     */
    fun classify(text: String): Category {
        val lower = text.lowercase()
        return merchantMap.entries.firstOrNull { (key, _) ->
            lower.contains(key)
        }?.value ?: Category.OTHER
    }

    fun isLikelyIncome(text: String): Boolean {
        val lower = text.lowercase()
        return incomeKeywords.any { lower.contains(it) }
    }

    fun isLikelyExpense(text: String): Boolean {
        val lower = text.lowercase()
        return expenseKeywords.any { lower.contains(it) }
    }

    /**
     * Extracts a likely merchant name from text.
     * Returns the first recognized merchant keyword, or null.
     */
    fun extractMerchant(text: String): String? {
        val lower = text.lowercase()
        return merchantMap.keys
            .filter { lower.contains(it) }
            .maxByOrNull { it.length } // prefer longest match
            ?.let { key ->
                // Find the original-cased version in text
                val idx = lower.indexOf(key)
                text.substring(idx, idx + key.length)
                    .replaceFirstChar { it.uppercase() }
            }
    }
}
