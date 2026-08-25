package com.example.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MuseumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MuseumViewModel,
    modifier: Modifier = Modifier
) {
    val cyanColor = Color(0xFF00F0FF)
    val goldColor = Color(0xFFFFAA00)
    val redColor = Color(0xFFEF4444)
    val darkBgColor = Color(0xFF070F1E) // Deep dark space background
    val darkCardColor = Color(0xFF0F172A)
    val borderCyanColor = Color(0x3300F0FF)

    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1D4ED8), // Cobalt Blue
            Color(0xFFD97706), // Yellow/Gold
            Color(0xFFDC2626)  // Crimson Red
        )
    )

    var visitorName by remember { mutableStateOf("") }
    var visitorEmail by remember { mutableStateOf("") }
    val defaultTicket = "MUSEAI-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"))
    var ticketCode by remember { mutableStateOf(defaultTicket) }
    var errorMessage by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    var showIpDialog by remember { mutableStateOf(false) }
    var tempIpAddress by remember { mutableStateOf(com.example.network.LocalClient.getBaseIp()) }
    var tempPortChat by remember { mutableStateOf(com.example.network.LocalClient.getChatPort()) }
    var tempPortPredict by remember { mutableStateOf(com.example.network.LocalClient.getPredictPort()) }
    var tempPortDecrypt by remember { mutableStateOf(com.example.network.LocalClient.getDecryptPort()) }
    var tempPortQuestion by remember { mutableStateOf(com.example.network.LocalClient.getQuestionPort()) }
    var tempPortImage by remember { mutableStateOf(com.example.network.LocalClient.getImagePort()) }
    var tempPortLeaderboard by remember { mutableStateOf(com.example.network.LocalClient.getLeaderboardPort()) }

    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = {
                Text(
                    text = "CẤU HÌNH HỆ THỐNG API",
                    color = cyanColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nhập địa chỉ IP/Hostname và các cổng dịch vụ bên dưới.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )

                    OutlinedTextField(
                        value = tempIpAddress,
                        onValueChange = { tempIpAddress = it },
                        label = { Text("IP Máy Chủ (Base IP)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = cyanColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempPortChat,
                            onValueChange = { tempPortChat = it },
                            label = { Text("Cổng Chat (8000)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = cyanColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempPortPredict,
                            onValueChange = { tempPortPredict = it },
                            label = { Text("Cổng AI (8001)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = cyanColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempPortDecrypt,
                            onValueChange = { tempPortDecrypt = it },
                            label = { Text("Cổng Giải Mã (8003)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = cyanColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempPortQuestion,
                            onValueChange = { tempPortQuestion = it },
                            label = { Text("Cổng Câu Hỏi (8004)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = cyanColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempPortImage,
                            onValueChange = { tempPortImage = it },
                            label = { Text("Cổng Ảnh (8005)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = cyanColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempPortLeaderboard,
                            onValueChange = { tempPortLeaderboard = it },
                            label = { Text("Cổng Xếp Hạng (8006)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedLabelColor = cyanColor,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            containerColor = darkCardColor,
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateServerSettings(
                            tempIpAddress,
                            tempPortChat,
                            tempPortPredict,
                            tempPortDecrypt,
                            tempPortQuestion,
                            tempPortImage,
                            tempPortLeaderboard
                        )
                        showIpDialog = false
                    }
                ) {
                    Text("LƯU CẤU HÌNH", color = cyanColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            tempIpAddress = "http://10.158.209.106"
                            tempPortChat = "8000"
                            tempPortPredict = "8001"
                            tempPortDecrypt = "8003"
                            tempPortQuestion = "8004"
                            tempPortImage = "8005"
                            tempPortLeaderboard = "8006"
                            viewModel.updateServerSettings(
                                "http://10.158.209.106",
                                "8000",
                                "8001",
                                "8003",
                                "8004",
                                "8005",
                                "8006"
                            )
                            showIpDialog = false
                        }
                    ) {
                        Text("MẶC ĐỊNH", color = Color.Gray)
                    }
                    TextButton(onClick = { showIpDialog = false }) {
                        Text("HỦY", color = Color.Gray)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ĐĂNG NHẬP KHÁM PHÁ", 
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
                actions = {
                    IconButton(onClick = { 
                        tempIpAddress = com.example.network.LocalClient.getBaseIp()
                        tempPortChat = com.example.network.LocalClient.getChatPort()
                        tempPortPredict = com.example.network.LocalClient.getPredictPort()
                        tempPortDecrypt = com.example.network.LocalClient.getDecryptPort()
                        tempPortQuestion = com.example.network.LocalClient.getQuestionPort()
                        tempPortImage = com.example.network.LocalClient.getImagePort()
                        tempPortLeaderboard = com.example.network.LocalClient.getLeaderboardPort()
                        showIpDialog = true 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cấu hình IP Máy chủ",
                            tint = cyanColor
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))

            // Electronic Digital Ticket (High tech design card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderCyanColor, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = darkCardColor),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VÉ ĐIỆN TỬ DI SẢN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = cyanColor,
                        letterSpacing = 1.5.sp
                    )
                    
                    Text(
                        text = "Hệ thống sẽ đồng bộ hóa lộ trình khám phá thực địa cùng trợ lý ảo thông minh.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    HorizontalDivider(
                        color = Color(0xFF1E293B),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Ô nhập họ tên du khách
                    OutlinedTextField(
                        value = visitorName,
                        onValueChange = { 
                            visitorName = it
                            errorMessage = ""
                        },
                        label = { Text("Họ tên") },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = cyanColor
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = cyanColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("visitor_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Ô nhập Email
                    OutlinedTextField(
                        value = visitorEmail,
                        onValueChange = { 
                            visitorEmail = it
                            errorMessage = ""
                        },
                        label = { Text("Email") },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = cyanColor
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = cyanColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("visitor_email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Ô nhập mã số vé (giả lập hoặc tự chọn)
                    OutlinedTextField(
                        value = ticketCode,
                        onValueChange = { 
                            ticketCode = it
                            errorMessage = ""
                        },
                        label = { Text("Mã số vé bảo tàng") },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = cyanColor
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanColor,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = cyanColor,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ticket_code_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = redColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tactical Gradient Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(buttonGradient)
                            .clickable {
                                val trimmedEmail = visitorEmail.trim()
                                val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
                                if (trimmedEmail.isEmpty()) {
                                    errorMessage = "Vui lòng nhập địa chỉ email để nhận báo cáo!"
                                    return@clickable
                                }
                                if (!emailPattern.matches(trimmedEmail)) {
                                    errorMessage = "Định dạng email không hợp lệ (ví dụ: ten@gmail.com)!"
                                    return@clickable
                                }
                                val name = if (visitorName.trim().isEmpty()) "Khách Tham Quan" else visitorName.trim()
                                val ticket = if (ticketCode.trim().isEmpty()) "VIP-2026" else ticketCode.trim()
                                viewModel.login(name, trimmedEmail, ticket)
                            }
                            .testTag("submit_login_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "XÁC NHẬN & VÀO CỔNG",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Mẹo: Bạn có thể nhập bất kỳ tên và mã vé nào để trải nghiệm toàn bộ trò chơi khám phá thực địa 3D.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
