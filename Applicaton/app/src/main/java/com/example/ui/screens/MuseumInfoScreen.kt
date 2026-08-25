package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.GeminiClient
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MuseumViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ZoomIn

data class MuseumChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuseumInfoScreen(
    viewModel: MuseumViewModel,
    modifier: Modifier = Modifier
) {
    val cyanColor = Color(0xFF00F0FF)
    val goldColor = Color(0xFFFFAA00)
    val darkBgColor = Color(0xFF070F1E) // Deep dark space background
    val darkCardColor = Color(0xFF0F172A)
    val borderCyanColor = Color(0x3300F0FF)
    val scrollState = rememberScrollState()

    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1D4ED8), // Cobalt Blue
            Color(0xFFD97706), // Yellow/Gold
            Color(0xFFDC2626)  // Crimson Red
        )
    )

    val context = LocalContext.current
    var showLogoDialog by remember { mutableStateOf(false) }

    fun downloadLogoToGallery() {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.img_museum_seal_3d_1785060476591)
            val filename = "Logo_BaoTang_QK9_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BaoTangQK9")
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
                Toast.makeText(context, "✅ Đã tải và lưu ảnh Logo Bảo Tàng vào Thư viện ảnh!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "❌ Không thể tạo file lưu ảnh!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Lỗi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Floating Draggable Gemini Chatbot States
    var isChatOpen by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()
    val chatMessages = remember {
        mutableStateListOf(
            MuseumChatMessage(
                sender = "gemini",
                text = "<strong>Xin chào!</strong> Tôi là <strong>Trợ lý AI Gemini</strong> chính thức của Bảo tàng Quân khu 9 (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ).<br><br>Tôi có thể giúp bạn tìm hiểu thông tin giờ mở cửa, vị trí hay 15 cổ vật trưng bày ở đây!"
            )
        )
    }
    var userInputText by remember { mutableStateOf("") }
    var isGeminiLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto scroll chat to bottom
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun sendToGemini(promptText: String) {
        if (promptText.isBlank() || isGeminiLoading) return

        val userMsg = MuseumChatMessage(sender = "user", text = promptText)
        chatMessages.add(userMsg)
        userInputText = ""
        isGeminiLoading = true

        coroutineScope.launch {
            val systemPrompt = """
                Bạn là Trợ lý AI Hướng dẫn viên chính thức tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ).
                Đang giải đáp câu hỏi của khách tham quan tại Màn hình Thông tin Bảo tàng.
                YÊU CẦU BẮC BUỘC:
                - Trả lời ĐÚNG TRỌNG TÂM, KHÔNG dài dòng rườm rà.
                - Trình bày đẹp mắt, dùng thẻ <strong> cho các ý chính (Giờ mở cửa, Giá vé, Vị trí, Cổ vật).
                - Ngắn gọn súc tích trong 2-4 câu.
            """.trimIndent()

            val response = GeminiClient.getChatbotResponse(promptText, systemPrompt)
            chatMessages.add(MuseumChatMessage(sender = "gemini", text = response))
            isGeminiLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "THÔNG TIN BẢO TÀNG",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.Welcome) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = darkBgColor,
                        titleContentColor = Color.White
                    )
                )
            },
            containerColor = darkBgColor
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(darkBgColor)
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glowing modern logo block with Download capability
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(darkCardColor)
                            .border(2.dp, cyanColor, RoundedCornerShape(24.dp))
                            .clickable { showLogoDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_museum_seal_3d_1785060476591),
                            contentDescription = "Logo Bảo Tàng 3D",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Xem lớn",
                                tint = cyanColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Download Button
                    Button(
                        onClick = { downloadLogoToGallery() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F2B48)),
                        border = BorderStroke(1.dp, cyanColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = cyanColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TẢI ẢNH LOGO VỀ MÁY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = cyanColor
                        )
                    }
                }

                Text(
                    text = "BẢO TÀNG LỊCH SỬ & DI SẢN SỐ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    letterSpacing = 0.5.sp
                )

                // Description block
                Text(
                    text = "Bảo tàng là nơi lưu trữ và trưng bày hàng ngàn di vật, cổ vật gắn liền với các mốc son chói lọi trong lịch sử dân tộc. Ứng dụng mang đến các thử thách tương tác thực địa 3D sống động và trợ lý ảo AI thông tin chuẩn xác nhất.",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // Info blocks (High-tech cyber cards style)
                InfoRowCard(
                    icon = Icons.Default.LocationOn,
                    title = "Vị trí địa lý",
                    content = "Khu di tích lịch sử đặc biệt, Trung tâm chỉ huy Quân khu 9, TP. Cần Thơ, Việt Nam.",
                    accentColor = cyanColor,
                    cardBg = darkCardColor,
                    borderCol = borderCyanColor
                )

                InfoRowCard(
                    icon = Icons.Default.CalendarToday,
                    title = "Giờ mở cửa",
                    content = "Tất cả các ngày trong tuần (Trừ thứ Hai).\nSáng: 08:00 - 11:30 | Chiều: 13:30 - 17:00.",
                    accentColor = goldColor,
                    cardBg = darkCardColor,
                    borderCol = borderCyanColor
                )

                InfoRowCard(
                    icon = Icons.Default.Info,
                    title = "Công nghệ tương tác",
                    content = "• Sơ đồ định vị: Lộ trình Hamilton liên kết 15 bảo vật độc đáo.\n• Trợ lý ảo AI: Phân tích ảnh chụp hiện vật và đàm thoại thời gian thực.",
                    accentColor = cyanColor,
                    cardBg = darkCardColor,
                    borderCol = borderCyanColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful Gradient Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(buttonGradient)
                        .clickable { viewModel.navigateTo(AppScreen.Login) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BẮT ĐẦU KHÁM PHÁ NGAY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // FLOATING DRAGGABLE AI CHATBOT BUTTON ("Con chat trôi lềnh bềnh")
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Surface(
                onClick = { isChatOpen = !isChatOpen },
                shape = CircleShape,
                color = Color(0xFF0F172A),
                border = BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF3B82F6)))),
                shadowElevation = 12.dp,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Gemini Chatbot",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(28.dp)
                    )

                    // Small Glowing AI Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // FLOATING GEMINI CHAT DIALOG
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isChatOpen,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(480.dp)
                    .shadow(24.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFB0A1120)),
                border = BorderStroke(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0F1E36), Color(0xFF1E293B))))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF1E3A8A))))
                                    .border(1.dp, Color(0xFF00F0FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI GEMINI BẢO TÀNG QK9",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00F0FF),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Hỏi đáp & Thuyết minh trực tuyến",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        IconButton(
                            onClick = { isChatOpen = false },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng chat",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quick prompt chips bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(Color(0xFF080E1A))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickPrompts = listOf(
                            "🏛️ Giới thiệu Bảo tàng" to "Giới thiệu tổng quan Bảo tàng Quân khu 9 Cần Thơ.",
                            "📍 Vị trí & Địa chỉ" to "Địa chỉ chính xác và cách di chuyển đến Bảo tàng QK9?",
                            "⏰ Giờ mở cửa" to "Bảo tàng QK9 mở cửa những ngày nào và khung giờ nào?",
                            "📜 15 Cổ vật tiêu biểu" to "Danh sách các cổ vật nổi bật nhất tại Bảo tàng QK9?"
                        )

                        quickPrompts.forEach { (label, promptText) ->
                            Surface(
                                onClick = { sendToGemini(promptText) },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF0F1E36),
                                border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                    // Chat messages scrollable area
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { msg ->
                            MuseumChatBubble(message = msg)
                        }

                        if (isGeminiLoading) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color(0xFF00F0FF),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini AI đang trả lời...",
                                        fontSize = 11.sp,
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                    // Input & Send Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B132B))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userInputText,
                            onValueChange = { userInputText = it },
                            placeholder = { Text("Hỏi AI Gemini về bảo tàng...", fontSize = 11.sp, color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF152238),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { sendToGemini(userInputText) },
                            enabled = userInputText.isNotBlank() && !isGeminiLoading,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (userInputText.isNotBlank() && !isGeminiLoading) Color(0xFF0284C7) else Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi câu hỏi",
                                tint = if (userInputText.isNotBlank() && !isGeminiLoading) Color.White else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen/Enlarged Logo Image Dialog
        if (showLogoDialog) {
            AlertDialog(
                onDismissRequest = { showLogoDialog = false },
                containerColor = Color(0xFF0F172A),
                title = {
                    Text(
                        "BIỂU TƯỢNG LOGO BẢO TÀNG",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F0FF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(20.dp))
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.img_museum_seal_3d_1785060476591),
                                contentDescription = "Logo Bảo Tàng 3D",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { downloadLogoToGallery() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tải về Thư viện", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoDialog = false }) {
                        Text("Đóng", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun MuseumChatBubble(message: MuseumChatMessage) {
    val isUser = message.sender == "user"
    val goldColor = Color(0xFFFFD700)
    val cyanColor = Color(0xFF00E5FF)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF1D4ED8) else Color(0xFF0F182A)
            ),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            border = if (isUser) null else BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.35f)),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (!isUser) {
                    Text(
                        text = "✨ AI GEMINI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                val annotatedString = remember(message.text, isUser) {
                    buildAnnotatedString {
                        val processed = message.text.replace("<br>", "\n").replace("<br/>", "\n")
                        val tagRegex = Regex("(<strong>(.*?)</strong>|\\*\\*(.*?)\\*\\*|<code>(.*?)</code>|`(.*?)`)", RegexOption.IGNORE_CASE)

                        var currentIndex = 0
                        tagRegex.findAll(processed).forEach { match ->
                            val range = match.range
                            if (range.first > currentIndex) {
                                append(processed.substring(currentIndex, range.first))
                            }

                            val fullMatch = match.value
                            when {
                                fullMatch.startsWith("<strong>", ignoreCase = true) -> {
                                    val inner = match.groupValues[2]
                                    withStyle(SpanStyle(color = if (isUser) Color.White else goldColor, fontWeight = FontWeight.ExtraBold)) {
                                        append(inner)
                                    }
                                }
                                fullMatch.startsWith("**") -> {
                                    val inner = match.groupValues[3]
                                    withStyle(SpanStyle(color = if (isUser) Color.White else goldColor, fontWeight = FontWeight.ExtraBold)) {
                                        append(inner)
                                    }
                                }
                                fullMatch.startsWith("<code>", ignoreCase = true) -> {
                                    val inner = match.groupValues[4]
                                    withStyle(SpanStyle(color = cyanColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, background = Color(0xFF0F2B48))) {
                                        append(" $inner ")
                                    }
                                }
                                fullMatch.startsWith("`") -> {
                                    val inner = match.groupValues[5]
                                    withStyle(SpanStyle(color = cyanColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, background = Color(0xFF0F2B48))) {
                                        append(" $inner ")
                                    }
                                }
                            }
                            currentIndex = range.last + 1
                        }

                        if (currentIndex < processed.length) {
                            append(processed.substring(currentIndex))
                        }
                    }
                }

                Text(
                    text = annotatedString,
                    fontSize = 12.sp,
                    color = if (isUser) Color.White else Color(0xFFE2E8F0),
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InfoRowCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    accentColor: Color,
    cardBg: Color,
    borderCol: Color
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = content,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

