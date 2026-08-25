package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Artifact

@Composable
fun StatChart(
    completedIds: Set<Int>,
    artifacts: List<Artifact>,
    modifier: Modifier = Modifier
) {
    // Phân loại 15 cổ vật thành 3 nhóm lớn
    // Nhóm 1: Vũ khí chiến đấu (Bom, Đạn tên lửa, Pháo, Súng thần công, Xe tăng) -> IDs: 1, 2, 8, 9, 14
    // Nhóm 2: Phương tiện di chuyển (Ghe xuồng, Mỏ neo, Tàu tuần tiễu, Xe bọc thép, Xe Peugeot, Máy bay trực thăng) -> IDs: 3, 7, 11, 12, 13, 15
    // Nhóm 3: Hậu cần & Di sản chiến khu (Lu hầm, Máy cán tol, Máy in, Trục máy bay B52) -> IDs: 4, 5, 6, 10
    
    val weaponIds = setOf(1, 2, 8, 9, 14)
    val vehicleIds = setOf(3, 7, 11, 12, 13, 15)
    val relicIds = setOf(4, 5, 6, 10)

    val weaponTotal = weaponIds.size
    val vehicleTotal = vehicleIds.size
    val relicTotal = relicIds.size

    val weaponCompleted = completedIds.count { it in weaponIds }
    val vehicleCompleted = completedIds.count { it in vehicleIds }
    val relicCompleted = completedIds.count { it in relicIds }

    val weaponPercent = if (weaponTotal > 0) weaponCompleted.toFloat() / weaponTotal else 0f
    val vehiclePercent = if (vehicleTotal > 0) vehicleCompleted.toFloat() / vehicleTotal else 0f
    val relicPercent = if (relicTotal > 0) relicCompleted.toFloat() / relicTotal else 0f

    val animWeapon by animateFloatAsState(targetValue = weaponPercent, animationSpec = tween(1000), label = "weapon")
    val animVehicle by animateFloatAsState(targetValue = vehiclePercent, animationSpec = tween(1000), label = "vehicle")
    val animRelic by animateFloatAsState(targetValue = relicPercent, animationSpec = tween(1000), label = "relic")

    val goldAccent = Color(0xFFF3C623) // Majestic Gold
    val bronzeAccent = Color(0xFFD32F2F) // Vibrant Red
    val jadeAccent = Color(0xFF00B0FF) // Electric Blue
    val marbleBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Thống Kê Khám Phá Di Sản",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Đã hoàn thành ${completedIds.size}/15 trò chơi cổ vật",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Vòng tròn tổng tiến trình nhỏ gọn
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(54.dp)
                ) {
                    val progressPercent = (completedIds.size.toFloat() / 15f)
                    val animProgress by animateFloatAsState(targetValue = progressPercent, animationSpec = tween(1000), label = "progress")
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = marbleBg,
                            style = Stroke(width = 6.dp.toPx())
                        )
                        drawArc(
                            color = goldAccent,
                            startAngle = -90f,
                            sweepAngle = animProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldAccent
                    )
                }
            }

            // Đồ thị dạng cột ngang tùy chỉnh siêu đẹp
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hàng 1: Vũ Khí Chiến Đấu
                StatBar(
                    title = "Vũ Khí Chiến Đấu",
                    progress = animWeapon,
                    displayValue = "$weaponCompleted/$weaponTotal",
                    accentColor = bronzeAccent,
                    trackColor = marbleBg
                )

                // Hàng 2: Phương Tiện Quân Sự
                StatBar(
                    title = "Phương Tiện Quân Sự",
                    progress = animVehicle,
                    displayValue = "$vehicleCompleted/$vehicleTotal",
                    accentColor = goldAccent,
                    trackColor = marbleBg
                )

                // Hàng 3: Hậu Cần & Di Tích Hầm Hào
                StatBar(
                    title = "Hậu Cần & Di Tích",
                    progress = animRelic,
                    displayValue = "$relicCompleted/$relicTotal",
                    accentColor = jadeAccent,
                    trackColor = marbleBg
                )
            }
        }
    }
}

@Composable
fun StatBar(
    title: String,
    progress: Float,
    displayValue: String,
    accentColor: Color,
    trackColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Text(
                text = displayValue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            val width = size.width
            val height = size.height
            val radius = height / 2

            // Vẽ thanh nền (Track)
            drawRoundRect(
                color = trackColor,
                size = Size(width, height),
                cornerRadius = CornerRadius(radius, radius)
            )

            // Vẽ thanh tiến trình (Progress Bar)
            if (progress > 0f) {
                val progressWidth = width * progress
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.7f),
                            accentColor
                        )
                    ),
                    size = Size(progressWidth, height),
                    cornerRadius = CornerRadius(radius, radius)
                )
            }
        }
    }
}
