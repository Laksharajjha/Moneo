package com.moneo.app.navigation

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Activity : NavRoutes("activity")
    data object Capture : NavRoutes("capture")
    data object Insights : NavRoutes("insights")
    data object Settings : NavRoutes("settings")
    data object Ask : NavRoutes("ask")
    data object AddTransaction : NavRoutes("add_transaction")
    data object TransactionDetail : NavRoutes("transaction/{id}") {
        fun createRoute(id: Long) = "transaction/$id"
    }
}
