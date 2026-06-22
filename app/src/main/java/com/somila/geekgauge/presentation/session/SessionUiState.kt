package com.somila.geekgauge.presentation.session

import kotlinx.coroutines.flow.StateFlow
import android.Manifest
import android.graphics.drawable.Icon
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.somila.geekgauge.domain.enums.SessionType
import com.somila.geekgauge.ui.theme.accentColor
import com.somila.geekgauge.ui.theme.backgroundColor
import com.somila.geekgauge.ui.theme.primaryColor
import kotlin.math.sin

sealed class SessionUiState {
    object Idle : SessionUiState()
    object Preparing : SessionUiState()
    data class Recording(val durationMs: Long, val amplitude: Int) : SessionUiState()
    data class Paused(val durationMs: Long) : SessionUiState()
    object Processing : SessionUiState()
    data class ReadyForTranscription(val audioFilePath: String) : SessionUiState()
    data class Error(val message: String) : SessionUiState()

    data class TranscriptReady(val sessionId: String) : SessionUiState()
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SessionScreen(
    geekId: String?,
    sessionType: String?,
    navController: NavHostController,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    LaunchedEffect(Unit) {
        notificationPermission?.launchPermissionRequest()
    }
    LaunchedEffect(sessionState) {
        when (val state = sessionState) {
            is SessionUiState.TranscriptReady -> {
                navController.navigate("transcript/${state.sessionId}") {
                    popUpTo("session/$geekId/$sessionType") { inclusive = true }
                }
            }
            else -> Unit
        }
    }

    // Request mic permission and start session
    LaunchedEffect(micPermission.status) {
        if (micPermission.status.isGranted) {
            val type = when (sessionType) {
                "TECHNICAL_EVALUATION" -> SessionType.TECHNICAL_EVALUATION
                else -> SessionType.CHECKUP
            }
            if (sessionState is SessionUiState.Idle) {
                viewModel.startSession(
                    geekId = geekId ?: "",
                    trainerId = "t001", // replace with logged-in trainer id
                    sessionType = type
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor,
                        Color.Black
                    )
                )
            )
    ) {
        when {
            !micPermission.status.isGranted -> {
                PermissionScreen(
                    shouldShowRationale = micPermission.status.shouldShowRationale,
                    onRequestPermission = { micPermission.launchPermissionRequest() }
                )
            }

            sessionState is SessionUiState.Error -> {
                ErrorScreen(
                    message = (sessionState as SessionUiState.Error).message,
                    onRetry = {
                        val type = when (sessionType) {
                            "TECHNICAL_EVALUATION" -> SessionType.TECHNICAL_EVALUATION
                            else -> SessionType.CHECKUP
                        }
                        viewModel.startSession(geekId ?: "", "t001", type)
                    }
                )
            }

            else -> {
                SessionContent(
                    sessionState = sessionState,
                    liveTranscript = liveTranscript,
                    sessionType = sessionType,
                    rmsLevel = rmsLevel,
                    onPause = { viewModel.pauseRecording() },
                    onResume = { viewModel.resumeRecording() },
                    onStop = { viewModel.stopRecording() },
                    onBack = { navController.popBackStack() },
                    onEdit = {
                        val state = sessionState
                        if (state is SessionUiState.TranscriptReady) {
                            navController.navigate("transcript/${state.sessionId}")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AudioOrb(
    rmsLevel: Float,
    sessionState: SessionUiState
) {
    val isIdle = sessionState is SessionUiState.Preparing ||
            sessionState is SessionUiState.Idle
    val isRecording = sessionState is SessionUiState.Recording
    val isPaused = sessionState is SessionUiState.Paused
    val isProcessing = sessionState is SessionUiState.Processing

    val isLoud = rmsLevel > 0.6f
    val isSpeaking = rmsLevel > 0.15f

    // ── Breathing animation for idle/paused ──
    val infiniteTransition = rememberInfiniteTransition(label = "orb_idle")

    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val breathGlow by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_glow"
    )

    // ── Outer ring pulse for loud speaking ──
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isLoud) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isLoud) 0.4f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // ── Processing rotation ──
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isProcessing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "processing_rotation"
    )

    // ── Speaking scale driven by rmsLevel ──
    val speakingScale by animateFloatAsState(
        targetValue = when {
            isProcessing -> 1f
            isPaused -> breathScale
            isIdle -> breathScale
            isLoud -> 1f + rmsLevel * 0.55f
            isSpeaking -> 1f + rmsLevel * 0.35f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "speaking_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = when {
            isProcessing -> 0.4f
            isPaused || isIdle -> breathGlow
            isLoud -> 0.3f + rmsLevel * 0.7f
            isSpeaking -> 0.2f + rmsLevel * 0.5f
            else -> 0.2f
        },
        animationSpec = tween(80),
        label = "glow_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // ── Outer pulse ring (loud speaking only) ──
        if (isLoud) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // ── Middle pulse ring (loud speaking only) ──
        if (isLoud) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = pulseScale * 0.9f
                        scaleY = pulseScale * 0.9f
                        alpha = pulseAlpha * 0.6f
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // ── Main glow aura ──
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer {
                    scaleX = speakingScale
                    scaleY = speakingScale
                    if (isProcessing) rotationZ = rotationAngle
                }
                .background(
                    brush = if (isProcessing) {
                        Brush.sweepGradient(
                            colors = listOf(
                                accentColor.copy(alpha = glowAlpha),
                                primaryColor.copy(alpha = glowAlpha * 0.3f),
                                accentColor.copy(alpha = 0f),
                                accentColor.copy(alpha = glowAlpha * 0.5f),
                                accentColor.copy(alpha = glowAlpha),
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = glowAlpha),
                                primaryColor.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    },
                    shape = CircleShape
                )
        )

        // ── Core orb ──
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    scaleX = 1f + rmsLevel * 0.15f
                    scaleY = 1f + rmsLevel * 0.15f
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            backgroundColor,
                            accentColor.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Processing percentage inside core
            if (isProcessing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = backgroundColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "AI",
                        color = backgroundColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionContent(
    sessionState: SessionUiState,
    liveTranscript: String,
    sessionType: String?,
    rmsLevel: Float,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val durationMs = when (sessionState) {
        is SessionUiState.Recording -> sessionState.durationMs
        is SessionUiState.Paused -> sessionState.durationMs
        else -> 0L
    }

    // amplitude extraction removed entirely

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf( accentColor, Color.Black)
                )
            )
    ) {
        FloatingParticles()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .align(Alignment.TopCenter)
        ) {
            SessionHeader(sessionType = sessionType, onBack = onBack)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AudioOrb(
                rmsLevel = rmsLevel,
                sessionState = sessionState
            )

            Spacer(Modifier.height(32.dp))
            ModernTimer(durationMs)
            Spacer(Modifier.height(16.dp))
            RecordingStatus(sessionState)
        }

        AnimatedVisibility(
            visible = liveTranscript.isNotBlank(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 160.dp)
        ) {
            TranscriptGlassCard(transcript = liveTranscript)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            ModernControls(
                sessionState = sessionState,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onEdit = onEdit
            )
        }
    }
}

@Composable
private fun SessionHeader(
    sessionType: String?,
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        GlassCircleButton(
            icon = Icons.Default.ArrowBack,
            onClick = onBack
        )

        Spacer(Modifier.weight(1f))

        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White.copy(alpha = 0.06f)
        ) {
            Text(
                text = if (sessionType == "TECHNICAL_EVALUATION")
                    "Technical Evaluation"
                else
                    "Check-up",
                color = backgroundColor,
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 10.dp
                )
            )
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.width(48.dp))
    }
}

@Composable
fun ModernTimer(durationMs: Long) {

    val hours = durationMs / 3600000
    val minutes = (durationMs % 3600000) / 60000
    val seconds = (durationMs % 60000) / 1000

    Text(
        text =
            if (hours > 0)
                "%02d:%02d:%02d".format(
                    hours,
                    minutes,
                    seconds
                )
            else
                "%02d:%02d".format(
                    minutes,
                    seconds
                ),
        fontSize = 72.sp,
        fontWeight = FontWeight.ExtraLight,
        color = backgroundColor,
        letterSpacing = 6.sp
    )
}

@Composable
fun RecordingStatus(
    state: SessionUiState
) {

    val text =
        when (state) {
            is SessionUiState.Recording ->
                "Listening..."
            is SessionUiState.Paused ->
                "Paused"
            is SessionUiState.Processing ->
                "Processing..."
            else -> ""
        }

    Text(
        text = text,
        color = backgroundColor.copy(alpha = 0.75f),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun TranscriptGlassCard(
    transcript: String
) {

    if (transcript.isBlank()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {

        Text(
            text = transcript,
            color = Color.White,
            modifier = Modifier.padding(18.dp),
            maxLines = 5
        )
    }
}

@Composable
fun ModernControls(
    sessionState: SessionUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit
) {

    val recording =
        sessionState is SessionUiState.Recording

    val paused =
        sessionState is SessionUiState.Paused

    Row(
        horizontalArrangement =
            Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        GlassCircleButton(
            icon = Icons.Default.Clear,
            onClick = onStop
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(
                    30.dp,
                    CircleShape
                )
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentColor,
                            primaryColor
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            IconButton(
                onClick = {
                    if (recording)
                        onPause()
                    else
                        onResume()
                }
            ) {

                Icon(
                    imageVector =
                        if (recording)
                            Icons.Default.PlayArrow
                        else
                            Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

    }
}

@Composable
fun GlassCircleButton(
    icon: ImageVector,
    onClick: () -> Unit
) {

    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.size(58.dp)
    ) {

        IconButton(onClick = onClick) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = backgroundColor
            )
        }
    }
}

@Composable
fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    Box(Modifier.fillMaxSize()) {
        repeat(12) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = (80 * index).toFloat(),
                targetValue = (80 * index - 40).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000 + index * 300,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle_$index"
            )

            Box(
                modifier = Modifier
                    .offset(
                        x = (20 * index).dp,
                        y = offsetY.dp
                    )
                    .size(if (index % 3 == 0) 8.dp else 5.dp)
                    .background(
                        backgroundColor.copy(alpha = if (index % 2 == 0) 0.2f else 0.1f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun ModernWaveform(
    amplitude: Int
) {

    val animated by animateFloatAsState(
        targetValue =
            (amplitude / 32767f)
                .coerceIn(0f, 1f),
        label = ""
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 20.dp)
    ) {

        val path = Path()

        val centerY = size.height / 2

        path.moveTo(0f, centerY)

        for (x in 0..size.width.toInt()) {

            val y =
                centerY +
                        sin(
                            x * 0.03f
                        ) *
                        (animated * 40f)

            path.lineTo(x.toFloat(), y)
        }

        drawPath(
            path,
            brush = Brush.horizontalGradient(
                listOf(
                    accentColor,
                    backgroundColor
                )
            ),
            style = Stroke(
                width = 6f,
                cap = StrokeCap.Round
            )
        )
    }
}

@Composable
private fun PermissionScreen(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B0F0D),
                       primaryColor,
                        Color(0xFF0B0F0D)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            PermissionOrb()

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Microphone Access",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = backgroundColor
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (shouldShowRationale)
                    "Geek Gauge needs microphone access to record learner evaluations, capture insights and generate AI-powered reports."
                else
                    "Allow microphone access to begin your evaluation session.",
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {

                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    "Grant Permission",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PermissionOrb() {

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentColor.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            backgroundColor,
                            accentColor
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B0F0D),
                        Color(0xFF16211C),
                        Color(0xFF0B0F0D)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            ErrorOrb()

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Something Went Wrong",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            GlassErrorCard(message)

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {

                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    "Try Again",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ErrorOrb() {

    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.Red.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFFFF6B6B),
                            Color(0xFFB00020)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun GlassErrorCard(
    message: String
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp)
    ) {

        Text(
            text = message,
            modifier = Modifier.padding(20.dp),
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

