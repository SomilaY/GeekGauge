package com.somila.geekgauge.presentation.report

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.somila.geekgauge.domain.enums.ConfidenceLevel
import com.somila.geekgauge.domain.enums.RecommendationPriority
import com.somila.geekgauge.domain.models.Recommendation
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.models.Topic
import com.somila.geekgauge.ui.theme.accentColor
import java.io.File

sealed class ReportUiState {
    object Loading : ReportUiState()
    data class Success(val report: Report) : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}


@Composable
fun ReportScreen(
    sessionId: String?,
    navController: NavHostController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sessionId) {
        sessionId?.let { viewModel.loadReport(it) }
    }

    // Handle export success — open share sheet
    LaunchedEffect(exportState) {
        if (exportState is ExportState.Success) {
            val file = (exportState as ExportState.Success).file
            shareReportPdf(context, file)
            viewModel.resetExportState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0F0D),
                        Color(0xFF16211C),
                        Color(0xFF0B0F0D)
                    )
                )
            )
    ) {
        when (val state = uiState) {
            is ReportUiState.Loading -> ReportLoadingScreen()
            is ReportUiState.Error -> ReportErrorScreen(
                message = state.message,
                onBack = { navController.popBackStack() }
            )
            is ReportUiState.Success -> ReportContent(
                report = state.report,
                exportState = exportState,
                onBack = { navController.popBackStack() },
                onExport = { sessionId?.let { viewModel.exportPdf(it) } },
                onSaveNotes = { notes ->
                    sessionId?.let { viewModel.saveManualNotes(it, notes) }
                }
            )
        }
    }
}

@Composable
private fun ReportContent(
    report: Report,
    exportState: ExportState,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onSaveNotes: (String) -> Unit
) {
    var manualNotes by remember { mutableStateOf(report.manualNotes) }
    var notesExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isExporting = exportState is ExportState.Exporting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // ── Header ──
        ReportHeader(
            isExporting = isExporting,
            onBack = onBack,
            onExport = onExport
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Summary ──
            ReportSection(title = "Summary") {
                Text(
                    text = report.summary,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }

            // ── Topics ──
            ReportSection(title = "Topics Covered") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.topics.forEach { topic ->
                        TopicChip(topic = topic)
                    }
                }
            }

            // ── Feedback ──
            ReportSection(title = "Feedback") {
                Text(
                    text = report.feedback,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }

            // ── Recommendations ──
            ReportSection(title = "Recommendations") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    report.recommendations.forEach { rec ->
                        RecommendationCard(recommendation = rec)
                    }
                }
            }

            // ── Manual Notes ──
            ReportSection(
                title = "Trainer Notes",
                trailing = {
                    TextButton(
                        onClick = { notesExpanded = !notesExpanded },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (notesExpanded) "Done" else "Edit",
                            color = accentColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            ) {
                AnimatedVisibility(
                    visible = !notesExpanded,
                    enter = fadeIn()
                ) {
                    Text(
                        text = manualNotes.ifBlank { "Tap Edit to add notes..." },
                        color = if (manualNotes.isBlank())
                            Color.White.copy(alpha = 0.3f)
                        else
                            Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }

                AnimatedVisibility(
                    visible = notesExpanded,
                    enter = expandVertically() + fadeIn()
                ) {
                    Column {
                        OutlinedTextField(
                            value = manualNotes,
                            onValueChange = { manualNotes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 120.dp),
                            placeholder = {
                                Text(
                                    "Add your observations...",
                                    color = Color.White.copy(alpha = 0.3f)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = accentColor
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onSaveNotes(manualNotes)
                                notesExpanded = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            )
                        ) {
                            Text("Save Notes", color = Color.White)
                        }
                    }
                }
            }

            // ── Export error ──
            AnimatedVisibility(visible = exportState is ExportState.Error) {
                val msg = (exportState as? ExportState.Error)?.message ?: ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Red.copy(alpha = 0.15f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = msg,
                        color = Color.Red.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportHeader(
    isExporting: Boolean,
    onBack: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Evaluation Report",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.weight(1f))

        // Export button
        IconButton(
            onClick = onExport,
            enabled = !isExporting
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = accentColor,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Export PDF",
                    tint = accentColor
                )
            }
        }
    }
}

@Composable
private fun ReportSection(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                color = accentColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            trailing?.invoke()
        }

        Spacer(Modifier.height(12.dp))

        content()
    }
}

@Composable
private fun TopicChip(topic: Topic) {
    val (chipColor, textColor) = when (topic.confidence) {
        ConfidenceLevel.HIGH -> Pair(
            Color(0xFF1A4D2E),
            Color(0xFF4CAF7D)
        )
        ConfidenceLevel.MEDIUM -> Pair(
            Color(0xFF3D3010),
            Color(0xFFFFB74D)
        )
        ConfidenceLevel.LOW -> Pair(
            Color(0xFF3D1010),
            Color(0xFFEF5350)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(chipColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = topic.name,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = textColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = topic.confidence.name,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun RecommendationCard(recommendation: Recommendation) {
    val (borderColor, labelColor, label) = when (recommendation.priority) {
        RecommendationPriority.MUST -> Triple(
            Color(0xFFEF5350),
            Color(0xFFEF5350),
            "MUST"
        )
        RecommendationPriority.SHOULD -> Triple(
            Color(0xFFFFB74D),
            Color(0xFFFFB74D),
            "SHOULD"
        )
        RecommendationPriority.GOOD -> Triple(
            Color(0xFF4CAF7D),
            Color(0xFF4CAF7D),
            "GOOD"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                borderColor.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Priority dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(labelColor, CircleShape)
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = labelColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = label,
                    color = labelColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = recommendation.action,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )

        if (recommendation.resource.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = recommendation.resource,
                    color = accentColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ReportLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = accentColor)
            Text(
                text = "Loading report...",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ReportErrorScreen(
    message: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = message,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Go Back", color = Color.White)
            }
        }
    }
}

private fun shareReportPdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report"))
}