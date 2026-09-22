package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.data.CarModel
import com.example.game.Collectible
import com.example.game.GameEngine
import com.example.game.PickupType
import com.example.game.TrafficType
import com.example.game.TrafficVehicle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RacingCanvas(
    engine: GameEngine,
    controlMode: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(controlMode) {
                if (controlMode == "DRAG") {
                    detectDragGestures { change, _ ->
                        val roadLeft = size.width * 0.10f
                        val roadRight = size.width * 0.90f
                        val roadWidth = roadRight - roadLeft
                        val normalized = (change.position.x - roadLeft) / roadWidth
                        engine.setDirectTargetX(normalized)
                    }
                } else {
                    detectTapGestures { offset ->
                        val screenMid = size.width / 2f
                        if (offset.x < screenMid) {
                            engine.steerLeft()
                        } else {
                            engine.steerRight()
                        }
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Apply Screen Shake
        val shakeOffset = if (engine.screenShake > 0f) {
            val shakeX = (sin(System.currentTimeMillis() * 0.05f) * engine.screenShake * 18f).toFloat()
            val shakeY = (cos(System.currentTimeMillis() * 0.07f) * engine.screenShake * 18f).toFloat()
            Offset(shakeX, shakeY)
        } else Offset.Zero

        withTransform({
            translate(shakeOffset.x, shakeOffset.y)
        }) {
            // 1. Draw Road Shoulders & Asphalt
            val roadMargin = canvasWidth * 0.08f
            val roadWidth = canvasWidth - (roadMargin * 2f)
            val roadLeft = roadMargin
            val roadRight = roadMargin + roadWidth

            // Environment Background (Grass / Cyber city borders)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        engine.weatherTheme.skyColor,
                        engine.weatherTheme.skyColor.copy(alpha = 0.85f),
                        Color(0xFF07090E)
                    )
                )
            )

            // Outer kerb (Red & White checkered border strips)
            drawKerbs(roadLeft, roadRight, canvasHeight, engine.roadScrollOffset)

            // Asphalt surface
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF161A22),
                        engine.weatherTheme.roadColor,
                        Color(0xFF161A22)
                    ),
                    startX = roadLeft,
                    endX = roadRight
                ),
                topLeft = Offset(roadLeft, 0f),
                size = Size(roadWidth, canvasHeight)
            )

            // Neon Guardrails
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(engine.weatherTheme.stripeColor.copy(alpha = 0.6f), engine.weatherTheme.stripeColor)
                ),
                start = Offset(roadLeft, 0f),
                end = Offset(roadLeft, canvasHeight),
                strokeWidth = 5f
            )
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(engine.weatherTheme.stripeColor.copy(alpha = 0.6f), engine.weatherTheme.stripeColor)
                ),
                start = Offset(roadRight, 0f),
                end = Offset(roadRight, canvasHeight),
                strokeWidth = 5f
            )

            // 2. Draw Lane Markings (3 dividers for 4 lanes)
            val laneWidth = roadWidth / 4f
            val stripeHeight = canvasHeight * 0.075f
            val gapHeight = canvasHeight * 0.065f
            val totalSegment = stripeHeight + gapHeight
            val scrollPx = (engine.roadScrollOffset * totalSegment)

            for (i in 1..3) {
                val lineX = roadLeft + (i * laneWidth)
                var y = -totalSegment + (scrollPx % totalSegment)
                while (y < canvasHeight + totalSegment) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.75f),
                        topLeft = Offset(lineX - 2.5f, y),
                        size = Size(5f, stripeHeight),
                        cornerRadius = CornerRadius(2.5f, 2.5f)
                    )
                    y += totalSegment
                }
            }

            // Speed lines on borders when going very fast or using nitro
            if (engine.currentSpeedKmh > 175f || engine.isNitroActive) {
                drawSpeedLines(roadLeft, roadRight, canvasHeight, engine.isNitroActive)
            }

            // 3. Draw Collectibles
            for (item in engine.collectibles) {
                val itemX = roadLeft + (item.x * roadWidth)
                val itemY = item.y * canvasHeight
                drawCollectible(item, itemX, itemY, pulseAnim)
            }

            // 4. Draw Traffic Vehicles
            for (traffic in engine.trafficVehicles) {
                val tX = roadLeft + (traffic.x * roadWidth)
                val tY = traffic.y * canvasHeight
                val carW = roadWidth * 0.16f * traffic.type.widthFactor
                val carH = canvasHeight * 0.11f * traffic.type.lengthFactor
                drawTrafficCar(traffic, tX, tY, carW, carH)
            }

            // 5. Draw Player Car
            val pX = roadLeft + (engine.playerX * roadWidth)
            val pY = engine.playerY * canvasHeight
            val playerW = roadWidth * 0.165f
            val playerH = canvasHeight * 0.115f

            // Headlight beams onto road
            drawHeadlights(pX, pY, playerW, playerH)

            // Player car body
            drawPlayerCar(engine.car, pX, pY, playerW, playerH, engine.isNitroActive, engine.isShieldActive, pulseAnim)

            // 6. Draw Particles (Exhaust flames, smoke, sparks)
            for (p in engine.particles) {
                val partX = roadLeft + (p.x * roadWidth)
                val partY = p.y * canvasHeight
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(partX, partY)
                )
            }

            // 7. Draw Floating Text Notices
            for (notice in engine.notices) {
                val nX = roadLeft + (notice.x * roadWidth)
                val nY = notice.y * canvasHeight
                val style = TextStyle(
                    color = notice.color.copy(alpha = notice.alpha),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                val measured = textMeasurer.measure(notice.text, style)
                drawText(
                    textMeasurer = textMeasurer,
                    text = notice.text,
                    style = style,
                    topLeft = Offset(nX - (measured.size.width / 2f), nY)
                )
            }
        }
    }
}

private fun DrawScope.drawKerbs(left: Float, right: Float, height: Float, scroll: Float) {
    val kerbW = 12f
    val segmentH = 34f
    val offset = (scroll * segmentH * 2f) % (segmentH * 2f)

    var y = -segmentH * 2f + offset
    var isRed = false
    while (y < height + segmentH * 2f) {
        val color = if (isRed) Color(0xFFE53935) else Color(0xFFF1F5F9)
        // Left kerb
        drawRect(color = color, topLeft = Offset(left - kerbW, y), size = Size(kerbW, segmentH))
        // Right kerb
        drawRect(color = color, topLeft = Offset(right, y), size = Size(kerbW, segmentH))
        isRed = !isRed
        y += segmentH
    }
}

private fun DrawScope.drawSpeedLines(left: Float, right: Float, height: Float, isNitro: Boolean) {
    val color = if (isNitro) Color(0xFF00F0FF).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.25f)
    for (i in 0..6) {
        val yStart = (System.currentTimeMillis() * 1.5f + (i * 180f)) % height
        val lineLen = 80f + (i * 20f)
        // Left side speed line
        drawLine(
            color = color,
            start = Offset(left + 8f + (i * 4f), yStart),
            end = Offset(left + 8f + (i * 4f), yStart + lineLen),
            strokeWidth = 3f
        )
        // Right side speed line
        drawLine(
            color = color,
            start = Offset(right - 8f - (i * 4f), yStart),
            end = Offset(right - 8f - (i * 4f), yStart + lineLen),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawHeadlights(x: Float, y: Float, width: Float, height: Float) {
    val beamPathLeft = Path().apply {
        moveTo(x - (width * 0.28f), y - (height * 0.4f))
        lineTo(x - (width * 0.75f), y - (height * 3.5f))
        lineTo(x - (width * 0.15f), y - (height * 3.5f))
        close()
    }
    val beamPathRight = Path().apply {
        moveTo(x + (width * 0.28f), y - (height * 0.4f))
        lineTo(x + (width * 0.15f), y - (height * 3.5f))
        lineTo(x + (width * 0.75f), y - (height * 3.5f))
        close()
    }

    val beamBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0x35FFF380),
            Color(0x10FFF380),
            Color.Transparent
        ),
        startY = y - (height * 3.5f),
        endY = y - (height * 0.3f)
    )

    drawPath(beamPathLeft, brush = beamBrush)
    drawPath(beamPathRight, brush = beamBrush)
}

private fun DrawScope.drawPlayerCar(
    car: CarModel,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    isNitro: Boolean,
    isShield: Boolean,
    pulse: Float
) {
    val halfW = w / 2f
    val halfH = h / 2f

    // 1. Soft Shadow
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(x - halfW - 4f, y - halfH + 8f),
        size = Size(w + 8f, h + 6f),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // 2. Wheels (4 tires)
    val wheelW = w * 0.22f
    val wheelH = h * 0.24f
    val tireColor = Color(0xFF0F1115)
    // Front wheels
    drawRoundRect(tireColor, Offset(x - halfW - (wheelW * 0.25f), y - (halfH * 0.75f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(tireColor, Offset(x + halfW - (wheelW * 0.75f), y - (halfH * 0.75f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    // Rear wheels
    drawRoundRect(tireColor, Offset(x - halfW - (wheelW * 0.25f), y + (halfH * 0.45f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(tireColor, Offset(x + halfW - (wheelW * 0.75f), y + (halfH * 0.45f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))

    // 3. Main Body
    val bodyBrush = Brush.verticalGradient(
        colors = listOf(
            car.primaryColor,
            car.primaryColor.copy(alpha = 0.85f),
            car.secondaryColor
        ),
        startY = y - halfH,
        endY = y + halfH
    )
    drawRoundRect(
        brush = bodyBrush,
        topLeft = Offset(x - halfW, y - halfH),
        size = Size(w, h),
        cornerRadius = CornerRadius(18f, 18f)
    )

    // 4. Racing Stripes or Hood Accent
    drawRoundRect(
        color = car.accentColor.copy(alpha = 0.85f),
        topLeft = Offset(x - (w * 0.08f), y - halfH + 4f),
        size = Size(w * 0.16f, h - 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // 5. Cabin & Windshields
    val cabinW = w * 0.68f
    val cabinH = h * 0.44f
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(x - (cabinW / 2f), y - (cabinH / 2f)),
        size = Size(cabinW, cabinH),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Front windshield
    drawRoundRect(
        color = Color(0xFF64748B),
        topLeft = Offset(x - (cabinW * 0.42f), y - (cabinH * 0.42f)),
        size = Size(cabinW * 0.84f, cabinH * 0.28f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Rear windshield
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(x - (cabinW * 0.42f), y + (cabinH * 0.14f)),
        size = Size(cabinW * 0.84f, cabinH * 0.22f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // 6. Rear Wing / Spoiler
    drawRoundRect(
        color = car.secondaryColor,
        topLeft = Offset(x - halfW - 2f, y + halfH - (h * 0.1f)),
        size = Size(w + 4f, h * 0.08f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // 7. Headlights (Bright Xenon)
    drawCircle(Color(0xFFFFFFEE), radius = w * 0.08f, center = Offset(x - (w * 0.32f), y - halfH + 6f))
    drawCircle(Color(0xFFFFFFEE), radius = w * 0.08f, center = Offset(x + (w * 0.32f), y - halfH + 6f))

    // 8. Taillights (Glowing Red)
    drawRoundRect(Color(0xFFFF1744), Offset(x - (w * 0.42f), y + halfH - 4f), Size(w * 0.22f, 4f), CornerRadius(2f, 2f))
    drawRoundRect(Color(0xFFFF1744), Offset(x + (w * 0.20f), y + halfH - 4f), Size(w * 0.22f, 4f), CornerRadius(2f, 2f))

    // 9. Police Sirens (Flashing red and blue)
    if (car.isPolice) {
        val flash = (System.currentTimeMillis() / 150) % 2 == 0L
        val leftSiren = if (flash) Color(0xFFFF1744) else Color(0xFF2979FF)
        val rightSiren = if (flash) Color(0xFF2979FF) else Color(0xFFFF1744)
        drawCircle(leftSiren, radius = w * 0.1f, center = Offset(x - (w * 0.15f), y))
        drawCircle(rightSiren, radius = w * 0.1f, center = Offset(x + (w * 0.15f), y))
    }

    // 10. Nitro Jets if active
    if (isNitro) {
        val flameLen = (h * 0.4f) * pulse
        // Left exhaust flame
        drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFF00F0FF), Color(0x0000F0FF))),
            topLeft = Offset(x - (w * 0.28f), y + halfH - 2f),
            size = Size(w * 0.16f, flameLen)
        )
        // Right exhaust flame
        drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFF00F0FF), Color(0x0000F0FF))),
            topLeft = Offset(x + (w * 0.12f), y + halfH - 2f),
            size = Size(w * 0.16f, flameLen)
        )
    }

    // 11. Forcefield Shield
    if (isShield) {
        drawCircle(
            color = Color(0xFF00F0FF).copy(alpha = 0.25f),
            radius = (h * 0.72f) * pulse,
            center = Offset(x, y)
        )
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = (h * 0.72f) * pulse,
            center = Offset(x, y),
            style = Stroke(width = 3.5f)
        )
    }
}

private fun DrawScope.drawTrafficCar(traffic: TrafficVehicle, x: Float, y: Float, w: Float, h: Float) {
    val halfW = w / 2f
    val halfH = h / 2f

    // Shadow
    drawRoundRect(
        color = Color(0x55000000),
        topLeft = Offset(x - halfW - 2f, y - halfH + 6f),
        size = Size(w + 4f, h + 4f),
        cornerRadius = CornerRadius(10f, 10f)
    )

    when (traffic.type) {
        TrafficType.TRUCK -> {
            // Big 18-Wheeler Truck
            // Tires
            val wheelW = w * 0.18f
            val wheelH = h * 0.12f
            for (offsetY in listOf(0.15f, 0.45f, 0.78f)) {
                drawRoundRect(Color(0xFF1E293B), Offset(x - halfW - (wheelW * 0.2f), y - halfH + (h * offsetY)), Size(wheelW, wheelH), CornerRadius(3f, 3f))
                drawRoundRect(Color(0xFF1E293B), Offset(x + halfW - (wheelW * 0.8f), y - halfH + (h * offsetY)), Size(wheelW, wheelH), CornerRadius(3f, 3f))
            }
            // Cargo container
            drawRoundRect(
                color = traffic.color,
                topLeft = Offset(x - halfW, y - halfH + (h * 0.25f)),
                size = Size(w, h * 0.72f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Warning stripes on truck rear
            for (i in 0..4) {
                val stripeX = x - halfW + (i * (w / 5f))
                val col = if (i % 2 == 0) Color(0xFFFFB703) else Color(0xFF1E293B)
                drawRect(col, Offset(stripeX, y + halfH - 6f), Size(w / 5f, 6f))
            }
            // Cab / Front
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(x - (w * 0.44f), y - halfH),
                size = Size(w * 0.88f, h * 0.22f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Windshield
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(x - (w * 0.38f), y - halfH + 3f),
                size = Size(w * 0.76f, h * 0.08f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Taillights
            drawRoundRect(Color(0xFFFF2A4B), Offset(x - (w * 0.45f), y + halfH - 4f), Size(w * 0.25f, 4f), CornerRadius(2f, 2f))
            drawRoundRect(Color(0xFFFF2A4B), Offset(x + (w * 0.20f), y + halfH - 4f), Size(w * 0.25f, 4f), CornerRadius(2f, 2f))
        }

        TrafficType.POLICE -> {
            // Police cruiser
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color.White, Color(0xFF1E3A8A))),
                topLeft = Offset(x - halfW, y - halfH),
                size = Size(w, h),
                cornerRadius = CornerRadius(14f, 14f)
            )
            // Cabin
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(x - (w * 0.32f), y - (h * 0.22f)),
                size = Size(w * 0.64f, h * 0.44f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Police light bar (alternating red/blue)
            val flash = (System.currentTimeMillis() / 120) % 2 == 0L
            val redCol = if (flash) Color(0xFFFF1744) else Color(0xFF2979FF)
            val blueCol = if (flash) Color(0xFF2979FF) else Color(0xFFFF1744)
            drawCircle(redCol, radius = w * 0.11f, center = Offset(x - (w * 0.16f), y))
            drawCircle(blueCol, radius = w * 0.11f, center = Offset(x + (w * 0.16f), y))

            // Taillights
            drawRoundRect(Color(0xFFFF1744), Offset(x - (w * 0.4f), y + halfH - 4f), Size(w * 0.22f, 4f), CornerRadius(2f, 2f))
            drawRoundRect(Color(0xFFFF1744), Offset(x + (w * 0.18f), y + halfH - 4f), Size(w * 0.22f, 4f), CornerRadius(2f, 2f))
        }

        else -> {
            // Standard Sedan or Sports car
            drawRoundRect(
                color = traffic.color,
                topLeft = Offset(x - halfW, y - halfH),
                size = Size(w, h),
                cornerRadius = CornerRadius(14f, 14f)
            )
            // Roof / Cabin
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(x - (w * 0.32f), y - (h * 0.2f)),
                size = Size(w * 0.64f, h * 0.4f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Front & Rear Glass
            drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(x - (w * 0.28f), y - (h * 0.18f)),
                size = Size(w * 0.56f, h * 0.14f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color(0xFF475569),
                topLeft = Offset(x - (w * 0.28f), y + (h * 0.05f)),
                size = Size(w * 0.56f, h * 0.12f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Taillights
            drawRoundRect(Color(0xFFFF1744), Offset(x - (w * 0.4f), y + halfH - 4f), Size(w * 0.22f, 4f), CornerRadius(2f, 2f))
            drawRoundRect(Color(0xFFFF1744), Offset(x + (w * 0.18f), y + halfH - 4f), Size(w * 0.22f, 4f), CornerRadius(2f, 2f))
        }
    }
}

private fun DrawScope.drawCollectible(item: Collectible, x: Float, y: Float, pulse: Float) {
    val radius = 18f * pulse
    when (item.type) {
        PickupType.COIN -> {
            // Outer glowing gold halo
            drawCircle(Color(0x55FFD700), radius = radius * 1.5f, center = Offset(x, y))
            // Gold coin body
            drawCircle(Color(0xFFFFD700), radius = radius, center = Offset(x, y))
            drawCircle(Color(0xFFFFB703), radius = radius * 0.72f, center = Offset(x, y))
            drawCircle(Color(0xFFFFFBEB), radius = radius * 0.35f, center = Offset(x, y))
        }

        PickupType.NITRO -> {
            // Glowing cyan bottle
            drawCircle(Color(0x5500F0FF), radius = radius * 1.6f, center = Offset(x, y))
            drawRoundRect(
                color = Color(0xFF00F0FF),
                topLeft = Offset(x - (radius * 0.65f), y - radius),
                size = Size(radius * 1.3f, radius * 2f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Nitro cap
            drawRect(Color.White, Offset(x - (radius * 0.35f), y - radius - 4f), Size(radius * 0.7f, 5f))
        }

        PickupType.SHIELD -> {
            // Electric shield orb
            drawCircle(Color(0x4438BDF8), radius = radius * 1.6f, center = Offset(x, y))
            drawCircle(Color(0xFF38BDF8), radius = radius, center = Offset(x, y), style = Stroke(width = 4f))
            drawCircle(Color.White, radius = radius * 0.45f, center = Offset(x, y))
        }

        PickupType.MAGNET -> {
            // Red / Blue magnet
            drawCircle(Color(0x44FF007F), radius = radius * 1.5f, center = Offset(x, y))
            drawArc(
                color = Color(0xFFFF1744),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(x - radius, y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 6f)
            )
        }

        PickupType.REPAIR -> {
            // Green medical / repair cross
            drawCircle(Color(0x4400FF66), radius = radius * 1.5f, center = Offset(x, y))
            drawCircle(Color(0xFF00FF66), radius = radius, center = Offset(x, y))
            drawRect(Color.White, Offset(x - 3f, y - (radius * 0.65f)), Size(6f, radius * 1.3f))
            drawRect(Color.White, Offset(x - (radius * 0.65f), y - 3f), Size(radius * 1.3f, 6f))
        }
    }
}
