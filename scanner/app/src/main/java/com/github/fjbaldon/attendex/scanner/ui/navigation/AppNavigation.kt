package com.github.fjbaldon.attendex.scanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.fjbaldon.attendex.scanner.ui.screens.eventlist.EventListScreen
import com.github.fjbaldon.attendex.scanner.ui.screens.login.LoginScreen
import com.github.fjbaldon.attendex.scanner.ui.screens.scanner.ScannerScreen
import com.github.fjbaldon.attendex.scanner.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val isLoggedIn by authViewModel.isLoggedInFlow.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onAuthChecked = { isAuthenticated ->
                    val destination =
                        if (isAuthenticated) Screen.EventList.route else Screen.Login.route
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
                        popUpTo(0)
                    }
                }
            )
        }
        composable(
            route = Screen.Scanner.route,
            arguments = listOf(navArgument("eventId") { type = NavType.LongType })
        ) {
            ScannerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
