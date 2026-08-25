package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Bản màu Bootstrap sáng tươi, logic và hiện đại (Bootstrap-inspired Fresh Theme)
private val BootstrapBlue = Color(0xFF0D6EFD)      // Primary blue
private val BootstrapGreen = Color(0xFF198754)     // Success green
private val BootstrapGrayBg = Color(0xFFF8F9FA)    // Light gray background
private val BootstrapWhiteCard = Color(0xFFFFFFFF) // Card surface
private val BootstrapTextDark = Color(0xFF212529)  // Dark body text
private val BootstrapBorderGray = Color(0xFFDEE2E6) // Border stroke color
private val BootstrapInfoTeal = Color(0xFF0DCAF0)  // Info teal
private val BootstrapWarningGold = Color(0xFFFFC107) // Warning gold

private val LightColorScheme = lightColorScheme(
    primary = BootstrapBlue,
    secondary = BootstrapGreen,
    tertiary = BootstrapInfoTeal,
    background = BootstrapGrayBg,
    surface = BootstrapWhiteCard,
    surfaceVariant = BootstrapBorderGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BootstrapTextDark,
    onSurface = BootstrapTextDark,
    onSurfaceVariant = Color(0xFF495057)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3788FF),                  // Brighter blue for dark mode
    secondary = Color(0xFF2EA86E),                // Brighter green
    tertiary = Color(0xFF3FD3F3),
    background = Color(0xFF0F172A),               // Modern dark slate background
    surface = Color(0xFF1E293B),                  // Slate card surface
    surfaceVariant = Color(0xFF334155),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF8FAFC),             // Soft white text
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Mặc định là giao diện tối hiện đại để phù hợp yêu cầu của người dùng
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
