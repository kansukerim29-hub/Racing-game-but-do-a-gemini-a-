package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.WeatherTheme
import com.example.util.AppLanguage
import com.example.util.resolveStrings

@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    onSoundChanged: (Boolean) -> Unit,
    hapticEnabled: Boolean,
    onHapticChanged: (Boolean) -> Unit,
    controlMode: String,
    onControlModeChanged: (String) -> Unit,
    currentTheme: WeatherTheme,
    onThemeChanged: (WeatherTheme) -> Unit,
    currentLanguage: AppLanguage = AppLanguage.SYSTEM,
    onLanguageChanged: (AppLanguage) -> Unit = {},
    onClose: () -> Unit
) {
    val strings = resolveStrings(currentLanguage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xBB090D16)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF374151))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.settingsTitle,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.close, tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Language Selection Section (English / Turkish / System Default)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        strings.languageSection,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = currentLanguage == lang
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onLanguageChanged(lang) }
                                .testTag("language_option_${lang.code}"),
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1F2937),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) Color(0xFF38BDF8) else Color(0xFF374151)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SYSTEM -> if (strings == com.example.util.TurkishStrings) "Sistem" else "System"
                                        AppLanguage.ENGLISH -> "English"
                                        AppLanguage.TURKISH -> "Türkçe"
                                    },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SYSTEM -> if (strings == com.example.util.TurkishStrings) "Varsayılan" else "Default"
                                        AppLanguage.ENGLISH -> "EN"
                                        AppLanguage.TURKISH -> "TR"
                                    },
                                    color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.soundEffects, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF), checkedTrackColor = Color(0xFF0369A1))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Haptic Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(strings.vibrationHaptics, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = onHapticChanged,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF), checkedTrackColor = Color(0xFF0369A1))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Mode
                Text(strings.controlScheme, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isButtons = controlMode == "BUTTONS"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onControlModeChanged("BUTTONS") },
                        color = if (isButtons) Color(0xFF0284C7) else Color(0xFF1F2937),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isButtons) Color(0xFF38BDF8) else Color(0xFF374151))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                strings.controlButtons,
                                color = Color.White,
                                fontWeight = if (isButtons) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Text(strings.controlButtonsSubtitle, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }

                    val isDrag = controlMode == "DRAG"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onControlModeChanged("DRAG") },
                        color = if (isDrag) Color(0xFF0284C7) else Color(0xFF1F2937),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDrag) Color(0xFF38BDF8) else Color(0xFF374151))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                strings.controlDrag,
                                color = Color.White,
                                fontWeight = if (isDrag) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Text(strings.controlDragSubtitle, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Selection
                Text(strings.trackAtmosphere, color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherTheme.entries.forEach { theme ->
                        val isSelected = currentTheme == theme
                        val themeTitle = when (theme) {
                            WeatherTheme.NEON_NIGHT -> strings.themeNeonNight
                            WeatherTheme.SUNSET_GLOW -> strings.themeSunsetHighway
                            WeatherTheme.CYBER_STORM -> strings.themeCyberRain
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onThemeChanged(theme) },
                            color = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) theme.stripeColor else Color(0xFF334155)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = themeTitle,
                                    color = if (isSelected) theme.stripeColor else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937))
                ) {
                    Text(strings.ok, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
