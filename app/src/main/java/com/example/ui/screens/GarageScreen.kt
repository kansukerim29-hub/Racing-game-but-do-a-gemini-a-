package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarModel
import com.example.data.GameRepository
import com.example.util.LocalAppStrings

@Composable
fun GarageScreen(
    repository: GameRepository,
    currentCoins: Int,
    selectedCarId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val unlockedCars = remember(currentCoins, selectedCarId) { repository.getUnlockedCarIds() }
    val allCars = CarModel.ALL_CARS

    var currentCarIndex by remember {
        val idx = allCars.indexOfFirst { it.id == selectedCarId }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val car = allCars[currentCarIndex]
    val isUnlocked = unlockedCars.contains(car.id)
    val isSelected = selectedCarId == car.id

    val upgrades = remember(car.id, currentCoins) {
        repository.getCarUpgradeLevels(car.id)
    }
    val currentCarModel = car.copy(
        speedLevel = upgrades.first,
        handlingLevel = upgrades.second,
        nitroLevel = upgrades.third
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("garage_back_button")
                        .background(Color(0xFF1E293B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = Color.White
                    )
                }

                Text(
                    text = strings.garageTitle,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                // Coin Pill
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$currentCoins",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Car Stage Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1F2937))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Car Render on Pedestal Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cW = size.width
                        val cH = size.height
                        val midX = cW / 2f
                        val midY = cH / 2f

                        // Neon Circular Pedestal
                        drawOval(
                            brush = Brush.radialGradient(
                                listOf(currentCarModel.primaryColor.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(midX, midY + 40f),
                                radius = 180f
                            ),
                            topLeft = Offset(midX - 160f, midY - 20f),
                            size = Size(320f, 120f)
                        )

                        // Car Body (Detailed top-down preview)
                        val carW = 100f
                        val carH = 175f

                        // Shadow
                        drawRoundRect(
                            color = Color(0x66000000),
                            topLeft = Offset(midX - (carW / 2f) - 6f, midY - (carH / 2f) + 10f),
                            size = Size(carW + 12f, carH + 8f),
                            cornerRadius = CornerRadius(20f, 20f)
                        )

                        // Wheels
                        val wheelW = 22f
                        val wheelH = 40f
                        val tireColor = Color(0xFF0F172A)
                        drawRoundRect(tireColor, Offset(midX - (carW / 2f) - 6f, midY - 60f), Size(wheelW, wheelH), CornerRadius(4f, 4f))
                        drawRoundRect(tireColor, Offset(midX + (carW / 2f) - 16f, midY - 60f), Size(wheelW, wheelH), CornerRadius(4f, 4f))
                        drawRoundRect(tireColor, Offset(midX - (carW / 2f) - 6f, midY + 25f), Size(wheelW, wheelH), CornerRadius(4f, 4f))
                        drawRoundRect(tireColor, Offset(midX + (carW / 2f) - 16f, midY + 25f), Size(wheelW, wheelH), CornerRadius(4f, 4f))

                        // Body
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(currentCarModel.primaryColor, currentCarModel.secondaryColor)
                            ),
                            topLeft = Offset(midX - (carW / 2f), midY - (carH / 2f)),
                            size = Size(carW, carH),
                            cornerRadius = CornerRadius(24f, 24f)
                        )

                        // Center racing stripe
                        drawRoundRect(
                            color = currentCarModel.accentColor,
                            topLeft = Offset(midX - 8f, midY - (carH / 2f) + 10f),
                            size = Size(16f, carH - 20f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Cabin & Windshields
                        drawRoundRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(midX - 35f, midY - 35f),
                            size = Size(70f, 75f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        // Windshield
                        drawRoundRect(Color(0xFF64748B), Offset(midX - 30f, midY - 30f), Size(60f, 22f), CornerRadius(4f, 4f))
                        drawRoundRect(Color(0xFF475569), Offset(midX - 30f, midY + 14f), Size(60f, 18f), CornerRadius(4f, 4f))

                        // Headlights
                        drawCircle(Color(0xFFFFFFEE), radius = 8f, center = Offset(midX - 32f, midY - (carH / 2f) + 12f))
                        drawCircle(Color(0xFFFFFFEE), radius = 8f, center = Offset(midX + 32f, midY - (carH / 2f) + 12f))

                        // Taillights
                        drawRoundRect(Color(0xFFFF1744), Offset(midX - 42f, midY + (carH / 2f) - 6f), Size(24f, 6f), CornerRadius(3f, 3f))
                        drawRoundRect(Color(0xFFFF1744), Offset(midX + 18f, midY + (carH / 2f) - 6f), Size(24f, 6f), CornerRadius(3f, 3f))

                        // Police siren preview
                        if (currentCarModel.isPolice) {
                            drawCircle(Color(0xFFFF1744), radius = 9f, center = Offset(midX - 16f, midY - 2f))
                            drawCircle(Color(0xFF2979FF), radius = 9f, center = Offset(midX + 16f, midY - 2f))
                        }
                    }

                    // Car Carousel Controls (Previous & Next Arrow Buttons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                if (currentCarIndex > 0) currentCarIndex--
                                else currentCarIndex = allCars.size - 1
                            },
                            modifier = Modifier
                                .testTag("car_prev_button")
                                .background(Color(0x99000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.previous,
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                if (currentCarIndex < allCars.size - 1) currentCarIndex++
                                else currentCarIndex = 0
                            },
                            modifier = Modifier
                                .testTag("car_next_button")
                                .background(Color(0x99000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = strings.next,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Car Status Pill in Preview (e.g. "SEÇİLİ ARAÇ" or "KİLİTLİ")
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        color = when {
                            isSelected -> Color(0xFF10B981)
                            isUnlocked -> Color(0xFF0284C7)
                            else -> Color(0xFFEF4444)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSelected) strings.selected else if (isUnlocked) strings.unlocked else strings.locked,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Car Title & Perk
            Text(
                text = currentCarModel.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = currentCarModel.subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentCarModel.specialPerk,
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STATS & UPGRADES SECTION
            Text(
                text = strings.perfAndUpgrades,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Speed Stat
                    StatRow(
                        title = strings.maxSpeed,
                        value = "${currentCarModel.topSpeedKmh} ${strings.kmh}",
                        level = currentCarModel.speedLevel,
                        progress = (currentCarModel.topSpeedKmh - 180f) / 100f,
                        cost = currentCarModel.upgradeCost,
                        canUpgrade = isUnlocked && currentCarModel.speedLevel < 5 && currentCoins >= currentCarModel.upgradeCost,
                        onUpgrade = {
                            repository.upgradeCarStat(currentCarModel.id, "speed", currentCarModel.upgradeCost)
                        }
                    )

                    // 2. Handling Stat
                    StatRow(
                        title = strings.handling,
                        value = "Lv. ${currentCarModel.handlingLevel}",
                        level = currentCarModel.handlingLevel,
                        progress = currentCarModel.handlingLevel / 5f,
                        cost = currentCarModel.upgradeCost,
                        canUpgrade = isUnlocked && currentCarModel.handlingLevel < 5 && currentCoins >= currentCarModel.upgradeCost,
                        onUpgrade = {
                            repository.upgradeCarStat(currentCarModel.id, "handling", currentCarModel.upgradeCost)
                        }
                    )

                    // 3. Nitro Stat
                    StatRow(
                        title = strings.nitroDuration,
                        value = "${String.format("%.1f", currentCarModel.nitroDurationSec)}s",
                        level = currentCarModel.nitroLevel,
                        progress = currentCarModel.nitroLevel / 5f,
                        cost = currentCarModel.upgradeCost,
                        canUpgrade = isUnlocked && currentCarModel.nitroLevel < 5 && currentCoins >= currentCarModel.upgradeCost,
                        onUpgrade = {
                            repository.upgradeCarStat(currentCarModel.id, "nitro", currentCarModel.upgradeCost)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BUY OR SELECT BUTTON
            if (isUnlocked) {
                if (isSelected) {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .testTag("car_selected_button")
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.6f),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.carEquipped, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            repository.setSelectedCarId(currentCarModel.id)
                        },
                        modifier = Modifier
                            .testTag("car_equip_button")
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.equipCar, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val canAfford = currentCoins >= currentCarModel.price
                Button(
                    onClick = {
                        if (repository.spendCoins(currentCarModel.price)) {
                            repository.unlockCar(currentCarModel.id)
                            repository.setSelectedCarId(currentCarModel.id)
                        }
                    },
                    enabled = canAfford,
                    modifier = Modifier
                        .testTag("car_buy_button")
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB703),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.buyCar.replace("%d", "${currentCarModel.price}"),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    title: String,
    value: String,
    level: Int,
    progress: Float,
    cost: Int,
    canUpgrade: Boolean,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = value, color = Color(0xFF00F0FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0.1f, 1.0f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF00F0FF),
                trackColor = Color(0xFF1E293B)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        if (level < 5) {
            Button(
                onClick = onUpgrade,
                enabled = canUpgrade,
                modifier = Modifier.height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF047857),
                    disabledContainerColor = Color(0xFF1E293B)
                )
            ) {
                Icon(imageVector = Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$cost C", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "MAX",
                    color = Color(0xFFFFB703),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
