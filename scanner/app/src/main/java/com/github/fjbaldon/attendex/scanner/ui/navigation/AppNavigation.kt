package com.github.fjbaldon.attendex.scanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.fjbaldon.attendex.scanner.ui.screens.eventlist.EventListScreen
import com.github.fjbaldon.attendex.scanner.ui.screens.login.LoginScreen
import com.github.fjbaldon.attendex.scanner.ui.screens.scanner.ScannerScreen
import com.github.fjbaldon.attendex.scanner.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onAuthChecked = { isLoggedIn ->
                    val destination = if (isLoggedIn) Screen.EventList.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                navToEventList = {
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.EventList.route) {
            EventListScreen(
                onEventSelected = { eventId ->
                    navController.navigate(Screen.Scanner.createRoute(eventId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.EventList.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Scanner.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) { backStackEntry ->
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
