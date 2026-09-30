package com.moneo.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.moneo.app.navigation.NavRoutes

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoutes.Home.route,
        label = "Home",
        icon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = NavRoutes.Activity.route,
        label = "Activity",
        icon = Icons.AutoMirrored.Outlined.List
    ),
    BottomNavItem(
        route = NavRoutes.Ask.route,
        label = "Ask",
        icon = Icons.Outlined.AutoAwesome
    ),
    BottomNavItem(
        route = NavRoutes.Insights.route,
        label = "Insights",
        icon = Icons.Outlined.BarChart
    )
)

@Composable
fun MoneoBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = androidx.compose.ui.unit.Dp.Hairline
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
