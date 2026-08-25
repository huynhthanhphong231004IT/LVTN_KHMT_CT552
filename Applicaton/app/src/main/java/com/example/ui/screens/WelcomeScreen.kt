package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MuseumViewModel

@Composable
fun WelcomeScreen(
    viewModel: MuseumViewModel,
    modifier: Modifier = Modifier
) {
    // Premium theme colors for a highly modern military/tactical cyber look
    val cyanColor = Color(0xFF00F0FF)
    val goldColor = Color(0xFFFFAA00)
    val redColor = Color(0xFFEF4444)
    val darkBgColor = Color(0xFF070F1E) // Sleek deep space military command background
    val darkCardColor = Color(0xFF0F172A)

    // Three-color gradient requested by the user
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1D4ED8), // Modern rich Cobalt Blue
            Color(0xFFD97706), // Gold/Yellow
            Color(0xFFDC2626)  // Crimson Red
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBgColor)
    ) {
        // High-tech circular radar/bronze-drum visual elements in the background
        Canvas(
            modifier = Modifier
                .size(420.dp)
                .align(Alignment.Center)
                .offset(y = (-30).dp)
        ) {
            val baseRadius = size.width / 2
            
            drawCircle(
                color = cyanColor.copy(alpha = 0.04f),
                radius = baseRadius,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = goldColor.copy(alpha = 0.05f),
                radius = baseRadius * 0.75f,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = cyanColor.copy(alpha = 0.07f),
                radius = baseRadius * 0.5f,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = redColor.copy(alpha = 0.09f),
                radius = baseRadius * 0.25f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Main branding header area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Futuristic Glowing Museum Icon Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, cyanColor, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Museum,
                        contentDescription = "Museum Logo",
                        tint = cyanColor,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BẢO TÀNG DI SẢN SỐ",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Hệ thống khám phá thực địa & Trợ lý ảo AI thông minh",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Premium Modern Action buttons (Optimized for no text overflows on Redmi Note 11)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Secondary Button: Info (Sleek outline with cyan border)
                Button(
                    onClick = { viewModel.navigateTo(AppScreen.MuseumInfo) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("museum_info_button")
                        .border(1.dp, cyanColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = cyanColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info Icon",
                            tint = cyanColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Thông Tin Bảo Tàng",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Primary Action Button: Enter (Futuristic Multi-color Gradient Button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(buttonGradient)
                        .clickable { viewModel.navigateTo(AppScreen.Login) }
                        .testTag("login_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = "Login Icon",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Bắt Đầu Trải Nghiệm",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer
            Text(
                text = "Hệ thống Nghiên cứu Di sản & Công nghệ số Quân khu 9\nBảo lưu mọi quyền © 2026",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}
