package com.somila.geekgauge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.somila.geekgauge.presentation.GeekDashboardScreen
import com.somila.geekgauge.presentation.LoginScreen
import com.somila.geekgauge.presentation.TrainerDashboardScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("trainerDashboard") { TrainerDashboardScreen(navController) }
        composable("geekDashboard") { GeekDashboardScreen() }
    }
}
