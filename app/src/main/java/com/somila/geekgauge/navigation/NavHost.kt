package com.somila.geekgauge.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.somila.geekgauge.presentation.auth.LoginScreen
import com.somila.geekgauge.presentation.dashboard.TrainerDashboardScreen
import com.somila.geekgauge.presentation.geek.GeekDetailScreen
import com.somila.geekgauge.presentation.geek.GeekDashboardScreen
import com.somila.geekgauge.navigation.navbar.BottomNavItem
import com.somila.geekgauge.presentation.report.ReportScreen
import com.somila.geekgauge.presentation.session.SessionScreen
import com.somila.geekgauge.presentation.settings.SettingsScreen
import com.somila.geekgauge.presentation.transcript.TranscriptEditorScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController)
        }

        composable(BottomNavItem.Home.route) {
            TrainerDashboardScreen(navController)
        }

        composable("trainerDashboard") {
            TrainerDashboardScreen(navController)
        }

        composable("geekDashboard") {
           // GeekDashboardScreen(navController)
        }

        composable("settings") {
            SettingsScreen(navController)
        }

        composable("cohort/{cohortId}") { backStackEntry ->
            val cohortId = backStackEntry.arguments?.getString("cohortId")
            GeekDetailScreen(cohortId, navController)
        }

        composable("session/{geekId}/{sessionType}") { backStackEntry ->
            val geekId = backStackEntry.arguments?.getString("geekId")
            val sessionType = backStackEntry.arguments?.getString("sessionType")
            SessionScreen(geekId, sessionType, navController)
        }

        composable("transcript/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            TranscriptEditorScreen(sessionId, navController)
        }

        composable("report/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            ReportScreen(sessionId, navController)
        }

        composable("geekDashboard") {
            GeekDashboardScreen(navController)
        }

        composable("reports/{geekId}") { backStackEntry ->
            val geekId = backStackEntry.arguments?.getString("geekId")
            // For now navigate to geek dashboard
            // Later: ReportHistoryScreen(geekId, navController)
            GeekDashboardScreen(navController)
        }
    }
}