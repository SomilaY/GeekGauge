package com.somila.geekgauge.presentation.transcript

import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.somila.geekgauge.ui.theme.accentColor

sealed class TranscriptEditorUiState {
    object Loading : TranscriptEditorUiState()
    data class Editing(
        val sessionId: String,
        val rawText: String,
        val editedText: String,
        val hasUnsavedChanges: Boolean = false
    ) : TranscriptEditorUiState()
    data class Saved(val sessionId: String) : TranscriptEditorUiState()
    data class Error(val message: String) : TranscriptEditorUiState()
}

@Composable
fun TranscriptEditorScreen(
    sessionId: String?,
    navController: NavHostController,
    viewModel: TranscriptEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val reportState by viewModel.reportGenerationState.collectAsState()

    // Load transcript when screen opens
    LaunchedEffect(sessionId) {
        sessionId?.let { viewModel.loadTranscript(it) }
    }

    // Navigate to report screen when generation succeeds
    LaunchedEffect(reportState) {
        if (reportState is ReportGenerationState.Success) {
            val reportId = (reportState as ReportGenerationState.Success).reportId
            navController.navigate("report/$sessionId") {
                popUpTo("transcript/$sessionId") { inclusive = true }
            }
            viewModel.resetReportState()
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
            is TranscriptEditorUiState.Loading -> {
                LoadingScreen()
            }

            is TranscriptEditorUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onBack = { navController.popBackStack() }
                )
            }

            is TranscriptEditorUiState.Editing -> {
                EditingScreen(
                    state = state,
                    reportState = reportState,
                    onTextChanged = { viewModel.onEditedTextChanged(it) },
                    onSave = { viewModel.saveTranscript() },
                    onGenerateReport = { viewModel.generateReport(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            is TranscriptEditorUiState.Saved -> {
                // Brief saved confirmation — stays on editing screen
            }
        }
    }
}

@Composable
private fun EditingScreen(
    state: TranscriptEditorUiState.Editing,
    reportState: ReportGenerationState,
    onTextChanged: (String) -> Unit,
    onSave: () -> Unit,
    onGenerateReport: (String) -> Unit,
    onBack: () -> Unit
) {
    var geekName by remember { mutableStateOf("") }
    var showGeekNameDialog by remember { mutableStateOf(false) }
    var showRawTranscript by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val isGenerating = reportState is ReportGenerationState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        // ── Header ──
        TranscriptHeader(
            hasUnsavedChanges = state.hasUnsavedChanges,
            onBack = onBack,
            onSave = onSave
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(8.dp))

            // ── Info banner ──
            InfoBanner()

            Spacer(Modifier.height(20.dp))

            // ── Raw transcript toggle ──
            RawTranscriptToggle(
                rawText = state.rawText,
                isVisible = showRawTranscript,
                onToggle = { showRawTranscript = !showRawTranscript }
            )

            Spacer(Modifier.height(16.dp))

            // ── Editable transcript ──
            EditableTranscriptField(
                text = state.editedText,
                onTextChanged = onTextChanged,
                enabled = !isGenerating
            )

            // ── Report generation error ──
            AnimatedVisibility(visible = reportState is ReportGenerationState.Error) {
                val errorMsg = (reportState as? ReportGenerationState.Error)?.message ?: ""
                ErrorBanner(message = errorMsg)
            }

            Spacer(Modifier.height(100.dp))
        }

        // ── Bottom action bar ──
        BottomActionBar(
            isGenerating = isGenerating,
            hasTranscript = state.editedText.isNotBlank(),
            onGenerateReport = {
                if (geekName.isBlank()) {
                    showGeekNameDialog = true
                } else {
                    onGenerateReport(geekName)
                }
            }
        )
    }

    // ── Geek name dialog ──
    if (showGeekNameDialog) {
        GeekNameDialog(
            onConfirm = { name ->
                geekName = name
                showGeekNameDialog = false
                onGenerateReport(name)
            },
            onDismiss = { showGeekNameDialog = false }
        )
    }
}

@Composable
private fun TranscriptHeader(
    hasUnsavedChanges: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Session Transcript",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            AnimatedVisibility(visible = hasUnsavedChanges) {
                Text(
                    text = "Unsaved changes",
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Save button — only visible when there are changes
        AnimatedVisibility(
            visible = hasUnsavedChanges,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TextButton(onClick = onSave) {
                Text(
                    text = "Save",
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Invisible placeholder to keep title centred when no save button
        if (!hasUnsavedChanges) {
            Spacer(Modifier.width(64.dp))
        }
    }
}

@Composable
private fun InfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Review and edit the transcript before generating your AI report. " +
                    "Corrections improve report accuracy.",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun RawTranscriptToggle(
    rawText: String,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    Column {
        TextButton(
            onClick = onToggle,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = if (isVisible)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isVisible) "Hide original" else "Show original transcript",
                color = accentColor,
                style = MaterialTheme.typography.bodySmall
            )
        }

        AnimatedVisibility(visible = isVisible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "ORIGINAL",
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = rawText.ifBlank { "No original transcript available." },
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun EditableTranscriptField(
    text: String,
    onTextChanged: (String) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = "EDITED TRANSCRIPT",
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(
                    1.dp,
                    if (enabled) accentColor.copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(20.dp)
                )
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChanged,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .defaultMinSize(minHeight = 200.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    lineHeight = 24.sp
                ),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "Transcript will appear here...",
                            color = Color.White.copy(alpha = 0.3f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size} words",
            color = Color.White.copy(alpha = 0.4f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun BottomActionBar(
    isGenerating: Boolean,
    hasTranscript: Boolean,
    onGenerateReport: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF0B0F0D).copy(alpha = 0.95f)
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onGenerateReport,
            enabled = hasTranscript && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                disabledContainerColor = accentColor.copy(alpha = 0.3f)
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Generating Report...",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Generate AI Report",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun GeekNameDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2820),
        title = {
            Text(
                text = "Learner Name",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the learner's name for the report.",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            "e.g. Thandeka Nkosi",
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = accentColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Generate", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = Color.Red.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            color = Color.Red.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = accentColor)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Loading transcript...",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorScreen(
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Go Back", color = Color.White)
            }
        }
    }
}