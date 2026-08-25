package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Label
import com.example.viewmodel.CardCollectData
import kotlinx.coroutines.launch

@Composable
fun CardCollect360Overlay(
    cardData: CardCollectData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (cardData == null) return

    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(0.1f) }
    val rotationY = remember { Animatable(0f) }
    val rotationZ = remember { Animatable(0f) }
    val translateX = remember { Animatable(0f) }
    val translateY = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    val bitmap = remember(cardData) {
        try {
            if (!cardData.imageStr.startsWith("FALLBACK_") &&
                !cardData.imageStr.startsWith("MẢNH_") &&
                cardData.imageStr.length >= 50
            ) {
                val cleanStr = cardData.imageStr.substringAfter("base64,")
                val decodedBytes = android.util.Base64.decode(cleanStr, android.util.Base64.DEFAULT)
                val bmp = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bmp != null) bmp else com.example.ui.screens.generateGamePieceBitmap(
                    cardData.imageStr,
                    cardData.artifactId,
                    cardData.pieceIndex
                )
            } else {
                com.example.ui.screens.generateGamePieceBitmap(
                    cardData.imageStr,
                    cardData.artifactId,
                    cardData.pieceIndex
                )
            }
        } catch (e: Exception) {
            com.example.ui.screens.generateGamePieceBitmap(
                cardData.imageStr,
                cardData.artifactId,
                cardData.pieceIndex
            )
        }
    }

    LaunchedEffect(cardData) {
        // Reset state
        scale.snapTo(0.15f)
        rotationY.snapTo(0f)
        rotationZ.snapTo(-20f)
        translateX.snapTo(0f)
        translateY.snapTo(0f)
        alpha.snapTo(1f)

        // Phase 1: Zoom in big (1.35x) & 360 Rotation
        launch {
            scale.animateTo(
                targetValue = 1.35f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow)
            )
        }
        launch {
            rotationY.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
            )
        }
        launch {
            rotationZ.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        kotlinx.coroutines.delay(1300)

        // Phase 2: Shrink and fly down to bottom right cart icon!
        launch {
            scale.animateTo(
                targetValue = 0.05f,
                animationSpec = tween(durationMillis = 800, easing = FastOutLinearInEasing)
            )
        }
        launch {
            translateX.animateTo(
                targetValue = 340f,
                animationSpec = tween(durationMillis = 800, easing = FastOutLinearInEasing)
            )
        }
        launch {
            translateY.animateTo(
                targetValue = 620f,
                animationSpec = tween(durationMillis = 800, easing = FastOutLinearInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        kotlinx.coroutines.delay(850)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f * alpha.value))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Decorative background aura glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .graphicsLayer {
                    this.scaleX = scale.value * 1.2f
                    this.scaleY = scale.value * 1.2f
                    this.translationX = translateX.value
                    this.translationY = translateY.value
                    this.alpha = alpha.value * 0.6f
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.8f),
                            Color(0xFF00F0FF).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Main Heritage Trading Card
        Surface(
            modifier = Modifier
                .width(260.dp)
                .graphicsLayer {
                    this.scaleX = scale.value
                    this.scaleY = scale.value
                    this.rotationY = rotationY.value
                    this.rotationZ = rotationZ.value
                    this.translationX = translateX.value
                    this.translationY = translateY.value
                    this.alpha = alpha.value
                    this.cameraDistance = 12f * density
                },
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(3.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00F0FF), Color(0xFFFF007F)))),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Badge Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "THẺ DI SẢN KHẢO CỔ",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700),
                            letterSpacing = 0.5.sp,
                            softWrap = true
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Collected",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Card Image Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = remember(cardData) {
                        com.example.network.LocalClient.resolveImageUrl(cardData.imageStr, cardData.artifactId)
                    }

                    if (imageUrl != null) {
                        coil.compose.SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = "Collected Piece",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Collected Piece",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            },
                            error = {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Collected Piece",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        )
                    } else if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Collected Piece",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Watermark badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Text("ĐÃ GIẢI MÃ", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }

                // Title & Description (Wrapped cleanly)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = cardData.artifactName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "🎉 Đã thu thập thành công mảnh ảnh di sản vào Giỏ Hàng!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38BDF8),
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingCartButton(
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFFFFD700),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp),
            modifier = Modifier
                .size(58.dp)
                .scale(if (itemCount > 0) pulseScale else 1f)
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00F0FF))), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Giỏ Hàng Mảnh Ảnh",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(28.dp)
            )
        }

        if (itemCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp),
                shape = CircleShape,
                color = Color(0xFFEF4444),
                border = BorderStroke(1.5.dp, Color.White)
            ) {
                Text(
                    text = if (itemCount > 99) "99+" else "$itemCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectedCartModalDialog(
    isOpen: Boolean,
    collectedMap: Map<Int, List<String>>,
    artifacts: List<com.example.data.Artifact>,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val totalPieces = collectedMap.values.sumOf { it.size }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.85f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A))
            .border(1.5.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFF00F0FF))), RoundedCornerShape(24.dp)),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "GIỎ HÀNG DI SẢN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "Tổng thu thập: $totalPieces mảnh ảnh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            if (totalPieces == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                        Text(
                            text = "CHƯA CÓ MẢNH ẢNH NÀO!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Giỏ hàng hiện tại đang trống. Hãy chọn các ải cổ vật và trả lời đúng câu hỏi trắc nghiệm để thu thập & giải mã mảnh ảnh di sản nhé!",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Flatten all collected pieces into a single list with artifact badges
                data class PieceItem(val artId: Int, val artName: String, val index: Int, val base64: String)
                val allPieces = mutableListOf<PieceItem>()
                collectedMap.forEach { (artId, list) ->
                    val artName = artifacts.find { it.id == artId }?.name ?: "Cổ vật #$artId"
                    list.forEachIndexed { idx, str ->
                        if (str.isNotBlank()) {
                            allPieces.add(PieceItem(artId, artName, idx, str))
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📦 BỘ BẢO TÀNG TỔNG HỢP MẢNH ÁNH (${allPieces.size} mảnh):",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(allPieces) { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.Black)
                                    ) {
                                        com.example.ui.screens.Base64Thumbnail(
                                            base64Str = item.base64,
                                            modifier = Modifier.fillMaxSize(),
                                            artifactId = item.artId,
                                            pieceIndex = item.index
                                        )

                                        // Badge top start: Piece #
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.8f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFFFFD700)),
                                            modifier = Modifier.padding(4.dp).align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "Mảnh #${item.index + 1}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFD700),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        // Watermark badge bottom: Artifact Name
                                        Surface(
                                            color = Color(0xCC0F172A),
                                            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Label,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00F0FF),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = item.artName,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF00F0FF),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
