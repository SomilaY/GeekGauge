package com.somila.geekgauge.presentation.dashboard

import android.R
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.somila.geekgauge.domain.models.Cohort
import com.somila.geekgauge.navigation.navbar.FloatingBottomNav
import com.somila.geekgauge.ui.theme.backgroundColor
import com.somila.geekgauge.ui.theme.primaryColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun AppScaffold(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit
) {

    Scaffold(
        containerColor = Color.Transparent,

        bottomBar = {
            FloatingBottomNav(navController)
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {

            content(padding)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrainerDashboardScreen(
    navController: NavHostController,
    viewModel: TrainerDashboardViewModel = hiltViewModel()
) {

    val cohorts by viewModel.cohorts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val formatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    val today = remember {
        LocalDate.now()
    }

    val stats = remember(cohorts) {

        listOf(
            DashboardStat(
                title = "Active Cohorts",
                value = cohorts.size.toString(),
                subtitle = "On track",
                icon = Icons.Default.KeyboardArrowUp
            ),
            DashboardStat(
                title = "Total Geeks",
                value = cohorts.sumOf { it.geeks.size }.toString(),
                subtitle = "${if (cohorts.isEmpty()) 0 else cohorts.sumOf { it.geeks.size } / cohorts.size} per cohort",
                icon = Icons.Default.Person
            ),
            DashboardStat(
                title = "Sessions",
                value = "0",
                subtitle = "This week",
                icon = Icons.Default.DateRange
            ),
            DashboardStat(
                title = "Reports",
                value = "--",
                subtitle = "Pending",
                icon = Icons.Default.Email
            )
        )
    }

    Scaffold(
        containerColor = Color(0xFFF7F8F6),

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp
            ),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                DashboardHeader()
            }

            item {

                DashboardSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.onSearchQueryChanged(it)
                    }
                )
            }

            item {

                SectionLabel("OVERVIEW")
            }

            item {

                StatsGrid(stats)
            }

            item {

                SectionLabel("RECENT REPORTS")
            }

            item {

                ReportCard(
                    title = "Kabelo Mokoena — Check-up",
                    subtitle = "Android Beginners • 2 days ago",
                    badge = "Mobile Dev"
                )
            }

            item {

                SectionLabel("COHORT PROGRESS")
            }

            items(cohorts) { cohort ->

                val start =
                    LocalDate.parse(
                        cohort.startDate,
                        formatter
                    )

                val end =
                    LocalDate.parse(
                        cohort.endDate,
                        formatter
                    )

                val totalDays =
                    ChronoUnit.DAYS.between(
                        start,
                        end
                    ).toFloat()

                val elapsed =
                    ChronoUnit.DAYS.between(
                        start,
                        today
                    )
                        .coerceAtLeast(0)
                        .toFloat()

                val progress =
                    if (totalDays > 0)
                        (elapsed / totalDays)
                            .coerceIn(0f, 1f)
                    else
                        1f

                CohortProgressCard(
                    cohort = cohort,
                    progress = progress,
                    onClick = {
                        navController.navigate(
                            "cohort/${cohort.id}"
                        )
                    }
                )
            }

            item {

                SectionLabel("THIS WEEK")
            }

            item {

                WeekStrip()
            }

            item {

                Spacer(
                    modifier = Modifier.height(
                        80.dp
                    )
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardHeader() {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                "Good Morning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                LocalDate.now().format(
                    DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy"
                    )
                ),
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {

            Text(
                "SY",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DashboardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {

    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            primaryColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search course or geek...")
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null)
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

data class DashboardStat(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun StatsGrid(
    stats: List<DashboardStat>
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        userScrollEnabled = false,
        modifier = Modifier.height(250.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        items(stats) { stat ->

            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.dp,
                    primaryColor
                ),
                colors = CardDefaults.cardColors(
                    containerColor = primaryColor
                )
            ) {

                Column(
                    modifier = Modifier.padding(14.dp)
                ) {

                    Text(
                        stat.title,
                        color = backgroundColor,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        stat.value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = backgroundColor
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            stat.icon,
                            null,
                            tint = backgroundColor,
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(
                            Modifier.width(4.dp)
                        )

                        Text(
                            stat.subtitle,
                            color = backgroundColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    subtitle: String,
    badge: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor
        ),
        border = BorderStroke(1.dp, primaryColor),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor.copy(alpha = .1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = backgroundColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = backgroundColor
                )
                Text(
                    text = subtitle,
                    color = backgroundColor,
                    fontSize = 12.sp
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = badge,
                        color = primaryColor
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = backgroundColor
                )
            )
        }
    }
}


@Composable
fun CohortProgressCard(
    cohort: Cohort,
    progress: Float,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = primaryColor
        ),
        border = BorderStroke(1.dp, primaryColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(backgroundColor)
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (expanded) "-" else "+",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cohort.name,
                        fontWeight = FontWeight.SemiBold,
                        color = backgroundColor
                    )
                    Text(
                        text = "${cohort.programme} • ${cohort.geeks.size} geeks",
                        color = backgroundColor,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = backgroundColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // 🔹 Expandable content
            if (expanded) {
                Spacer(Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = backgroundColor,
                    trackColor = primaryColor
                )

                Spacer(Modifier.height(12.dp))

                cohort.geeks.take(3).forEach { geek ->
                    GeekRow(name = geek.firstName)
                }
            }
        }
    }
}



@Composable
fun GeekRow(
    name: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(
                    backgroundColor
                ),

            contentAlignment = Alignment.Center
        ) {

            Text(
                name.split(" ")
                    .take(2)
                    .joinToString("") {
                        it.first().toString()
                    },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }

        Spacer(Modifier.width(10.dp))

        Column {

            Text(
                name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = backgroundColor
            )

            Text(
                "Last session recently",
                fontSize = 11.sp,
                color = backgroundColor
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray,
        letterSpacing = 1.sp
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekStrip() {

    val today = LocalDate.now()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor
        ),
        border = BorderStroke(
            1.dp,
            primaryColor
        ),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            horizontalArrangement =
                Arrangement.SpaceEvenly
        ) {

            repeat(7) { index ->

                val monday =
                    today.minusDays(
                        (today.dayOfWeek.value - 1).toLong()
                    )

                val date =
                    monday.plusDays(
                        index.toLong()
                    )

                val isToday =
                    date == today

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        date.dayOfWeek
                            .name
                            .take(3),
                        color = backgroundColor

                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isToday)
                                    backgroundColor
                                else
                                    Color.Transparent
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            date.dayOfMonth.toString(),
                            color =
                                if (isToday)
                                    primaryColor
                                else
                                    backgroundColor
                        )
                    }
                }
            }
        }
    }
}








