package com.github.fjbaldon.attendex.capture.ui.navigation

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
import com.github.fjbaldon.attendex.capture.feature.changepassword.ChangePasswordScreen
import com.github.fjbaldon.attendex.capture.feature.eventlist.EventListScreen
import com.github.fjbaldon.attendex.capture.feature.login.LoginScreen
import com.github.fjbaldon.attendex.capture.feature.scanner.ScannerScreen
import com.github.fjbaldon.attendex.capture.feature.splash.SplashScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedInFlow.collectAsState()
    // NEW: Observe this state
    val requirePasswordChange by authViewModel.requirePasswordChange.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(isLoggedIn, requirePasswordChange, currentRoute) {
        // FIX: Only auto-redirect to EventList if password change is NOT required
        if (isLoggedIn && !requirePasswordChange && currentRoute == Screen.Login.route) {
            navController.navigate(Screen.EventList.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }

        // Security check: Kick out users who are on protected screens but logged out
        val publicRoutes = listOf(Screen.Splash.route, Screen.Login.route)
        if (!isLoggedIn && currentRoute !in publicRoutes) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onAuthChecked = { isAuthenticated, needsChange ->
                    val destination = if (isAuthenticated) {
                        if (needsChange) Screen.ChangePassword.route else Screen.EventList.route
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { requireChange ->
                    if (requireChange) {
                        navController.navigate(Screen.ChangePassword.route) {
                            // FIX: Pop Login off the stack so "Back" doesn't go to Login
                            // (which might then auto-forward to EventList in a race condition)
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.EventList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
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
        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateToEvents = {
                    // Success path: Go to app
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(0)
                    }
                },
                onNavigateToLogin = {
                    // Logout path: Go to login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}
