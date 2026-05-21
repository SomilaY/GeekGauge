package com.somila.geekgauge.navigation.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {

    object Home : BottomNavItem(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home
    )

    object Cohorts : BottomNavItem(
        route = "cohorts",
        label = "Cohorts",
        icon = Icons.Default.Face
    )

    object Profile : BottomNavItem(
        route = "profile",
        label = "Profile",
        icon = Icons.Default.Person
    )
}