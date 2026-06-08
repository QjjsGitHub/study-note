package com.example.study.videoPlayer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 视频播放器专用深色主题色板
val VideoBackground = Color(0xFF0F0F0F)
val VideoSurface = Color(0xFF1A1A1A)
val VideoSurfaceVariant = Color(0xFF2A2A2A)
val VideoPrimary = Color(0xFF4FC3F7)
val VideoOnPrimary = Color(0xFF003544)
val VideoSecondary = Color(0xFF80CBC4)
val VideoOnBackground = Color(0xFFE8E8E8)
val VideoOnSurface = Color(0xFFD0D0D0)
val VideoOnSurfaceVariant = Color(0xFF999999)
val VideoAccent = Color(0xFFFF6D00)
val VideoControlBg = Color(0xCC1A1A1A)

private val VideoPlayerDarkColorScheme = darkColorScheme(
    primary = VideoPrimary,
    onPrimary = VideoOnPrimary,
    secondary = VideoSecondary,
    background = VideoBackground,
    surface = VideoSurface,
    surfaceVariant = VideoSurfaceVariant,
    onBackground = VideoOnBackground,
    onSurface = VideoOnSurface,
    onSurfaceVariant = VideoOnSurfaceVariant,
    error = Color(0xFFFF5252),
    outline = Color(0xFF3A3A3A)
)

@Composable
fun VideoPlayerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = VideoPlayerDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}