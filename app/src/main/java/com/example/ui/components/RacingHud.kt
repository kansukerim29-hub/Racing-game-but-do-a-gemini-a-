package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.util.LocalAppStrings

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RacingHud(
    engine: GameEngine,
    controlMode: String,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // TOP HUD BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Upper Info Row: Score, Mode, Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Multiplier
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xCC0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.score.uppercase()}: ${engine.score.toInt()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            if (engine.combo > 1.05f) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFB703),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "x${String.format("%.1f", engine.combo)}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Coins
                    Surface(
                        color = Color(0xCC0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${engine.coinsEarned}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Pause Button
                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .testTag("pause_button")
                        .size(42.dp)
                        .background(Color(0xCC0F172A), CircleShape)
                        .border(1.dp, Color(0xFF475569), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = strings.gamePaused,
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lower Info Row: Speedometer + Distance + Lives / Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speedometer Gauge Display
                Surface(
                    color = Color(0xDD0B0F19),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.5.dp,
                        color = if (engine.isNitroActive) Color(0xFF00F0FF) else Color(0xFF3B82F6)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = strings.speed,
                            tint = if (engine.isNitroActive) Color(0xFF00F0FF) else Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${engine.currentSpeedKmh.toInt()}",
                            color = if (engine.isNitroActive) Color(0xFF00F0FF) else Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.kmh.uppercase(),
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Distance
                Surface(
                    color = Color(0xCC0F172A),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", engine.distanceMeters / 1000f)} km",
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Lives or Timer
                if (engine.mode == GameMode.TIME_ATTACK) {
                    Surface(
                        color = if (engine.timeRemainingSec < 10f) Color(0xDDDC2626) else Color(0xCC0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Zaman",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${engine.timeRemainingSec.toInt()}s",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .background(Color(0xCC0F172A), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..4) {
                            val active = i <= engine.lives
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Can $i",
                                tint = if (active) Color(0xFFFF2A4B) else Color(0x4464748B),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(horizontal = 1.dp)
                            )
                        }
                        if (engine.isShieldActive) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Kalkan",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM CONTROLS BAR (Only if controlMode == "BUTTONS")
        if (controlMode == "BUTTONS") {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Steering Buttons (Left & Right)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Left Steer
                    Box(
                        modifier = Modifier
                            .testTag("steer_left_button")
                            .size(72.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(Color(0xEE1E293B), Color(0xDD0F172A))
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFF475569), CircleShape)
                            .clickable { engine.steerLeft() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Sola Dön",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Right Steer
                    Box(
                        modifier = Modifier
                            .testTag("steer_right_button")
                            .size(72.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(Color(0xEE1E293B), Color(0xDD0F172A))
                                ),
                                shape = CircleShape
                            )
                            .border(2.dp, Color(0xFF475569), CircleShape)
                            .clickable { engine.steerRight() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Sağa Dön",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Action Buttons: Brake & Nitro Boost
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Brake Button (Hold to brake)
                    Box(
                        modifier = Modifier
                            .testTag("brake_button")
                            .size(72.dp)
                            .background(
                                color = if (engine.isBraking) Color(0xEEB91C1C) else Color(0xCC1E293B),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (engine.isBraking) Color(0xFFFF2A4B) else Color(0xFF64748B),
                                shape = CircleShape
                            )
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        engine.setBrake(true)
                                        true
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        engine.setBrake(false)
                                        true
                                    }
                                    else -> false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = strings.brake,
                                color = if (engine.isBraking) Color.White else Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Nitro Boost Button
                    val nitroAvailable = engine.nitroGauge > 0.05f
                    Box(
                        modifier = Modifier
                            .testTag("nitro_button")
                            .size(74.dp)
                            .background(
                                brush = if (engine.isNitroActive) {
                                    Brush.radialGradient(listOf(Color(0xFF00F0FF), Color(0xFF0284C7)))
                                } else {
                                    Brush.radialGradient(listOf(Color(0xDD0F172A), Color(0xCC0284C7)))
                                },
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (engine.isNitroActive) Color.White else Color(0xFF00F0FF),
                                shape = CircleShape
                            )
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> {
                                        engine.setNitro(true)
                                        true
                                    }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                        engine.setNitro(false)
                                        true
                                    }
                                    else -> false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = strings.nitro,
                                tint = if (engine.isNitroActive) Color.White else Color(0xFF00F0FF),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = strings.nitro.uppercase(),
                                color = if (engine.isNitroActive) Color.White else Color(0xFF00F0FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                            // Mini circular or linear gauge indication
                            Text(
                                text = "${(engine.nitroGauge * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Drag mode hint & quick nitro
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xAA0F172A),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.dragToSteer,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                // Nitro Button for drag mode
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(
                            brush = if (engine.isNitroActive) {
                                Brush.radialGradient(listOf(Color(0xFF00F0FF), Color(0xFF0284C7)))
                            } else {
                                Brush.radialGradient(listOf(Color(0xDD0F172A), Color(0xCC0284C7)))
                            },
                            shape = CircleShape
                        )
                        .border(2.dp, Color(0xFF00F0FF), CircleShape)
                        .pointerInteropFilter { event ->
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    engine.setNitro(true)
                                    true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    engine.setNitro(false)
                                    true
                                }
                                else -> false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = strings.nitro,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = strings.nitro.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
