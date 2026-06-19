package com.somila.geekgauge.presentation.geek

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.somila.geekgauge.domain.enums.ConfidenceLevel
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.ui.theme.accentColor
import com.somila.geekgauge.ui.theme.backgroundColor
import com.somila.geekgauge.ui.theme.primaryColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GeekDashboardScreen(
    navController: NavHostController,
    viewModel: GeekDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF7F8F6)
    ) { padding ->

        when (val state = uiState) {
            is GeekDashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }

            is GeekDashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = state.message,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is GeekDashboardUiState.Success -> {
                GeekDashboardContent(
                    state = state,
                    padding = padding,
                    onReportClick = { report ->
                        navController.navigate("report/${report.sessionId}")
                    }
                )
            }
        }
    }
}

@Composable
private fun GeekDashboardContent(
    state: GeekDashboardUiState.Success,
    padding: PaddingValues,
    onReportClick: (Report) -> Unit
) {
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

        // ── Header ──
        item {
            GeekDashboardHeader(geekName = state.geekName)
        }

        // ── Stats ──
        item {
            SectionLabel("MY PROGRESS")
        }

        item {
            GeekStatsRow(reports = state.reports)
        }

        // ── Reports ──
        item {
            SectionLabel("MY REPORTS")
        }

        if (state.reports.isEmpty()) {
            item {
                EmptyReportsCard()
            }
        } else {
            items(
                items = state.reports,
                key = { it.id }
            ) { report ->
                GeekReportCard(
                    report = report,
                    onClick = { onReportClick(report) }
                )
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

// ── Header ──────────────────────────────────────────────────

@Composable
private fun GeekDashboardHeader(geekName: String) {
    val firstName = geekName.split(" ").firstOrNull() ?: geekName
    val initials = geekName.split(" ")
        .take(2)
        .joinToString("") { it.first().toString() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, $firstName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = primaryColor
            )
            Text(
                text = "Here's your progress overview",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = backgroundColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// ── Stats row ────────────────────────────────────────────────

@Composable
private fun GeekStatsRow(reports: List<Report>) {
    val totalSessions = reports.size
    val highConfidenceTopics = reports
        .flatMap { it.topics }
        .count { it.confidence == ConfidenceLevel.HIGH }
    val mustDo = reports
        .flatMap { it.recommendations }
        .count { it.priority.name == "MUST" }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GeekStatChip(
            label = "Sessions",
            value = totalSessions.toString(),
            icon = Icons.Default.PlayArrow,
            modifier = Modifier.weight(1f)
        )
        GeekStatChip(
            label = "Strong Topics",
            value = highConfidenceTopics.toString(),
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f)
        )
        GeekStatChip(
            label = "Actions",
            value = mustDo.toString(),
            icon = Icons.Default.Warning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GeekStatChip(
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

// ── Report card ──────────────────────────────────────────────

@Composable
private fun GeekReportCard(
    report: Report,
    onClick: () -> Unit
) {
    val dateString = remember(report.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(report.createdAt))
    }

    val topicCount = report.topics.size
    val highCount = report.topics.count { it.confidence == ConfidenceLevel.HIGH }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        border = BorderStroke(1.dp, primaryColor),
        colors = CardDefaults.cardColors(containerColor = primaryColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Top row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(backgroundColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = backgroundColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = dateString,
                            fontWeight = FontWeight.SemiBold,
                            color = backgroundColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "$topicCount topics covered",
                            color = backgroundColor.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Session type badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = backgroundColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (report.sessionId.isNotBlank()) "CHECK-UP" else "EVAL",
                        color = backgroundColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(
                color = backgroundColor.copy(alpha = 0.15f),
                thickness = 0.5.dp
            )

            Spacer(Modifier.height(12.dp))

            // ── Summary ──
            Text(
                text = report.summary,
                color = backgroundColor.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            // ── Topic confidence pills ──
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                report.topics.take(3).forEach { topic ->
                    val pillColor = when (topic.confidence) {
                        ConfidenceLevel.HIGH -> Color(0xFF4CAF7D)
                        ConfidenceLevel.MEDIUM -> Color(0xFFFFB74D)
                        ConfidenceLevel.LOW -> Color(0xFFEF5350)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = pillColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = topic.name,
                            color = pillColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                }

                if (report.topics.size > 3) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = backgroundColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "+${report.topics.size - 3}",
                            color = backgroundColor,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── View report CTA ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View full report",
                    color = backgroundColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Empty state ──────────────────────────────────────────────

@Composable
private fun EmptyReportsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = primaryColor.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No reports yet",
                fontWeight = FontWeight.SemiBold,
                color = primaryColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Your evaluation reports will appear here after your trainer completes a session with you.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}