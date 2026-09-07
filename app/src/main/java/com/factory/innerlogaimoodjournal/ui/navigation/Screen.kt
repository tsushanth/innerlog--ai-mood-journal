package com.factory.innerlogaimoodjournal.ui.navigation

sealed class Screen(val route: String) {
    object Lock : Screen("lock")
    object SetupPin : Screen("setup_pin")
    object Home : Screen("home")
    object NewEntry : Screen("new_entry?entryId={entryId}") {
        fun createRoute(entryId: Long? = null) = "new_entry?entryId=${entryId ?: -1}"
    }
    object JournalDetail : Screen("journal_detail/{entryId}") {
        fun createRoute(entryId: Long) = "journal_detail/$entryId"
    }
    object Habits : Screen("habits")
    object Goals : Screen("goals")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
    object Paywall : Screen("paywall")
}
