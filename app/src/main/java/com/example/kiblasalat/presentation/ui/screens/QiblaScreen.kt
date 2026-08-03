package com.example.kiblasalat.presentation.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiblasalat.presentation.viewmodel.QiblaViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.qiblaState.collectAsState()

    // Register sensors when screen is active and unregister when inactive
    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose {
            viewModel.stopListening()
        }
    }

    // Access system vibrator for alignment feedback
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val qiblaDirection = state.qiblaDirection
    val isAligned = qiblaDirection?.isAligned == true

    // Trigger vibration exactly once when entering aligned state
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(120)
            }
        }
        wasAligned = isAligned
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Header
        Text(
            text = "Qibla Finder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            text = "Keep your phone flat for accurate results",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Start
        )

        if (!state.hasSensors) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Calibration Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.errorMessage ?: "Hardware Sensors Missing",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Compass Visualizer
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (qiblaDirection != null) {
                    val emerald = MaterialTheme.colorScheme.primary
                    val gold = MaterialTheme.colorScheme.secondary
                    val surfaceColor = MaterialTheme.colorScheme.surface
                    val textDark = MaterialTheme.colorScheme.onSurface

                    // Animate visual changes on alignment
                    val alignmentPulse by animateFloatAsState(
                        targetValue = if (isAligned) 1.1f else 1.0f,
                        animationSpec = tween(300),
                        label = "AlignmentPulse"
                    )

                    Canvas(
                        modifier = Modifier
                            .size(280.dp * alignmentPulse)
                    ) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 20.dp.toPx()

                        // 1. Draw Outer Compass Background Ring
                        drawCircle(
                            color = if (isAligned) emerald.copy(alpha = 0.05f) else Color.Transparent,
                            radius = radius + 15.dp.toPx(),
                            center = center
                        )

                        drawCircle(
                            color = surfaceColor,
                            radius = radius,
                            center = center
                        )

                        drawCircle(
                            color = if (isAligned) emerald else gold.copy(alpha = 0.5f),
                            radius = radius,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                        )

                        // 2. Draw North Dial (Rotates in negative user azimuth direction)
                        // This matches true North to physical North
                        rotate(-qiblaDirection.userAzimuth, pivot = center) {
                            // North marker
                            drawCircle(
                                color = Color.Red,
                                radius = 6.dp.toPx(),
                                center = Offset(center.x, center.y - radius + 12.dp.toPx())
                            )
                            
                            // Tick lines
                            for (angle in 0 until 360 step 30) {
                                val tickLength = if (angle % 90 == 0) 15.dp.toPx() else 8.dp.toPx()
                                val tickWidth = if (angle % 90 == 0) 3.dp.toPx() else 1.dp.toPx()
                                val tickColor = if (angle == 0) Color.Red else textDark.copy(alpha = 0.4f)
                                
                                val rad = Math.toRadians(angle.toDouble())
                                val startX = center.x + (radius - tickLength) * sin(rad).toFloat()
                                val startY = center.y - (radius - tickLength) * cos(rad).toFloat()
                                val endX = center.x + radius * sin(rad).toFloat()
                                val endY = center.y - radius * cos(rad).toFloat()
                                
                                drawLine(
                                    color = tickColor,
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = tickWidth
                                )
                            }
                        }

                        // 3. Draw Inner Qibla Pointer Arrow (Rotates in relative angle direction)
                        rotate(qiblaDirection.relativeAngle, pivot = center) {
                            // Draw an elegant pointer arrow indicating the Kaaba
                            val arrowPath = Path().apply {
                                moveTo(center.x, center.y - radius + 25.dp.toPx()) // Tip
                                lineTo(center.x - 12.dp.toPx(), center.y - radius + 60.dp.toPx()) // Bottom-Left
                                lineTo(center.x - 4.dp.toPx(), center.y - radius + 52.dp.toPx()) // Inset Left
                                lineTo(center.x - 4.dp.toPx(), center.y - 10.dp.toPx()) // Shaft Left
                                lineTo(center.x + 4.dp.toPx(), center.y - 10.dp.toPx()) // Shaft Right
                                lineTo(center.x + 4.dp.toPx(), center.y - radius + 52.dp.toPx()) // Inset Right
                                lineTo(center.x + 12.dp.toPx(), center.y - radius + 60.dp.toPx()) // Bottom-Right
                                close()
                            }

                            drawPath(
                                path = arrowPath,
                                color = if (isAligned) emerald else gold
                            )
                        }

                        // 4. Center Core
                        drawCircle(
                            color = if (isAligned) emerald else gold,
                            radius = 12.dp.toPx(),
                            center = center
                        )
                        drawCircle(
                            color = surfaceColor,
                            radius = 6.dp.toPx(),
                            center = center
                        )
                    }
                }
            }

            // Alignment Banner card
            if (qiblaDirection != null) {
                AnimatedVisibility(
                    visible = isAligned,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Kaaba Aligned",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Kaaba Aligned!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "You are facing directly towards Makkah.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Info Cards showing angles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "QIBLA ANGLE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f°", qiblaDirection.qiblaBearing),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .height(40.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "YOUR HEADING",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f°", qiblaDirection.userAzimuth),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isAligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
