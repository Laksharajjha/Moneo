package com.moneo.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moneo.app.presentation.ask.AskMoneoScreen
import com.moneo.app.presentation.capture.CaptureScreen
import com.moneo.app.presentation.components.MoneoBottomBar
import com.moneo.app.presentation.home.HomeScreen
import com.moneo.app.presentation.insights.InsightsScreen
import com.moneo.app.presentation.settings.SettingsScreen

@Composable
fun MoneoNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        NavRoutes.Home.route,
        NavRoutes.Activity.route,
        NavRoutes.Ask.route,
        NavRoutes.Insights.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes
    val showFab = currentRoute in setOf(NavRoutes.Home.route, NavRoutes.Activity.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MoneoBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(NavRoutes.Capture.route) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = { Icon(Icons.Outlined.Mic, contentDescription = "Tell Moneo") },
                    text = { Text("Tell Moneo") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen(
                    onNavigateToCapture = { navController.navigate(NavRoutes.Capture.route) },
                    onNavigateToTransaction = { id ->
                        navController.navigate(NavRoutes.TransactionDetail.createRoute(id))
                    },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) }
                )
            }
            composable(NavRoutes.Activity.route) {
                com.moneo.app.presentation.activity.ActivityScreen(
                    onNavigateToTransaction = { id ->
                        navController.navigate(NavRoutes.TransactionDetail.createRoute(id))
                    }
                )
            }
            composable(NavRoutes.Capture.route) {
                CaptureScreen(
                    onDismiss = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Ask.route) {
                AskMoneoScreen()
            }
            composable(NavRoutes.Insights.route) {
                InsightsScreen()
            }
            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = NavRoutes.TransactionDetail.route,
                arguments = listOf(androidx.navigation.navArgument("id") {
                    type = androidx.navigation.NavType.StringType
                })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                com.moneo.app.presentation.detail.TransactionDetailScreen(
                    transactionId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
