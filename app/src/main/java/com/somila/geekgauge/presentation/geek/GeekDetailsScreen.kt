package com.somila.geekgauge.presentation.geek

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.somila.geekgauge.R
import com.somila.geekgauge.domain.models.Cohort
import com.somila.geekgauge.domain.models.User
import com.somila.geekgauge.presentation.dashboard.AppScaffold
import com.somila.geekgauge.presentation.dashboard.DashboardSearchBar
import com.somila.geekgauge.presentation.dashboard.SectionLabel
import com.somila.geekgauge.ui.theme.accentColor
import com.somila.geekgauge.ui.theme.primaryColor
import com.somila.geekgauge.presentation.geek.GeekDetailViewModel
import com.somila.geekgauge.ui.theme.backgroundColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeekDetailScreen(
    cohortId: String?,
    navController: NavHostController,
    viewModel: GeekDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(cohortId) {
        viewModel.loadCohort(cohortId)
    }

    val cohort by viewModel.cohort.collectAsState()
    val filteredGeeks by viewModel.filteredGeeks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedGeekId by remember { mutableStateOf<String?>(null) }
    var showSessionTypeSheet by remember { mutableStateOf(false) }

    AppScaffold(navController) { padding ->

        cohort?.let { currentCohort ->

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
                    CohortDetailHeader(
                        cohortName = currentCohort.name,
                        programme = currentCohort.programme,
                        startDate = currentCohort.startDate,
                        endDate = currentCohort.endDate,
                        onBack = { navController.popBackStack() }
                    )
                }

                item {
                    DashboardSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) }
                    )
                }

                item { SectionLabel("COHORT OVERVIEW") }

                item { CohortStatRow(cohort = currentCohort) }

                item { SectionLabel("MEMBERS") }

                if (filteredGeeks.isEmpty()) {
                    item {
                        EmptyMembersCard(isFiltered = searchQuery.isNotBlank())
                    }
                } else {
                    items(
                        items = filteredGeeks,
                        key = { it.id }
                    ) { geek ->
                        GeekMemberCard(
                            geek = geek,
                            onStartSession = {
                                selectedGeekId = geek.id
                                showSessionTypeSheet = true
                            },
                            onViewReport = {
                                navController.navigate("reports/${geek.id}")
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
    }

    if (showSessionTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSessionTypeSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Start Session",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(text = "Choose a session type", color = Color.Gray)

                Spacer(Modifier.height(20.dp))

                SessionTypeCard(
                    title = "Check-up",
                    subtitle = "General progress review",
                    icon = Icons.Default.Favorite,
                    onClick = {
                        showSessionTypeSheet = false
                        navController.navigate("session/${selectedGeekId}/CHECKUP")
                    }
                )

                Spacer(Modifier.height(12.dp))

                SessionTypeCard(
                    title = "Technical Evaluation",
                    subtitle = "Skills and competency assessment",
                    icon = Icons.Default.Build,
                    onClick = {
                        showSessionTypeSheet = false
                        navController.navigate("session/${selectedGeekId}/TECHNICAL_EVALUATION")
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CohortDetailHeader(
    cohortName: String,
    programme: String,
    startDate: String,
    endDate: String,
    onBack: () -> Unit
) {

    val inputFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    val outputFormatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy")
    }

    val formattedStart = remember(startDate) {
        runCatching {
            LocalDate.parse(startDate, inputFormatter)
                .format(outputFormatter)
        }.getOrDefault(startDate)
    }

    val formattedEnd = remember(endDate) {
        runCatching {
            LocalDate.parse(endDate, inputFormatter)
                .format(outputFormatter)
        }.getOrDefault(endDate)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(Modifier.width(4.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = cohortName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = programme,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "$formattedStart  •  $formattedEnd",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun CohortStatRow(cohort: Cohort) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CohortStatChip(
            label = "Members",
            value = cohort.geeks.size.toString(),
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )
        CohortStatChip(
            label = "Sessions",
            value = "0",
            icon = Icons.Default.PlayArrow,
            modifier = Modifier.weight(1f)
        )
        CohortStatChip(
            label = "Reports",
            value = "0",
            icon = Icons.Default.DateRange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CohortStatChip(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, primaryColor),
        colors = CardDefaults.cardColors(containerColor = primaryColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )
            Text(
                text = label,
                color = backgroundColor,
                fontSize = 11.sp
            )
        }
    }
}



@Composable
private fun GeekMemberCard(
    geek: User,
    onStartSession: () -> Unit,
    onViewReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, primaryColor),
        colors = CardDefaults.cardColors(containerColor = primaryColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                // ── Avatar ──
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${geek.firstName.first()}${geek.lastName.first()}",
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.width(14.dp))

                // ── Name + Email ──
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${geek.firstName} ${geek.lastName}",
                        fontWeight = FontWeight.SemiBold,
                        color = backgroundColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = geek.email,
                        color = backgroundColor.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onStartSession,
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                primaryColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Session",
                            tint = backgroundColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = onViewReport,
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                primaryColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Reports",
                            tint = backgroundColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                }
        }
    }
}


@Composable
private fun EmptyMembersCard(isFiltered: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, primaryColor),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.4f),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = if (isFiltered) "No members match your search"
                else "No members in this cohort",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SessionTypeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}