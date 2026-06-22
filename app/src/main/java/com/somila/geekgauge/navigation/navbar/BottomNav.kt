package com.somila.geekgauge.navigation.navbar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.somila.geekgauge.presentation.geek.GeekDetailScreen
import com.somila.geekgauge.presentation.dashboard.TrainerDashboardScreen
import com.somila.geekgauge.ui.theme.backgroundColor
import com.somila.geekgauge.ui.theme.primaryColor

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RootScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route
    ) {
        composable(BottomNavItem.Home.route) {
            TrainerDashboardScreen(navController)
        }

        composable("cohort/{cohortId}") { backStackEntry ->
            val cohortId = backStackEntry.arguments?.getString("cohortId")
            GeekDetailScreen(cohortId, navController)
        }
    }
}



@Composable
fun FloatingBottomNav(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Cohorts,
        BottomNavItem.Settings
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(65.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = primaryColor.copy(alpha = 0.95f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )

                        AnimatedVisibility(visible = selected) {
                            Text(
                                text = item.label,
                                color = backgroundColor,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
