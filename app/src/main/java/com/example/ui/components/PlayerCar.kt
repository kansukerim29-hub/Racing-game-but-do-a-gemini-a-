package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.CarModel
import kotlinx.coroutines.launch

/**
 * PlayerCar Composable.
 *
 * Renders an interactive 2D top-down sports car using [Canvas] positioned
 * at the bottom of the screen. Supports fluid horizontal dragging and tap
 * steering based on touch gestures.
 *
 * @param car Configuration for vehicle color scheme, perks, and aesthetics.
 * @param modifier Composable modifier.
 * @param normalizedX Horizontal position normalized between 0.0f (left) and 1.0f (right).
 *                    If null, internal state tracks touch drag input directly.
 * @param bottomOffsetFraction Vertical position relative to screen height (default ~0.78f).
 * @param carWidth Dp width of the car (default 64.dp).
 * @param carHeight Dp height of the car (default 112.dp).
 * @param isNitroActive Whether nitro boost flames should be rendered at the exhaust.
 * @param isShieldActive Whether neon energy shield bubble is active.
 * @param isBraking Whether brake lights are intensified.
 * @param onPositionChange Callback fired when user drags or moves the car (normalized 0.0f..1.0f).
 */
@Composable
fun PlayerCar(
    modifier: Modifier = Modifier,
    car: CarModel = CarModel.DEFAULT_CAR,
    normalizedX: Float? = null,
    bottomOffsetFraction: Float = 0.78f,
    carWidth: Dp = 64.dp,
    carHeight: Dp = 112.dp,
    isNitroActive: Boolean = false,
    isShieldActive: Boolean = false,
    isBraking: Boolean = false,
    onPositionChange: ((normalizedX: Float) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Internal position state when not externally driven
    var internalX by remember { mutableFloatStateOf(0.5f) }
    // Animated tilt angle when steering (-10° to +10°)
    val tiltAngle = remember { Animatable(0f) }

    val activeNormalizedX = normalizedX ?: internalX

    // Infinite animation for nitro exhaust and shield pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "car_effects")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("player_car_component")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val targetNormalized = (offset.x / size.width).coerceIn(0.08f, 0.92f)
                        internalX = targetNormalized
                        onPositionChange?.invoke(targetNormalized)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val deltaNormalized = dragAmount.x / size.width
                        val newX = (activeNormalizedX + deltaNormalized).coerceIn(0.08f, 0.92f)
                        internalX = newX
                        onPositionChange?.invoke(newX)

                        // Apply dynamic steering tilt
                        val targetTilt = (dragAmount.x * 0.4f).coerceIn(-12f, 12f)
                        coroutineScope.launch {
                            tiltAngle.snapTo(targetTilt)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            tiltAngle.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 600f))
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            tiltAngle.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 600f))
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val targetNormalized = (offset.x / size.width).coerceIn(0.08f, 0.92f)
                    val direction = if (targetNormalized < activeNormalizedX) -8f else 8f
                    internalX = targetNormalized
                    onPositionChange?.invoke(targetNormalized)
                    coroutineScope.launch {
                        tiltAngle.snapTo(direction)
                        tiltAngle.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 500f))
                    }
                }
            }
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // Convert normalized coordinates to local canvas pixels
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val carCenterX = activeNormalizedX * canvasW
            val carCenterY = bottomOffsetFraction * canvasH

            val widthPx = carWidth.toPx()
            val heightPx = carHeight.toPx()

            rotate(degrees = tiltAngle.value, pivot = Offset(carCenterX, carCenterY)) {
                drawCarBody(
                    car = car,
                    x = carCenterX,
                    y = carCenterY,
                    w = widthPx,
                    h = heightPx,
                    isNitro = isNitroActive,
                    isShield = isShieldActive,
                    isBraking = isBraking,
                    pulse = pulseAnim
                )
            }
        }
    }
}

/**
 * Draws the high-fidelity sports car top-down visual elements on [DrawScope].
 */
private fun DrawScope.drawCarBody(
    car: CarModel,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    isNitro: Boolean,
    isShield: Boolean,
    isBraking: Boolean,
    pulse: Float
) {
    val halfW = w / 2f
    val halfH = h / 2f

    // 1. Soft Dynamic Drop Shadow
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(x - halfW - 4f, y - halfH + 8f),
        size = Size(w + 8f, h + 6f),
        cornerRadius = CornerRadius(20f, 20f)
    )

    // 2. Wheels / Tires
    val wheelW = w * 0.22f
    val wheelH = h * 0.22f
    val tireColor = Color(0xFF0F1115)
    val rimColor = Color(0xFF64748B)

    // Front Wheels
    drawRoundRect(tireColor, Offset(x - halfW - (wheelW * 0.22f), y - (halfH * 0.75f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(tireColor, Offset(x + halfW - (wheelW * 0.78f), y - (halfH * 0.75f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    // Rear Wheels
    drawRoundRect(tireColor, Offset(x - halfW - (wheelW * 0.22f), y + (halfH * 0.45f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))
    drawRoundRect(tireColor, Offset(x + halfW - (wheelW * 0.78f), y + (halfH * 0.45f)), Size(wheelW, wheelH), CornerRadius(4f, 4f))

    // Wheel Rims
    drawRect(rimColor, Offset(x - halfW + 1f, y - (halfH * 0.75f) + 4f), Size(wheelW * 0.3f, wheelH - 8f))
    drawRect(rimColor, Offset(x + halfW - (wheelW * 0.5f), y - (halfH * 0.75f) + 4f), Size(wheelW * 0.3f, wheelH - 8f))

    // 3. Aerodynamic Main Body
    val bodyBrush = Brush.verticalGradient(
        colors = listOf(
            car.primaryColor,
            car.primaryColor.copy(alpha = 0.9f),
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

    // 4. Center Racing Stripe
    drawRoundRect(
        color = car.accentColor.copy(alpha = 0.9f),
        topLeft = Offset(x - (w * 0.08f), y - halfH + 4f),
        size = Size(w * 0.16f, h - 8f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // 5. Cockpit / Cabin
    val cabinW = w * 0.68f
    val cabinH = h * 0.44f
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(x - (cabinW / 2f), y - (cabinH / 2f)),
        size = Size(cabinW, cabinH),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Front Windshield (Reflective glass)
    drawRoundRect(
        color = Color(0xFF64748B),
        topLeft = Offset(x - (cabinW * 0.42f), y - (cabinH * 0.42f)),
        size = Size(cabinW * 0.84f, cabinH * 0.28f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Rear Windshield
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(x - (cabinW * 0.42f), y + (cabinH * 0.14f)),
        size = Size(cabinW * 0.84f, cabinH * 0.22f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // 6. Rear Wing / Spoiler
    drawRoundRect(
        color = car.secondaryColor,
        topLeft = Offset(x - halfW - 2f, y + halfH - (h * 0.09f)),
        size = Size(w + 4f, h * 0.07f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // 7. Xenon Headlights
    drawCircle(Color(0xFFFFFFEE), radius = w * 0.08f, center = Offset(x - (w * 0.32f), y - halfH + 6f))
    drawCircle(Color(0xFFFFFFEE), radius = w * 0.08f, center = Offset(x + (w * 0.32f), y - halfH + 6f))
    // Soft Headlight Beams Forward
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color(0x33FFFFFF), Color.Transparent),
            startY = y - halfH,
            endY = y - halfH - (h * 0.6f)
        ),
        topLeft = Offset(x - (w * 0.45f), y - halfH - (h * 0.6f)),
        size = Size(w * 0.9f, h * 0.6f)
    )

    // 8. Taillights (Intensified when braking)
    val taillightColor = if (isBraking) Color(0xFFFF0033) else Color(0xFFFF1744)
    val taillightHeight = if (isBraking) 6f else 4f
    drawRoundRect(taillightColor, Offset(x - (w * 0.42f), y + halfH - taillightHeight), Size(w * 0.22f, taillightHeight), CornerRadius(2f, 2f))
    drawRoundRect(taillightColor, Offset(x + (w * 0.20f), y + halfH - taillightHeight), Size(w * 0.22f, taillightHeight), CornerRadius(2f, 2f))

    // 9. Police Sirens
    if (car.isPolice) {
        val flash = (System.currentTimeMillis() / 150) % 2 == 0L
        val leftSiren = if (flash) Color(0xFFFF1744) else Color(0xFF2979FF)
        val rightSiren = if (flash) Color(0xFF2979FF) else Color(0xFFFF1744)
        drawCircle(leftSiren, radius = w * 0.1f, center = Offset(x - (w * 0.15f), y))
        drawCircle(rightSiren, radius = w * 0.1f, center = Offset(x + (w * 0.15f), y))
    }

    // 10. Nitro Boost Jets
    if (isNitro) {
        val flameLen = (h * 0.45f) * pulse
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

    // 11. Protective Energy Shield
    if (isShield) {
        drawCircle(
            color = Color(0xFF00F0FF).copy(alpha = 0.22f),
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
