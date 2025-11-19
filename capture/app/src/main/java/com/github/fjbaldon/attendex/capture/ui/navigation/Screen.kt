package com.github.fjbaldon.attendex.capture.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object EventList : Screen("event_list")
    data object Scanner : Screen("scanner/{eventId}") {
        fun createRoute(eventId: Long) = "scanner/$eventId"
    }

    data object ChangePassword : Screen("change_password")
}
