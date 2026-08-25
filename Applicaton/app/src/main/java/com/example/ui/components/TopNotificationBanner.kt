package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopSlideNotificationBanner(
    message: String?,
    type: String = "info",
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 250)
        ) + fadeOut(),
        modifier = modifier
    ) {
        if (message != null) {
            val (bgColor, borderColor, icon, iconColor, headerTitle) = when (type) {
                "success" -> Tuple5(
                    Color(0xFF022C22),
                    Color(0xFF10B981),
                    Icons.Default.CheckCircle,
                    Color(0xFF34D399),
                    "THÔNG BÁO THÀNH CÔNG"
                )
                "error" -> Tuple5(
                    Color(0xFF450A0A),
                    Color(0xFFEF4444),
                    Icons.Default.Error,
                    Color(0xFFF87171),
                    "CẢNH BÁO HỆ THỐNG"
                )
                "warning" -> Tuple5(
                    Color(0xFF451A03),
                    Color(0xFFF59E0B),
                    Icons.Default.Warning,
                    Color(0xFFFBBF24),
                    "LƯU Ý QUAN TRỌNG"
                )
                else -> Tuple5(
                    Color(0xFF031129),
                    Color(0xFF00F0FF),
                    Icons.Default.NotificationsActive,
                    Color(0xFF00F0FF),
                    "THÔNG BÁO HỆ THỐNG"
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                border = BorderStroke(1.5.dp, borderColor),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = headerTitle,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = iconColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = message,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 15.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
