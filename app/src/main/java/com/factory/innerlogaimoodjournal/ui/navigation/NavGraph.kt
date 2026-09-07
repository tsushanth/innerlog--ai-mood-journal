package com.factory.innerlogaimoodjournal.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.factory.innerlogaimoodjournal.ui.components.ProBadge
import com.factory.innerlogaimoodjournal.ui.currentApp
import com.factory.innerlogaimoodjournal.ui.screens.goals.GoalsScreen
import com.factory.innerlogaimoodjournal.ui.screens.habits.HabitsScreen
import com.factory.innerlogaimoodjournal.ui.screens.home.HomeScreen
import com.factory.innerlogaimoodjournal.ui.screens.journal.JournalDetailScreen
import com.factory.innerlogaimoodjournal.ui.screens.journal.NewEntryScreen
import com.factory.innerlogaimoodjournal.ui.screens.lock.LockScreen
import com.factory.innerlogaimoodjournal.ui.screens.lock.SetupPinScreen
import com.factory.innerlogaimoodjournal.ui.screens.paywall.PaywallScreen
import com.factory.innerlogaimoodjournal.ui.screens.settings.SettingsScreen
import com.factory.innerlogaimoodjournal.ui.screens.stats.MoodStatsScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val isPremium: Boolean = false
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Journal", Icons.Filled.Book),
    BottomNavItem(Screen.Habits, "Habits", Icons.Filled.CheckCircle, isPremium = true),
    BottomNavItem(Screen.Goals, "Goals", Icons.Filled.Flag, isPremium = true),
    BottomNavItem(Screen.Insights, "Insights", Icons.Filled.Insights, isPremium = true),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
)

@Composable
fun InnerLogNavGraph(activity: FragmentActivity) {
    val navController = rememberNavController()
    val app = currentApp()
    val lockManager = app.lockManager
    val premiumManager = app.premiumManager
    val isPremium by premiumManager.isPremium.collectAsState()

    val startDestination = when {
        lockManager.isLockEnabled() -> Screen.Lock.route
        !isPremium && !premiumManager.hasSeenPaywall() -> Screen.Paywall.route
        else -> Screen.Home.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                val destination = if (item.isPremium && !isPremium) Screen.Paywall.route else item.screen.route
                                navController.navigate(destination) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Box {
                                    Icon(item.icon, contentDescription = item.label)
                                    if (item.isPremium && !isPremium) {
                                        ProBadge(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(start = 16.dp)
                                        )
                                    }
                                }
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Lock.route) {
                LockScreen(
                    activity = activity,
                    lockManager = lockManager,
                    onUnlocked = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.SetupPin.route) {
                SetupPinScreen(
                    lockManager = lockManager,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onEntryClick = { id -> navController.navigate(Screen.JournalDetail.createRoute(id)) },
                    onNewEntry = { navController.navigate(Screen.NewEntry.createRoute()) }
                )
            }
            composable(
                route = Screen.NewEntry.route,
                arguments = listOf(navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
                NewEntryScreen(
                    entryId = if (entryId == -1L) null else entryId,
                    onDone = { navController.popBackStack() },
                    onUpgrade = { navController.navigate(Screen.Paywall.route) }
                )
            }
            composable(
                route = Screen.JournalDetail.route,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: 0L
                JournalDetailScreen(
                    entryId = entryId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.NewEntry.createRoute(id)) }
                )
            }
            composable(Screen.Habits.route) { HabitsScreen() }
            composable(Screen.Goals.route) { GoalsScreen() }
            composable(Screen.Insights.route) { MoodStatsScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onSetupPin = { navController.navigate(Screen.SetupPin.route) },
                    onUpgrade = { navController.navigate(Screen.Paywall.route) }
                )
            }
            composable(Screen.Paywall.route) {
                PaywallScreen(
                    activity = activity,
                    onClose = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Paywall.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
