package com.example.ui.screens

import android.net.Uri
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.Artifact
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.MessageSender
import com.example.viewmodel.MuseumViewModel
import com.example.viewmodel.getGroupName
import com.example.viewmodel.getGroupAndLettersForArtifact
import com.example.viewmodel.secretCandidatesList
import com.example.viewmodel.getGroupPuzzleName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.abs

@Composable
fun Base64Thumbnail(
    base64Str: String,
    modifier: Modifier = Modifier,
    artifactId: Int = 1,
    pieceIndex: Int = 0
) {
    val imageUrl = remember(base64Str, artifactId) {
        com.example.network.LocalClient.resolveImageUrl(base64Str, artifactId)
    }

    if (imageUrl != null) {
        coil.compose.SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = "Piece",
            modifier = modifier,
            contentScale = ContentScale.Crop,
            loading = {
                val fallbackBmp = remember(base64Str, artifactId, pieceIndex) {
                    generatePieceBitmap(base64Str, artifactId, pieceIndex)
                }
                androidx.compose.foundation.Image(
                    bitmap = fallbackBmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            },
            error = {
                val fallbackBmp = remember(base64Str, artifactId, pieceIndex) {
                    generatePieceBitmap(base64Str, artifactId, pieceIndex)
                }
                androidx.compose.foundation.Image(
                    bitmap = fallbackBmp.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        )
    } else {
        val bitmap = remember(base64Str, artifactId, pieceIndex) {
            try {
                if (!base64Str.startsWith("FALLBACK_") && !base64Str.startsWith("MẢNH_") && base64Str.length >= 50) {
                    val cleanStr = base64Str.substringAfter("base64,")
                    val decodedBytes = android.util.Base64.decode(cleanStr, android.util.Base64.DEFAULT)
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bmp != null) bmp else generatePieceBitmap(base64Str, artifactId, pieceIndex)
                } else {
                    generatePieceBitmap(base64Str, artifactId, pieceIndex)
                }
            } catch (e: Exception) {
                generatePieceBitmap(base64Str, artifactId, pieceIndex)
            }
        }

        androidx.compose.foundation.Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Piece",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

fun generatePieceBitmap(base64Str: String, artifactId: Int, pieceIndex: Int): android.graphics.Bitmap {
    return com.example.ui.screens.generateGamePieceBitmap(base64Str, artifactId, pieceIndex)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MuseumViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // central cyberpunk color palette
    val darkBackground = Color(0xFF030712)
    val goldColor = Color(0xFFFFAA00)
    val cyanColor = Color(0xFF00F0FF)
    val pinkColor = Color(0xFFFF007F)
    val emeraldColor = Color(0xFF10B981)
    val borderCyan = Color(0x6600F0FF)

    // Camera capture contracts for real photos
    val routeStartCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            try {
                val tempFile = File(context.cacheDir, "start_route_camera_img.jpg")
                java.io.FileOutputStream(tempFile).use { out ->
                    it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                viewModel.uploadAndPredictImage(tempFile)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi lưu ảnh chụp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val chatCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            try {
                val tempFile = File(context.cacheDir, "chat_camera_img.jpg")
                java.io.FileOutputStream(tempFile).use { out ->
                    it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                viewModel.setChatSelectedImage(Uri.fromFile(tempFile).toString())
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi lưu ảnh chụp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // TTS Setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
            }
        }
        ttsInstance.language = Locale("vi", "VN")
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Image Pickers
    val routeStartPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val tempFile = File(context.cacheDir, "start_route_img.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                viewModel.uploadAndPredictImage(tempFile)
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi đọc tệp tin: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val chatImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.setChatSelectedImage(uri.toString())
        }
    }

    // Mock picker list state
    var showMockSelectorDialog by remember { mutableStateOf(false) }

    var dashboardTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(dashboardTab) {
        viewModel.setTab(dashboardTab)
        if (dashboardTab == 3) {
            viewModel.startDecoderTimer()
        }
    }
    var showTopMenuDropdown by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isWide = configuration.screenWidthDp >= 800

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(emeraldColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HÀNH TRÌNH QK9",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Profile/User Score Badge
                    Row(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, borderCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Score",
                            tint = goldColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.totalScore} Điểm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = goldColor
                        )
                    }

                    // Mobile Responsive Dropdown Menu Button
                    Box {
                        IconButton(
                            onClick = { showTopMenuDropdown = true },
                            modifier = Modifier.testTag("app_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu tùy chọn",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showTopMenuDropdown,
                            onDismissRequest = { showTopMenuDropdown = false },
                            modifier = Modifier
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lộ Trình Tham Quan", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.AltRoute, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showTopMenuDropdown = false
                                    viewModel.showRouteModal(true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bảng Xếp Hạng", color = goldColor, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = goldColor, modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showTopMenuDropdown = false
                                    viewModel.showLeaderboardModal(true)
                                }
                            )
                            HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text("Kết Thúc Thám Hiểm", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showTopMenuDropdown = false
                                    viewModel.endGameAndSendMail(isAutoTimeout = false)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Đăng Xuất Tài Khoản", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) },
                                onClick = {
                                    showTopMenuDropdown = false
                                    viewModel.logout()
                                },
                                modifier = Modifier.testTag("logout_button")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF090E1A)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Ambient tech background canvas drawing
            CyberGridBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                // High-tech responsive Nav-Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "Bản đồ", Icons.Default.Map),
                        Triple(1, "Trợ lý AI", Icons.Default.Chat),
                        Triple(2, "Phòng 3D", Icons.Default.Autorenew),
                        Triple(3, "Giải mã", Icons.Default.Extension)
                    )
                    
                    val selectedGradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1D4ED8), // Blue
                            Color(0xFFD97706), // Yellow-Gold
                            Color(0xFFDC2626)  // Red
                        )
                    )

                    tabs.forEach { (index, title, icon) ->
                        val isSelected = dashboardTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (isSelected) Modifier.background(selectedGradient)
                                    else Modifier.background(Color.Transparent)
                                )
                                .clickable { dashboardTab = index }
                                .padding(horizontal = 2.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Clip
                                )
                            }
                        }
                    }
                }

                // Render Active Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (dashboardTab) {
                        0 -> {
                            NavigatorPanel(
                                viewModel = viewModel,
                                state = state,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        1 -> {
                            ChatPanel(
                                viewModel = viewModel,
                                state = state,
                                tts = tts,
                                routeStartPickerLauncher = {
                                    viewModel.setImageSourceDialog(true, "start")
                                },
                                chatImagePickerLauncher = {
                                    viewModel.setImageSourceDialog(true, "chat")
                                },
                                onShowMockSelector = { showMockSelectorDialog = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        2 -> {
                            ArtifactRewards3DShowcase(
                                state = state,
                                viewModel = viewModel
                            )
                        }
                        3 -> {
                            SecretDecoderPanel(
                                viewModel = viewModel,
                                state = state,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Elevated floating HUD panel to prevent blocking of bottom chat inputs
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                HUDControlOverlay(
                    viewModel = viewModel,
                    state = state,
                    goldColor = goldColor,
                    cyanColor = cyanColor,
                    emeraldColor = emeraldColor
                )
            }

            // MODALS / DIALOGS
            if (state.showRankRules) {
                RulesModal(onDismiss = { viewModel.showRulesModal(false) })
            }

            if (state.showRouteModal) {
                RouteModal(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { viewModel.showRouteModal(false) }
                )
            }

            if (state.showLeaderboardModal) {
                LeaderboardModal(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { viewModel.showLeaderboardModal(false) }
                )
            }

            if (state.showHistoryLog) {
                HistoryLogModal(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { viewModel.showHistoryLog(false) }
                )
            }

            if (state.showSummaryModal) {
                SummaryModal(
                    state = state,
                    viewModel = viewModel,
                    goldColor = goldColor,
                    cyanColor = cyanColor,
                    pinkColor = pinkColor,
                    onDismiss = { viewModel.closeSummary() }
                )
            }

            if (state.showEndGameModal) {
                EndGameModal(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = {}
                )
            }

            if (showMockSelectorDialog) {
                MockArtifactSelector(
                    state = state,
                    onSelect = { artifactId ->
                        val nodeId = viewModel.artifactIdToNodeId(artifactId)
                        viewModel.uploadAndPredictImage(null, simulatedNodeId = nodeId)
                        showMockSelectorDialog = false
                    },
                    onDismiss = { showMockSelectorDialog = false }
                )
            }

            if (state.showImageSourceDialog) {
                Dialog(onDismissRequest = { viewModel.setImageSourceDialog(false) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.5.dp, cyanColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "LỰA CHỌN THU THẬP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = cyanColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Chụp ảnh thực địa bằng camera thiết bị hoặc tải tệp tin hình ảnh có sẵn từ thư viện:",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                            
                            Button(
                                onClick = {
                                    viewModel.setImageSourceDialog(false)
                                    if (state.sourceDialogTarget == "start") {
                                        routeStartCameraLauncher.launch(null)
                                    } else {
                                        chatCameraLauncher.launch(null)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chụp ảnh", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    viewModel.setImageSourceDialog(false)
                                    if (state.sourceDialogTarget == "start") {
                                        routeStartPickerLauncher.launch("image/*")
                                    } else {
                                        chatImagePickerLauncher.launch("image/*")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = emeraldColor),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tải ảnh", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { viewModel.setImageSourceDialog(false) },
                                border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Hủy bỏ")
                            }
                        }
                    }
                }
            }

            if (state.showMultiObjectSelectionDialog) {
                MultiObjectSelectionDialog(
                    state = state,
                    onSelect = { item -> viewModel.selectDetectedObject(item) },
                    onRetry = { viewModel.retryMultiObjectCapture() },
                    onDismiss = { viewModel.dismissMultiObjectDialog() }
                )
            }

            if (state.showGpsBlockWarning) {
                Dialog(onDismissRequest = { viewModel.setGpsBlockWarning(false) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F0808)),
                        border = BorderStroke(2.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsOff,
                                contentDescription = "GPS Lock Out",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "🚨 NGOÀI PHẠM VI (GIỚI HẠN GPS)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Hệ thống GIS ghi nhận tọa độ thực của bạn ngoài phạm vi hoạt động của trạm di sản:\n\n" +
                                       "📍 Trạm: ${state.blockedArtifactName}\n" +
                                       "📏 Khoảng cách: ${state.blockedArtifactDistance.toInt()} mét (Giới hạn: 50m)\n\n" +
                                       "Để tuân thủ yêu cầu nghiên cứu thực địa của Luận văn, bạn cần đứng gần hiện vật để mở khóa. Hoặc sử dụng nút Học Thử để nghiên cứu từ xa.",
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.setGpsBlockWarning(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Xác Nhận", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        val activeNodeId = state.lockedHamiltonPath.getOrNull(state.currentPathIndex) ?: 0
                                        val artId = viewModel.nodeIdToArtifactId(activeNodeId)
                                        val artObj = state.artifacts.find { it.id == artId }
                                        if (artObj != null) {
                                            viewModel.teleportTo(artObj)
                                        }
                                        viewModel.setGpsBlockWarning(false)
                                        Toast.makeText(context, "Đã dịch chuyển GPS thành công!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                                    modifier = Modifier.weight(1.2f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Dịch Chuyển Học Thử", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalProgressBlock(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    cyanColor: Color,
    emeraldColor: Color,
    goldColor: Color
) {
    val textDarkColor = MaterialTheme.colorScheme.onBackground

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFDEE2E6))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Lightning",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tiến trình khám phá",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "${state.completedArtifactIds.size} / 15 Trạm",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDarkColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Glowing beautiful progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE9ECEF))
                    .padding(2.dp)
            ) {
                val progressFraction = state.completedArtifactIds.size / 15f
                if (progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                )
                            )
                    )
                }
            }

            // Progress details footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "Rank Badge",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cấp bậc: ",
                        fontSize = 12.sp,
                        color = textDarkColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = viewModel.getHeroTitle(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { viewModel.showRulesModal(true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quy định Cấp bậc", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { viewModel.showHistoryLog(true) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nhật ký (${state.completedArtifactIds.size}/15)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CyberGridBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar Sweep")
    val sweepOffsetY by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sweep offset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSpacing = 40.dp.toPx()
        val gridColor = Color(0xFF3B82F6).copy(alpha = 0.05f)

        // Horizontal lines
        var y = 0f
        while (y < size.height) {
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
            y += gridSpacing
        }

        // Vertical lines
        var x = 0f
        while (x < size.width) {
            drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
            x += gridSpacing
        }

        // Radar laser beam sweep effect
        val sweepPos = sweepOffsetY * size.height
        val sweepBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFF3B82F6).copy(alpha = 0.08f),
                Color.Transparent
            )
        )
        drawRect(
            brush = sweepBrush,
            topLeft = Offset(0f, sweepPos - 100f),
            size = androidx.compose.ui.geometry.Size(size.width, 200f)
        )
    }
}

@Composable
fun ChatPanel(
    viewModel: MuseumViewModel,
    state: com.example.viewmodel.MuseumUiState,
    tts: TextToSpeech?,
    routeStartPickerLauncher: () -> Unit,
    chatImagePickerLauncher: () -> Unit,
    onShowMockSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goldColor = Color(0xFFFFAA00)
    val cyanColor = Color(0xFF00F0FF)
    val borderCyan = Color(0x6600F0FF)
    val emeraldColor = Color(0xFF10B981)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Keep chat scrolled to bottom
    LaunchedEffect(state.chatMessages.size) {
        if (state.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.chatMessages.size - 1)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xEB0B1222)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Giao diện AI Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Hành Trình Di Sản QK9",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (state.gameLocked) Color(0xFFEF4444) else emeraldColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.gameLocked) "LỘ TRÌNH ĐÃ KHÓA" else "CHỜ ĐỊNH VỊ ẢNH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.gameLocked) Color(0xFFEF4444) else emeraldColor,
                            maxLines = 1
                        )
                    }
                }

                // Countdown Timer when game is locked
                if (state.gameLocked && !state.currentStageCleared) {
                    val lowTime = state.timeLeft < 60
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (lowTime) Color(0x33EF4444) else Color(0x3300F0FF))
                            .border(1.dp, if (lowTime) Color(0xFFEF4444) else cyanColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Timer",
                            tint = if (lowTime) Color(0xFFEF4444) else cyanColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewModel.formatTime(state.timeLeft),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (lowTime) Color(0xFFEF4444) else cyanColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Giao diện upload khi chưa khóa map
            if (!state.gameLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0xFF0F172A).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tải lên hình ảnh hiện vật bảo tàng để xác định vị trí xuất phát của bạn:",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = routeStartPickerLauncher,
                        colors = ButtonDefaults.buttonColors(containerColor = cyanColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                            .testTag("upload_camera_button")
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp / Tải ảnh thật", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (state.uploading) {
                        Spacer(modifier = Modifier.height(6.dp))
                        CircularProgressIndicator(color = cyanColor, modifier = Modifier.size(24.dp))
                        Text("Đang phân tích nhận diện hiện vật...", fontSize = 11.sp, color = cyanColor)
                    }
                }
            }

            // Chat Messages Content Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.chatMessages) { message ->
                    ComposeChatBubble(message = message, tts = tts, viewModel = viewModel, state = state)
                }

                if (state.isChatLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x3300F0FF)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = "Đang truy xuất trung tâm cơ sở dữ liệu...",
                                    fontSize = 12.sp,
                                    color = cyanColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Giao diện hộp nhập liệu khi game đang khóa
            if (state.gameLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090E1A))
                        .border(1.dp, Color(0xFF1E293B))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Selected Image Preview bar
                    if (state.chatSelectedImageSrc != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x3300F0FF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = state.chatSelectedImageSrc,
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ảnh hiện vật đã sẵn sàng được phân tích...",
                                    fontSize = 11.sp,
                                    color = cyanColor
                                )
                            }
                            IconButton(onClick = { viewModel.setChatSelectedImage(null) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Cancel, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }



                    // Quick Action Prompt Chips Bar
                    if (!state.currentStageCleared) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val quickPrompts = listOf(
                                "🏛️ Giới thiệu Bảo tàng QK9" to "Giới thiệu tổng quan ngắn gọn về Bảo tàng Quân khu 9 tại Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ.",
                                "📍 Địa chỉ & Vị trí" to "Địa chỉ chính xác và thông tin tham quan Bảo tàng Quân khu 9 - Cần Thơ ở đâu?",
                                "📜 Lịch sử cổ vật" to "Thuyết minh súc tích về lịch sử cổ vật ${state.currentArtifactName} tại Bảo tàng QK9.",
                                "💡 Đặc điểm nổi bật" to "Đặc điểm nổi bật nhất của hiện vật ${state.currentArtifactName} là gì?",
                                "🔑 Gợi ý giải mã" to "Cho tôi gợi ý ngắn gọn về mật mã trạm ${state.currentArtifactName}."
                            )

                            quickPrompts.forEach { (label, promptText) ->
                                Surface(
                                    onClick = { viewModel.sendChatMessage(promptText) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF0F1E36),
                                    border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f)),
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Text & Action Inputs row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = chatImagePickerLauncher,
                            enabled = !state.currentStageCleared,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Choose image",
                                tint = if (state.currentStageCleared) Color.Gray else cyanColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            placeholder = {
                                Text(
                                    text = if (state.currentStageCleared) "Trạm đã mở, nhấn nút [Đi Tới Chặng Tiếp Theo]..." else "Hỏi về hiện vật: ${state.currentArtifactName}...",
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            singleLine = true,
                            enabled = !state.currentStageCleared,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF030712),
                                unfocusedContainerColor = Color(0xFF030712),
                                disabledContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = cyanColor,
                                unfocusedBorderColor = Color(0xFF334155),
                                disabledBorderColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = {
                                if (userInput.isNotBlank() || state.chatSelectedImageSrc != null) {
                                    viewModel.sendChatMessage(userInput, state.chatSelectedImageSrc)
                                    userInput = ""
                                    viewModel.setChatSelectedImage(null)
                                }
                            },
                            enabled = !state.currentStageCleared,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (state.currentStageCleared) {
                                        Brush.linearGradient(listOf(Color.Gray, Color.Gray))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
                                    }
                                )
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedChatMessageText(
    rawText: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val goldColor = Color(0xFFFFD700)
    val cyanColor = Color(0xFF00E5FF)
    val textColor = if (isUser) Color.White else Color(0xFFE2E8F0)

    val annotatedString = remember(rawText, isUser) {
        buildAnnotatedString {
            val processed = rawText.replace("<br>", "\n").replace("<br/>", "\n")
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
        fontSize = 13.sp,
        color = textColor,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}

@Composable
fun ComposeChatBubble(
    message: ChatMessage,
    tts: TextToSpeech?,
    viewModel: MuseumViewModel,
    state: com.example.viewmodel.MuseumUiState
) {
    val isUser = message.sender == MessageSender.USER
    val isSpeaking = state.isSpeaking && state.activeSpeechText == message.text

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF1E3A8A))))
                    .border(1.dp, Color(0xFF00F0FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Android, contentDescription = "AI QK9", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Color(0xFF1D4ED8) else Color(0xFF0F182A)
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = if (isUser) null else BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI HƯỚNG DẪN VIÊN BẢO TÀNG QK9",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    if (message.image != null) {
                        AsyncImage(
                            model = message.image,
                            contentDescription = "Image sent",
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .heightIn(max = 140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    FormattedChatMessageText(
                        rawText = message.text,
                        isUser = isUser
                    )
                }
            }

            // Audio synthesis voice narrator trigger
            if (!isUser) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = {
                            if (isSpeaking) {
                                tts?.stop()
                                viewModel.toggleSpeaking(false)
                            } else {
                                val cleanText = message.text.replace(Regex("<[^>]*>"), "")
                                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null)
                                viewModel.toggleSpeaking(true, message.text)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSpeaking) Color(0x33EF4444) else Color(0x2200F0FF),
                        border = BorderStroke(1.dp, if (isSpeaking) Color(0xFFEF4444) else Color(0xFF00F0FF).copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isSpeaking) Color(0xFFEF4444) else Color(0xFF00F0FF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSpeaking) "⏹ Dừng đọc" else "🔊 Nghe thuyết minh",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSpeaking) Color(0xFFEF4444) else Color(0xFF00F0FF)
                            )
                        }
                    }
                }

                // AI suggestions
                if (message.suggestions.isNotEmpty()) {
                    Text(
                        text = "💡 Gợi ý tìm hiểu thêm:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        message.suggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F1E36))
                                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.sendChatMessage(suggestion) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF991B1B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun NavigatorPanel(
    viewModel: MuseumViewModel,
    state: com.example.viewmodel.MuseumUiState,
    modifier: Modifier = Modifier
) {
    val goldColor = Color(0xFFFFAA00)
    val cyanColor = Color(0xFF00F0FF)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xEB0B1222)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = cyanColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HỆ THỐNG ĐỊNH VỊ DI SẢN (GIS)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = cyanColor
                )
            }

            Text(
                text = "Hệ thống định vị thông minh đang thiết lập một chuỗi liên kết logic đi qua toàn bộ 15 trạm di sản trong bảo tàng. Đường nét đứt màu xanh neon chính là Lộ trình Hamilton cố định mà bạn cần bám sát để giải mã nối tiếp nhau.",
                fontSize = 11.sp,
                color = Color.White,
                lineHeight = 15.sp,
                textAlign = TextAlign.Justify
            )

            // GIS map canvas visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF102542), Color(0xFF070F1E))
                        )
                    )
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                GISCanvasMap(viewModel = viewModel, state = state)
            }
        }
    }
}

@Composable
fun GISCanvasMap(
    viewModel: MuseumViewModel,
    state: com.example.viewmodel.MuseumUiState
) {
    val context = LocalContext.current
    val isEdgeInLockedPath = { u: Int, v: Int, path: List<Int> ->
        if (path.isEmpty()) false
        else {
            val idxU = path.indexOf(u)
            val idxV = path.indexOf(v)
            idxU != -1 && idxV != -1 && abs(idxU - idxV) == 1
        }
    }

    var radarSweepAngle by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(24)
            radarSweepAngle = (radarSweepAngle + 2.5f) % 360f
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse glow"
    )

    val nodeCoords = remember {
        listOf(
            Offset(319f, 39f), Offset(382f, 108f), Offset(382f, 221f), Offset(304f, 270f), Offset(221f, 304f),
            Offset(137f, 270f), Offset(59f, 221f), Offset(123f, 147f), Offset(59f, 108f), Offset(172f, 157f),
            Offset(221f, 29f), Offset(221f, 88f), Offset(270f, 157f), Offset(319f, 147f), Offset(123f, 39f)
        )
    }

    val staticEdges = remember {
        listOf(
            Pair(0, 11), Pair(0, 14), Pair(1, 8), Pair(1, 11), Pair(1, 12), Pair(2, 3), Pair(2, 12), Pair(3, 7), Pair(3, 13),
            Pair(4, 5), Pair(4, 8), Pair(4, 9), Pair(5, 6), Pair(5, 9), Pair(6, 13), Pair(6, 14), Pair(7, 10), Pair(7, 11),
            Pair(7, 14), Pair(8, 11), Pair(9, 12), Pair(9, 13), Pair(10, 12), Pair(10, 13), Pair(13, 14)
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight

        val scaleX = w / 460f
        val scaleY = h / 360f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleXPx = size.width / 460f
            val scaleYPx = size.height / 360f

            // 0. Draw sweeping military radar overlay (Feature 9)
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = maxOf(size.width, size.height)
            val angleRad = Math.toRadians(radarSweepAngle.toDouble())
            val lineEndX = (center.x + maxRadius * Math.cos(angleRad)).toFloat()
            val lineEndY = (center.y + maxRadius * Math.sin(angleRad)).toFloat()

            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                start = center,
                end = Offset(lineEndX, lineEndY),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.03f),
                center = center,
                radius = size.width / 4,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.02f),
                center = center,
                radius = size.width / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )

            // 1. Draw static background connections
            staticEdges.forEach { edge ->
                val fromNode = nodeCoords[edge.first]
                val toNode = nodeCoords[edge.second]
                val isLocked = isEdgeInLockedPath(edge.first, edge.second, state.lockedHamiltonPath)

                if (isLocked) {
                    // Glowing cyan dash line representing active Hamilton track
                    drawLine(
                        color = Color(0xFF00F0FF),
                        start = Offset(fromNode.x * scaleXPx, fromNode.y * scaleYPx),
                        end = Offset(toNode.x * scaleXPx, toNode.y * scaleYPx),
                        strokeWidth = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f),
                        cap = StrokeCap.Round
                    )
                } else {
                    // Dark background lines
                    drawLine(
                        color = Color(0xFF1E293B).copy(alpha = 0.6f),
                        start = Offset(fromNode.x * scaleXPx, fromNode.y * scaleYPx),
                        end = Offset(toNode.x * scaleXPx, toNode.y * scaleYPx),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }



        // 2. Draw nodes overlay buttons
        nodeCoords.forEachIndexed { id, coord ->
            val posX = scaleX * coord.x
            val posY = scaleY * coord.y

            val artId = viewModel.nodeIdToArtifactId(id)
            val artifact = state.artifacts.find { it.id == artId }
            val distance = if (artifact != null) viewModel.getDistanceToArtifact(artifact) else 999.0

            val isCompleted = state.completedArtifactIds.contains(artId)
            val isDeferred = state.deferredArtifactIds.contains(artId)
            val isNear = distance <= 15.0 && !isCompleted && !isDeferred

            val activeNodeId = state.lockedHamiltonPath.getOrNull(state.currentPathIndex) ?: -1
            val isCurrent = state.gameLocked && activeNodeId == id

            val nodeBgColor = when {
                isCompleted -> Color(0xFF064E3B) // dark green
                isDeferred -> Color(0xFF78350F) // dark yellow / amber
                isNear -> Color(0xFF1E3A8A) // dark blue
                isCurrent -> Color(0xFF991B1B) // dark red
                else -> Color(0xFF0F172A) // dark slate
            }

            val nodeBorderColor = when {
                isCompleted -> Color(0xFF10B981) // emerald 🟢
                isDeferred -> Color(0xFFFBBF24) // amber 🟡
                isNear -> Color(0xFF3B82F6) // blue 🔵
                isCurrent -> Color(0xFFEF4444) // bright red
                else -> Color(0xFF94A3B8) // slate white ⚪
            }

            val opacity = if (!isCompleted && !isDeferred && !isNear && !isCurrent) 0.55f else 1.0f
            val nodeSize = 36.dp

            Box(
                modifier = Modifier
                    .offset(x = posX - (nodeSize / 2), y = posY - (nodeSize / 2))
                    .size(nodeSize)
                    .alpha(opacity)
                    .clip(CircleShape)
                    .background(nodeBgColor)
                    .border(
                        width = if (isCurrent) (3.dp * pulseGlow) else 1.5.dp,
                        color = nodeBorderColor,
                        shape = CircleShape
                    )
                    .clickable {
                        val art = viewModel.getArtifactByNodeId(id)
                        if (art != null) {
                            viewModel.unlockAndPlayStageWithPoints(art.id)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$id",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun HUDControlOverlay(
    viewModel: MuseumViewModel,
    state: com.example.viewmodel.MuseumUiState,
    goldColor: Color,
    cyanColor: Color,
    emeraldColor: Color
) {
    if (!state.gameLocked) return

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()
    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1D4ED8), // Cobalt Blue
            Color(0xFFD97706), // Yellow/Gold
            Color(0xFFDC2626)  // Crimson Red
        )
    )
    val redColor = Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .offset { androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Box {
                if (state.isMinimized) {
                    // Minimized view: floating button pill
                    Button(
                        onClick = { viewModel.setIsMinimized(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xEE0D1E36)),
                        border = BorderStroke(1.dp, cyanColor),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("max_hud_button")
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = cyanColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HỆ THỐNG MINI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cyanColor)
                    }
                } else {
                    // Maximized dashboard action buttons HUD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFA061021)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, cyanColor),
                        elevation = CardDefaults.cardElevation(12.dp),
                        modifier = Modifier
                            .width(320.dp)
                            .wrapContentHeight()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = cyanColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "HỆ THỐNG ĐIỀU KHIỂN TIẾN TRÌNH",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = cyanColor
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.setIsMinimized(true) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("min_hud_button")
                                ) {
                                    Icon(Icons.Default.CloseFullscreen, contentDescription = "Minimize", tint = cyanColor, modifier = Modifier.size(14.dp))
                                }
                            }

                            // Main controller buttons inside HUD
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")
                            
                            val isGameEnabled = !state.currentStageCleared
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isGameEnabled) buttonGradient else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B))))
                                    .border(1.dp, if (isGameEnabled) Color(0x80FFFFFF) else Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .clickable(enabled = isGameEnabled) {
                                        val activeNodeId = state.lockedHamiltonPath.getOrNull(state.currentPathIndex) ?: 0
                                        val artId = viewModel.nodeIdToArtifactId(activeNodeId)
                                        val artObj = state.artifacts.find { it.id == artId }
                                        if (artObj != null) {
                                            val dist = viewModel.getDistanceToArtifact(artObj)
                                            val isWithin = viewModel.isWithinRange(artObj)
                                            if (isWithin) {
                                                viewModel.navigateTo(AppScreen.Game(artId))
                                            } else {
                                                viewModel.setGpsBlockWarning(true, artObj.name, dist)
                                            }
                                        } else {
                                            viewModel.navigateTo(AppScreen.Game(artId))
                                        }
                                    }
                                    .testTag("puzz_game_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (state.currentStageCleared) "Đã Giải Mã Trạm" else "Tiến Vào Trận Khóa Puzzle",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (isGameEnabled) Color.White else Color.Gray
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                var isPressed1 by remember { mutableStateOf(false) }
                                val scale1 by animateFloatAsState(if (isPressed1) 0.95f else 1f, label = "scale1")
                                
                                // Finish early (Red Border secondary action)
                                Button(
                                    onClick = { viewModel.triggerEarlyFinish() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, redColor.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .graphicsLayer(scaleX = scale1, scaleY = scale1)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    isPressed1 = true
                                                    tryAwaitRelease()
                                                    isPressed1 = false
                                                }
                                            )
                                        }
                                        .testTag("early_finish_button")
                                ) {
                                    Text("Kết Thúc", color = redColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                var isPressed2 by remember { mutableStateOf(false) }
                                val scale2 by animateFloatAsState(if (isPressed2) 0.95f else 1f, label = "scale2")
                                val isNextEnabled = state.currentStageCleared
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(44.dp)
                                        .graphicsLayer(scaleX = scale2, scaleY = scale2)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isNextEnabled) buttonGradient else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B))))
                                        .border(1.dp, if (isNextEnabled) Color(0x80FFFFFF) else Color(0xFF334155), RoundedCornerShape(12.dp))
                                        .clickable(enabled = isNextEnabled) {
                                            viewModel.advanceNextStage()
                                        }
                                        .testTag("next_stage_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            "Chặng Kế", 
                                            color = if (isNextEnabled) Color.White else Color.Gray, 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.ArrowForward, 
                                            contentDescription = null, 
                                            tint = if (isNextEnabled) Color.White else Color.Gray, 
                                            modifier = Modifier.size(12.dp)
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

@Composable
fun RulesModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFFCC00))
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUY CHẾ PHONG TẶNG CẤP BẬC", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Hệ thống tự động quét và đánh giá cấp bậc dựa trên 3 chỉ số: Số trạm giải mã, Tổng điểm tích lũy và Thời gian thực thi.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(6.dp))

                RuleItem(rankName = "Tân Binh Nhập Ngũ", cond = "Trạng thái khởi tạo khi chưa giải mã thành công trạm nào", color = Color(0xFF64748B))
                RuleItem(rankName = "Chiến Sĩ Trinh Sát", cond = "Đạt từ 1 - 4 Trạm bất kỳ trên Sơ đồ Hamilton", color = Color(0xFF00F0FF))
                RuleItem(rankName = "Sĩ Quan Tham Mưu", cond = "Đạt từ 5 - 9 Trạm hoặc Tổng điểm ≥ 400 điểm", color = Color(0xFF9400D3))
                RuleItem(rankName = "Huyền Thoại Tốc Biến Di Sản", cond = "Đạt ≥ 10 Trạm và tổng thời gian < 10 phút", color = Color(0xFFFFCC00))
                RuleItem(rankName = "Anh Hùng Di Sản Toàn Lộ Trình", cond = "Đạt tối đa 15/15 Trạm + Tổng điểm ≥ 1200 điểm", color = Color(0xFFFF0055))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00))
            ) {
                Text("ĐÃ HIỂU ĐIỀU KIỆN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        containerColor = Color(0xFF0D1222),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun RuleItem(rankName: String, cond: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(rankName, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(cond, color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
fun RouteModal(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2071329)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(
                listOf(Color(0xFF00F0FF), Color(0xFF1D4ED8), Color(0xFFFFD700))
            )),
            elevation = CardDefaults.cardElevation(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with title and clear exit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0F2B48),
                            border = BorderStroke(1.dp, Color(0xFF00F0FF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "LỘ TRÌNH THÁM HIỂM HAMILTON",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "15 Trạm Di Sản Cổ Vật Quân Khu 9",
                                fontSize = 10.5.sp,
                                color = Color(0xFF00F0FF)
                            )
                        }
                    }

                    // Exit button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Thoát",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Route status summary bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ĐÃ HOÀN THÀNH", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("${state.completedArtifactIds.size} / 15", fontSize = 14.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Black)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF334155)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CHƯA HOÀN THÀNH", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("${state.deferredArtifactIds.size}", fontSize = 14.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Black)
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF334155)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TỔNG ĐIỂM", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("${state.totalScore} đ", fontSize = 14.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable List of 15 Stations
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.lockedHamiltonPath) { idx, nodeId ->
                        val artId = viewModel.nodeIdToArtifactId(nodeId)
                        val artObj = viewModel.getArtifactByNodeId(nodeId)
                        val artName = artObj?.name ?: "Trạm di sản #$artId"
                        val isCompleted = state.completedArtifactIds.contains(artId)
                        val isDeferred = state.deferredArtifactIds.contains(artId)

                        val cardBg = when {
                            isCompleted -> Color(0x2010B981)
                            isDeferred -> Color(0x20FBBF24)
                            else -> Color(0x10FFFFFF)
                        }

                        val borderColor = when {
                            isCompleted -> Color(0xFF10B981)
                            isDeferred -> Color(0xFFFBBF24)
                            else -> Color(0xFF334155)
                        }

                        val statusBadgeText = when {
                            isCompleted -> "🟢 Đã điều tra"
                            isDeferred -> "🟡 Chưa hoàn thành"
                            else -> "⚪ Chưa thám hiểm"
                        }

                        val statusColor = when {
                            isCompleted -> Color(0xFF10B981)
                            isDeferred -> Color(0xFFFBBF24)
                            else -> Color(0xFF94A3B8)
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = statusColor.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, statusColor)
                                    ) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = statusColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = artName,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = statusBadgeText,
                                            fontSize = 10.sp,
                                            color = statusColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        viewModel.unlockAndPlayStageWithPoints(artId)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCompleted) Color(0xFF059669) else Color(0xFF2563EB)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isCompleted) "Chơi lại" else "Đổi 100đ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom exit button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "THOÁT LỘ TRÌNH",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

data class LeaderItem(val rank: Int, val name: String, val title: String, val score: Int, val cleared: Int, val isUser: Boolean)

@Composable
fun LeaderboardModal(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val heroTitle = viewModel.getHeroTitle()
    val realScoresState by com.example.data.LeaderboardRepository.realScores.collectAsState()

    var showIpDialog by remember { mutableStateOf(false) }
    var tempIpAddress by remember { mutableStateOf(com.example.network.LocalClient.getBaseIp()) }
    var tempPortChat by remember { mutableStateOf(com.example.network.LocalClient.getChatPort()) }
    var tempPortPredict by remember { mutableStateOf(com.example.network.LocalClient.getPredictPort()) }
    var tempPortDecrypt by remember { mutableStateOf(com.example.network.LocalClient.getDecryptPort()) }
    var tempPortQuestion by remember { mutableStateOf(com.example.network.LocalClient.getQuestionPort()) }
    var tempPortImage by remember { mutableStateOf(com.example.network.LocalClient.getImagePort()) }
    var tempPortLeaderboard by remember { mutableStateOf(com.example.network.LocalClient.getLeaderboardPort()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.example.data.LeaderboardRepository.fetchLeaderboard(state.loggedInUser)
        if (state.isLoggedIn && state.totalScore > 0) {
            com.example.data.LeaderboardRepository.submitUserScore(
                userName = state.loggedInUser,
                heroTitle = viewModel.getHeroTitle(),
                score = state.totalScore,
                clearedStages = state.completedArtifactIds.size,
                userEmail = state.userEmail,
                ticketCode = state.ticketCode
            )
        }
    }

    if (showIpDialog) {
        val cyanColor = Color(0xFF00E5FF)
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = {
                Text(
                    text = "CẤU HÌNH API MÁY CHỦ",
                    color = cyanColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Thay đổi IP và các Cổng dịch vụ (Mặc định: IP 10.158.209.106)",
                        fontSize = 11.5.sp,
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
                            label = { Text("Chat (8000)") },
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
                            label = { Text("AI (8001)") },
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
                            label = { Text("Giải Mã (8003)") },
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
                            label = { Text("Câu Hỏi (8004)") },
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
                            label = { Text("Ảnh (8005)") },
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
                            label = { Text("Xếp Hạng (8006)") },
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
            containerColor = Color(0xFF0F172A),
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
                        coroutineScope.launch {
                            com.example.data.LeaderboardRepository.fetchLeaderboard(state.loggedInUser)
                        }
                    }
                ) {
                    Text("LƯU & KẾT NỐI", color = cyanColor, fontWeight = FontWeight.Bold)
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
                            coroutineScope.launch {
                                com.example.data.LeaderboardRepository.fetchLeaderboard(state.loggedInUser)
                            }
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

    val leaderboard = realScoresState.map { entry ->
        val isCurrent = entry.name.equals(state.loggedInUser, ignoreCase = true) || (state.userEmail.isNotEmpty() && entry.email.equals(state.userEmail, ignoreCase = true))
        LeaderItem(
            rank = entry.rank,
            name = if (isCurrent) "BẠN (${entry.name})" else entry.name,
            title = if (isCurrent) viewModel.getHeroTitle() else entry.title,
            score = if (isCurrent) maxOf(entry.score, state.totalScore) else entry.score,
            cleared = if (isCurrent) maxOf(entry.cleared, state.completedArtifactIds.size) else entry.cleared,
            isUser = isCurrent
        )
    }.sortedByDescending { it.score }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2120822)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(
                listOf(Color(0xFFFFD700), Color(0xFFDC2626), Color(0xFF00F0FF))
            )),
            elevation = CardDefaults.cardElevation(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with title and buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3B122C),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "BẢNG XẾP HẠNG KHẢO CỔ QUÂN KHU 9",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Bảng Vàng Danh Dự Thám Hiểm Di Sản",
                                fontSize = 10.5.sp,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Settings button
                        IconButton(
                            onClick = {
                                tempIpAddress = com.example.network.LocalClient.getBaseIp()
                                tempPortChat = com.example.network.LocalClient.getChatPort()
                                tempPortPredict = com.example.network.LocalClient.getPredictPort()
                                tempPortDecrypt = com.example.network.LocalClient.getDecryptPort()
                                tempPortQuestion = com.example.network.LocalClient.getQuestionPort()
                                tempPortImage = com.example.network.LocalClient.getImagePort()
                                tempPortLeaderboard = com.example.network.LocalClient.getLeaderboardPort()
                                showIpDialog = true
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt Server API",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Exit button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Thoát",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User current card profile banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1035),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFEF4444))))
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.loggedInUser.take(1).uppercase().ifBlank { "U" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            Column {
                                Text(
                                    text = state.loggedInUser.ifBlank { "Người Chơi Quả Cảm" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "🎖️ $heroTitle",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${state.totalScore} ĐIỂM",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00F0FF)
                            )
                            Text(
                                text = "${state.completedArtifactIds.size}/15 Trạm",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Leaderboard list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(leaderboard) { index, item ->
                        val rankIcon = when (index) {
                            0 -> "🥇"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> "#${index + 1}"
                        }

                        val rankColor = when (index) {
                            0 -> Color(0xFFFFD700)
                            1 -> Color(0xFFC0C0C0)
                            2 -> Color(0xFFCD7F32)
                            else -> Color(0xFF94A3B8)
                        }

                        val cardBg = if (item.isUser) Color(0x3500F0FF) else Color(0x10FFFFFF)
                        val borderColor = if (item.isUser) Color(0xFF00F0FF) else Color(0xFF334155)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = cardBg,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = rankIcon,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = rankColor
                                    )

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.name,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isUser) Color(0xFF00F0FF) else Color.White
                                            )
                                            if (item.isUser) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFF00F0FF),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "BẠN",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = item.title,
                                            fontSize = 9.5.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.score} đ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                    Text(
                                        text = "${item.cleared}/15 ải",
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom exit button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "THOÁT BẢNG XẾP HẠNG",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryLogModal(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    onDismiss: () -> Unit
) {
    val goldColor = Color(0xFFFFAA00)
    val cyanColor = Color(0xFF00F0FF)
    val emeraldColor = Color(0xFF10B981)
    
    val investigatedCount = state.completedArtifactIds.size
    val deferredCount = state.deferredArtifactIds.size
    val undiscoveredCount = maxOf(0, 15 - investigatedCount - deferredCount)
    val progressPercent = (investigatedCount / 15f) * 100f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = cyanColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "NHẬT KÝ ĐIỀU TRA QUÂN KHU 9",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Statistical Summary Grid
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070F1E)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ĐÃ ĐIỀU TRA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("🟢 $investigatedCount", fontSize = 16.sp, fontWeight = FontWeight.Black, color = emeraldColor)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ĐỂ SAU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("🟡 $deferredCount", fontSize = 16.sp, fontWeight = FontWeight.Black, color = goldColor)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CHƯA PHÁT HIỆN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("⚪ $undiscoveredCount", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        
                        HorizontalDivider(color = Color(0xFF1E293B))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tiến độ giải mật chiến dịch:", fontSize = 11.sp, color = Color.LightGray)
                            Text("${String.format(Locale.US, "%.0f", progressPercent)}%", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = cyanColor)
                        }
                    }
                }

                // 📌 SECTION 1: DEFERRED / "ĐỂ SAU" list
                Text(
                    text = "📌 TRẠM CHỜ ĐIỀU TRA (ĐỂ SAU)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldColor,
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .background(Color(0xFF040815), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    val deferredArtifacts = state.artifacts.filter { state.deferredArtifactIds.contains(it.id) }
                    if (deferredArtifacts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Chưa có trạm nào bị hoãn. Hãy bám sát lộ trình!",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(deferredArtifacts) { artifact ->
                                val dist = viewModel.getDistanceToArtifact(artifact)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x33FFAA00), RoundedCornerShape(8.dp))
                                        .border(1.dp, goldColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🟡 ${artifact.name}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Cách bạn: ${String.format(Locale.US, "%.1f", dist)}m | Tọa độ: (${artifact.latitude}, ${artifact.longitude})",
                                            fontSize = 9.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.teleportAndNavigateTo(artifact.id)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("QUAY LẠI", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 📜 SECTION 2: FULL JOURNEY HISTORY
                Text(
                    text = "📜 LỘ TRÌNH CHI TIẾT (15 TRẠM)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = cyanColor,
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.8f)
                        .background(Color(0xFF040815), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(state.lockedHamiltonPath) { idx, nodeId ->
                            val artId = viewModel.nodeIdToArtifactId(nodeId)
                            val completed = state.completedArtifactIds.contains(artId)
                            val deferred = state.deferredArtifactIds.contains(artId)
                            val name = viewModel.getArtifactByNodeId(nodeId)?.name ?: "Trạm di sản"

                            val itemBgColor = when {
                                completed -> Color(0x1A10B981)
                                deferred -> Color(0x1AFBBF24)
                                else -> Color(0x0AFFFFFF)
                            }

                            val itemBorderColor = when {
                                completed -> emeraldColor.copy(alpha = 0.2f)
                                deferred -> goldColor.copy(alpha = 0.2f)
                                else -> Color(0xFF1E293B)
                            }

                            val statusText = when {
                                completed -> "🟢 Đã điều tra"
                                deferred -> "🟡 Chưa hoàn thành"
                                else -> "⚪ Chưa thám hiểm"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(itemBgColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, itemBorderColor, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${idx + 1}",
                                        color = if (completed) emeraldColor else if (deferred) goldColor else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        name,
                                        color = if (completed || deferred) Color.White else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = statusText,
                                        color = if (completed) emeraldColor else if (deferred) goldColor else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = {
                                            viewModel.unlockAndPlayStageWithPoints(artId)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (completed) Color(0xFF10B981) else Color(0xFFFFD700),
                                            contentColor = Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (completed) "Chơi lại" else "100đ Chơi",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = cyanColor, contentColor = Color.Black)
            ) {
                Text("ĐÓNG NHẬT KÝ", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF090F1E),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SummaryModal(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    goldColor: Color,
    cyanColor: Color,
    pinkColor: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFAA00))
                        .border(1.dp, goldColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = goldColor)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "BẢNG VÀNG TỔNG KẾT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = cyanColor
                )
                Text(
                    "HÀNH TRÌNH DI SẢN QUÂN KHU 9",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FF007F)),
                    border = BorderStroke(1.dp, pinkColor)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("DANH HIỆU ĐẠT ĐƯỢC", fontSize = 10.sp, color = pinkColor, fontWeight = FontWeight.Bold)
                        Text(
                            viewModel.getHeroTitle(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                if (state.secretGuessCorrect) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x3300F0FF)),
                        border = BorderStroke(1.dp, cyanColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CHIẾN THẮNG BẺ KHÓA THẦN TỐC!", fontSize = 10.sp, color = cyanColor, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val secretName = secretCandidatesList.getOrNull(state.secretArtifactId)?.name ?: ""
                            Text(
                                "Mật danh đúng: $secretName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Đã nhận thưởng x${state.secretGuessMultiplier} tổng điểm!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = goldColor
                            )
                        }
                    }
                }

                // Stats grid inside gold modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryStatCard(
                        icon = Icons.Default.LocationOn,
                        label = "Số trạm đã qua",
                        value = "${state.stationHistoryLog.size} / 15",
                        color = cyanColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        icon = Icons.Default.Stars,
                        label = "Tổng điểm",
                        value = "${state.totalScore}",
                        color = goldColor,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        icon = Icons.Default.AccessTime,
                        label = "Tổng thời gian",
                        value = viewModel.formatTotalTime(state.totalElapsedTime),
                        color = pinkColor,
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = cyanColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Hệ thống ghi nhận: Bạn đã hoàn thành xuất sắc công tác số hóa và bẻ khóa cơ sở dữ liệu lịch sử bảo tàng!",
                        fontSize = 11.sp,
                        color = Color(0xFFBACDDF),
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.restartNewAdventure() },
                    colors = ButtonDefaults.buttonColors(containerColor = pinkColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Chơi Lại", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Xem Bản Đồ", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        containerColor = Color(0xFF0D1222),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SummaryStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center, maxLines = 1)
            Text(value, fontSize = 12.sp, color = color, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MultiObjectSelectionDialog(
    state: com.example.viewmodel.MuseumUiState,
    onSelect: (com.example.viewmodel.DetectedObjectItem) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val cyanColor = Color(0xFF00F0FF)
    val goldColor = Color(0xFFFFAA00)
    val emeraldColor = Color(0xFF10B981)

    val bitmap = remember(state.multiObjectDetectionImageBase64) {
        state.multiObjectDetectionImageBase64?.let { base64Str ->
            try {
                val decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.5.dp, cyanColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF2563EB), cyanColor))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PHÁT HIỆN NHIỀU ĐỐI TƯỢNG",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = cyanColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Hệ thống AI nhận diện được ${state.detectedObjectsList.size} hiện vật",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Show bounding box image preview if available
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Ảnh nhận diện AI",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .background(Color(0xCC0F172A), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "AI Detection Bounding Boxes",
                                fontSize = 9.sp,
                                color = cyanColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = if (state.multiObjectSourceTarget == "start")
                        "Vui lòng chọn hiện vật bạn muốn bắt đầu lộ trình tham quan hoặc chụp lại:"
                    else
                        "Vui lòng chọn hiện vật bạn muốn trợ lý AI thuyết minh chi tiết hoặc tải lại:",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                // List of detected objects
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.detectedObjectsList) { index, item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Number Badge
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(goldColor, Color(0xFFFF6600)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.artifactName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    if (item.artifactDescription.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = item.artifactDescription,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { onSelect(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = cyanColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Chọn", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }

                // Action buttons: Retake / Upload & Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Chụp / Tải lại",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(0.7f)
                            .height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                    ) {
                        Text("Đóng", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MockArtifactSelector(
    state: com.example.viewmodel.MuseumUiState,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationSearching, contentDescription = null, tint = Color(0xFFFFCC00))
                Spacer(modifier = Modifier.width(8.dp))
                Text("QUÉT DI SẢN MÔ PHỎNG", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Chọn cổ vật bạn muốn mô phỏng chụp để hệ thống định vị điểm khởi hành và lập lộ trình Hamilton:",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
                Box(modifier = Modifier.height(300.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.artifacts) { artifact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .clickable { onSelect(artifact.id) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFCC00)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${artifact.id}", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(artifact.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("HỦY BỎ", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF0D1222),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ArtifactRewards3DShowcase(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel
) {
    val cyanColor = Color(0xFF00F0FF)
    val goldColor = Color(0xFFFFAA00)
    val redColor = Color(0xFFEF4444)
    val darkCard = Color(0xFF0F172A)
    val borderCyan = Color(0x3300F0FF)

    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF1D4ED8), // Cobalt Blue
            Color(0xFFD97706), // Yellow/Gold
            Color(0xFFDC2626)  // Crimson Red
        )
    )

    // 3D showcase states
    var selectedArtifactIndex by remember { mutableStateOf(0) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var scaleFactor by remember { mutableStateOf(1f) }

    val artifactList = listOf(
        Triple("Trống Đồng Đông Sơn", "Cổ vật quốc gia linh thiêng với các hoa văn mặt trời mặt trống và chim lạc bay lượn.", com.example.R.drawable.img_artifact_bronze_3d),
        Triple("Cúp Vàng Di Sản", "Phần thưởng danh giá dành cho những nhà nghiên cứu lịch sử xuất sắc đạt thành tích cao.", com.example.R.drawable.img_trophy_3d),
        Triple("Vương Miện Lạc Việt", "Relic hoàng gia cổ xưa phát quang ánh ngọc lục bảo huyền ảo quý hiếm.", com.example.R.drawable.img_artifact_gem_3d)
    )

    val currentArtifact = artifactList[selectedArtifactIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCyan, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = darkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PHÒNG TRƯNG BÀY 3D (360°)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cyanColor
                    )
                    Text(
                        text = "Vuốt trực tiếp hoặc kéo thanh trượt để xoay 360 độ",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "360 rotation",
                    tint = cyanColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Interactive 3D image block with swipe gesture detection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    )
                    .border(1.dp, borderCyan, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotationAngle += dragAmount.x * 0.5f
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Interactive 3D graphics layer transformation
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = currentArtifact.third),
                    contentDescription = currentArtifact.first,
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer {
                            rotationY = rotationAngle
                            rotationX = rotationAngle * 0.12f
                            scaleX = scaleFactor
                            scaleY = scaleFactor
                            cameraDistance = 12f * density
                        },
                    contentScale = ContentScale.Fit
                )

                // 3D angle HUD indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Góc xoay: ${rotationAngle.toInt() % 360}°",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 360-degree slider control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotate Left",
                    tint = cyanColor,
                    modifier = Modifier.size(16.dp)
                )
                
                Slider(
                    value = rotationAngle,
                    onValueChange = { rotationAngle = it },
                    valueRange = -180f..180f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = goldColor,
                        activeTrackColor = cyanColor,
                        inactiveTrackColor = Color(0xFF1E293B)
                    )
                )

                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = "Rotate Right",
                    tint = cyanColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Artifact info & descriptions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .border(0.5.dp, borderCyan, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currentArtifact.first,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldColor
                )
                Text(
                    text = currentArtifact.second,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    lineHeight = 18.sp
                )
            }

            // Selector tabs for artifacts (Avoid fixed height overflow on Redmi Note 11)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                artifactList.forEachIndexed { index, item ->
                    val isSelected = selectedArtifactIndex == index
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(buttonGradient)
                                .clickable {
                                    selectedArtifactIndex = index
                                    rotationAngle = 0f
                                }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.first.split(" ").last(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedArtifactIndex = index
                                    rotationAngle = 0f
                                }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.first.split(" ").last(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretDecoderPanel(
    viewModel: MuseumViewModel,
    state: com.example.viewmodel.MuseumUiState,
    modifier: Modifier = Modifier
) {
    var guessText by remember { mutableStateOf("") }
    var artifactSearchQuery by remember { mutableStateOf("") }
    var puzzleSlots by remember { mutableStateOf<List<String?>>(listOf(null, null, null, null)) }
    var selectedBagArtifactId by remember { mutableStateOf(14) }
    var animSlotIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Cyan/Gold cyber styling colors
    val darkCard = Color(0xFF0F172A)
    val cyanColor = Color(0xFF00F0FF)
    val goldColor = Color(0xFFFFAA00)
    val pinkColor = Color(0xFFFF007F)
    val borderCyan = Color(0x3300F0FF)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp), // Extra spacing to avoid overlap with bottom navigation or HUD
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(1.5.dp, cyanColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Decoder Key",
                        tint = cyanColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "GIẢI MÃ DI SẢN QUÂN KHU 9",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Text(
                    text = "Hãy thu thập đầy đủ các mảnh ghép chữ cái và xâu chuỗi manh mối lịch sử để giải mã danh tính hiện vật bí ẩn. Bẻ khóa đúng mục tiêu để dành chiến thắng thần tốc với hệ số nhân lên tới x5 điểm thưởng!",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    lineHeight = 16.sp
                )
                
                // Show current multiplier status
                val numCleared = state.completedArtifactIds.size
                val currentMultiplier = when {
                    numCleared < 3 -> 5
                    numCleared < 6 -> 4
                    numCleared < 9 -> 3
                    numCleared < 12 -> 2
                    else -> 1
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TIẾN TRÌNH",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$numCleared / 15 ải đã vượt",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (currentMultiplier > 1) Color(0xFF0284C7) else Color(0xFF334155))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Hệ số: x$currentMultiplier",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Timer display (60s countdown or game timer tracking)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(
                1.dp, 
                if (state.decoderThinkingSecondsLeft > 0 && !state.help1Used && !state.help2Used) cyanColor else Color(0xFF334155)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Timer",
                            tint = if (state.decoderThinkingSecondsLeft > 0 && !state.help1Used && !state.help2Used) cyanColor else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (state.decoderThinkingSecondsLeft > 0 && !state.help1Used && !state.help2Used) {
                                "ĐỒNG HỒ SUY NGHĨ"
                            } else {
                                "THỜI GIAN TRẬN ĐẤU"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    val timeText = if (state.decoderThinkingSecondsLeft > 0 && !state.help1Used && !state.help2Used) {
                        "${state.decoderThinkingSecondsLeft}s"
                    } else {
                        viewModel.formatTotalTime(state.totalElapsedTime)
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = timeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (state.decoderThinkingSecondsLeft > 0 && !state.help1Used && !state.help2Used) cyanColor else Color(0xFFF59E0B)
                        )
                    }
                }

                if (state.decoderThinkingSecondsLeft > 0 && !state.help1Used && !state.help2Used) {
                    LinearProgressIndicator(
                        progress = { state.decoderThinkingSecondsLeft / 60f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = cyanColor,
                        trackColor = Color(0xFF1E293B)
                    )
                } else {
                    Text(
                        text = "⚠️ Hết thời gian suy nghĩ miễn phí - Đang tính giờ thi đấu chính thức.",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Section: Decoding Assistance (Trợ giúp 1 và Trợ giúp 2)
        // Displays only after 13 stages have been cleared
        val hasCleared13Stages = state.completedArtifactIds.size >= 13
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(1.2.dp, if (hasCleared13Stages) goldColor else Color.Gray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpCenter,
                        contentDescription = "Help Panel",
                        tint = if (hasCleared13Stages) goldColor else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "HỘP TRỢ GIÚP ĐẶC BIỆT (CHỈ SAU 13 ẢI)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!hasCleared13Stages) {
                    // Locked warning helper message
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x22EF4444), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🔒 ĐÃ KHÓA TRỢ GIÚP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF87171)
                        )
                        Text(
                            text = "Bạn cần vượt qua ít nhất 13 ải trong hành trình di sản để mở khóa quyền trợ giúp bẻ khóa mật danh bí ẩn. Tiến độ hiện tại: ${state.completedArtifactIds.size}/13 chặng.",
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            lineHeight = 14.sp
                        )
                    }
                } else {
                    // Help features unlocked!
                    Text(
                        text = "Bạn đã kích hoạt chế độ trợ giúp khẩn cấp. Sử dụng trợ giúp sẽ dừng thời gian suy nghĩ miễn phí và trừ thẳng phần trăm số điểm hiện tại của bạn.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        lineHeight = 14.sp
                    )

                    HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                    // HELP 1: Caesar Cipher
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRỢ GIÚP 1: Mật Mã Caesar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = cyanColor
                            )
                            Text(
                                text = "Trừ 15% điểm",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171)
                            )
                        }

                        if (state.help1Used) {
                            // Revealed Caesar cipher name
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Mật danh đã mã hóa Caesar (Lùi ${state.help1Shift} chữ cái):",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = state.help1CaesarText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = cyanColor,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Alphabet display button toggle
                            Button(
                                onClick = { viewModel.toggleCaesarAlphabet(!state.showCaesarAlphabet) },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                            ) {
                                Text(
                                    text = if (state.showCaesarAlphabet) "Ẩn bảng chữ cái tiếng Anh" else "Xem bảng chữ cái tiếng Anh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (state.showCaesarAlphabet) {
                                // Render A-Z alphabet layout with index
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "BẢNG CHỮ CÁI TIẾNG ANH (A-Z) HỖ TRỢ GIẢI MÃ CAESAR LÙI:",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = goldColor
                                    )
                                    
                                    val alphabet = ('A'..'Z').toList()
                                    // Row showing A..M
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        alphabet.take(13).forEach { char ->
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = char.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(text = "${char - 'A'}", fontSize = 8.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Row showing N..Z
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        alphabet.takeLast(13).forEach { char ->
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = char.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(text = "${char - 'A'}", fontSize = 8.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    
                                    Text(
                                        text = "💡 Mẹo giải mã Caesar: Hãy lấy từng chữ cái ở kết quả mã hóa, đếm lùi sang phải (hoặc cộng thêm ${state.help1Shift} chữ cái trong vòng tròn A-Z) để khôi phục chữ gốc!",
                                        fontSize = 9.sp,
                                        color = Color.LightGray,
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.useDecoderHelp1() },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = cyanColor)
                            ) {
                                Text(
                                    text = "Kích hoạt Trợ giúp 1 (-15% điểm)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // HELP 2: Fill-in masked letters
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRỢ GIÚP 2: Điền khuyết ký tự",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                            Text(
                                text = "Trừ 65% điểm",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171)
                            )
                        }

                        if (state.help2Used) {
                            // Revealed masked name
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF020617), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Gợi ý hiển thị 50% - 70% đáp án thực tế:",
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = state.help2MaskedText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = goldColor,
                                    letterSpacing = 2.sp
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.useDecoderHelp2() },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = goldColor)
                            ) {
                                Text(
                                    text = "Kích hoạt Trợ giúp 2 (-65% điểm)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Collected Letters Group Card (Hidden Group Name Puzzle Board)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(1.dp, borderCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Mảnh Ghép Chữ Cái Nhóm Hiện Vật Ẩn",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor
                    )
                }
                
                Text(
                    text = "Hãy vượt qua các ải để thu thập các chữ cái bí ẩn và giải mã hoàn toàn tên nhóm hiện vật của vật bảo vật đang tìm kiếm!",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    lineHeight = 14.sp
                )

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                val puzzleName = getGroupPuzzleName(state.secretGroupTargetId)
                val words = puzzleName.split(" ")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tiến độ bẻ khóa tên nhóm hiện vật:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        words.forEach { word ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .background(Color(0x1A00F0FF), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, Color(0x3300F0FF), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                word.forEach { char ->
                                    val charStr = char.toString()
                                    val isCollected = state.collectedGroupLetters.contains(charStr)
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (isCollected) Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF2563EB)))
                                                else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                            )
                                            .border(
                                                1.dp,
                                                if (isCollected) Color(0xFF60A5FA) else Color(0xFF475569),
                                                RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isCollected) charStr else "?",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isCollected) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1.5: Collected Clues Summary (Directly beneath Letters)
        val collectedCluesList = state.collectedClues.values.filter { it.isNotBlank() }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(1.dp, borderCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Clues",
                        tint = cyanColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Manh Mối Lịch Sử Đã Thu Thập",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = cyanColor
                    )
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                if (collectedCluesList.isNotEmpty()) {
                    collectedCluesList.forEachIndexed { index, clue ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🔑 Manh mối #${index + 1}:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                            Text(
                                text = "\"$clue\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 15.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Chưa tìm thấy mật hiệu nào. Hãy tiếp tục giải mật các trạm để thu thập thông điệp ẩn!",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Section 1.8: Giỏ Hàng Cá Nhân (Thu Thập 4 Ảnh Từ Các Ải)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(1.2.dp, cyanColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = cyanColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Giỏ Hàng Cá Nhân (15 Trạm Di Sản)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = cyanColor
                    )
                }

                Text(
                    text = "Tất cả 15 ải đều có 4 ảnh tự động lưu vào giỏ hàng. Nhấp chọn hiện vật để xem & chọn ảnh đưa vào Bảng Ghép Puzzle 4 ô:",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )

                // Horizontal List of 15 Artifacts in Personal Bag
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val artifactList = listOf(
                        14 to "Xe tăng", 13 to "Xe Peugeot", 12 to "Xe bọc thép", 11 to "Tàu tuần tiễu PCF",
                        10 to "Trục máy B52", 9 to "Súng thần công", 8 to "Pháo", 7 to "Mỏ neo tàu",
                        6 to "Máy in Pédal", 5 to "Máy cán tol", 4 to "Lu hầm bí mật", 3 to "Ghe xuồng thuyền",
                        2 to "Bệ đạn tên lửa", 1 to "Bom Mỹ", 15 to "Máy bay trực thăng"
                    )
                    items(artifactList) { (artId, name) ->
                        val images = state.collectedImages[artId] ?: emptyList()
                        val isSelected = selectedBagArtifactId == artId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF1E3A8A) else Color(0xFF0F172A))
                                .border(1.dp, if (isSelected) cyanColor else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable { selectedBagArtifactId = artId }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.LightGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${images.size}/4 ảnh", fontSize = 8.5.sp, color = if (images.size >= 4) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                // Collected Images of selected artifact in Personal Bag
                val bagImages = state.collectedImages[selectedBagArtifactId] ?: emptyList()
                val currentBagArtifactName = when (selectedBagArtifactId) {
                    14 -> "Xe tăng"; 13 -> "Xe Peugeot"; 12 -> "Xe bọc thép"; 11 -> "Tàu tuần tiễu PCF"
                    10 -> "Trục máy B52"; 9 -> "Súng thần công"; 8 -> "Pháo"; 7 -> "Mỏ neo tàu"
                    6 -> "Máy in Pédal"; 5 -> "Máy cán tol"; 4 -> "Lu hầm bí mật"; 3 -> "Ghe xuồng thuyền"
                    2 -> "Bệ đạn tên lửa"; 1 -> "Bom Mỹ"; 15 -> "Máy bay trực thăng"
                    else -> "Hiện vật"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = goldColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Kho Ảnh $currentBagArtifactName (${bagImages.size}/4):",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldColor
                        )
                    }

                    if (bagImages.size < 4) {
                        Button(
                            onClick = {
                                val success = viewModel.buyArtifactImagesWithPoints(selectedBagArtifactId)
                                if (success) {
                                    val msg = "🎉 Đã dùng 100 điểm đổi đủ 4 ảnh cho $currentBagArtifactName!"
                                    viewModel.showTopNotification(msg, "success")
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                } else {
                                    val msg = "❌ Cần ít nhất 100 điểm để đổi 4 ảnh!"
                                    viewModel.showTopNotification(msg, "error")
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C2D12)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = goldColor, modifier = Modifier.size(14.dp))
                                Text("ĐỔI 100 ĐIỂM QUAY LẠI ẢI", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = goldColor)
                            }
                        }
                    }
                }

                // Display 4 Thumbnails from Bag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0..3) {
                        val b64 = bagImages.getOrNull(i)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, if (b64 != null) cyanColor else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (b64 != null) {
                                        val nextEmptyIndex = puzzleSlots.indexOfFirst { it == null }
                                        if (nextEmptyIndex != -1) {
                                            animSlotIndex = nextEmptyIndex
                                            val updated = puzzleSlots.toMutableList()
                                            updated[nextEmptyIndex] = b64
                                            puzzleSlots = updated
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (b64 != null) {
                                Base64Thumbnail(
                                    base64Str = b64,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                    artifactId = selectedBagArtifactId,
                                    pieceIndex = i
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Text("Chưa có", fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 0.5.dp)

                // 2x2 PUZZLE BOARD HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = null, tint = pinkColor, modifier = Modifier.size(18.dp))
                        Text(
                            text = "BẢNG GHÉP 4 MẢNH PUZZLE CHỌN LỌC:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = pinkColor
                        )
                    }
                }

                Text(
                    text = "Nhấp trực tiếp từng mảnh ảnh thu thập ở trên để tự nắm kéo đưa vào Bảng Ghép. Yêu cầu ghép đúng 4 mảnh của hiện vật dự đoán!",
                    fontSize = 9.5.sp,
                    color = Color.LightGray,
                    lineHeight = 13.sp
                )

                // 2x2 Puzzle Grid with 360-degree rotation animation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        for (i in 0..1) {
                            val slotB64 = puzzleSlots.getOrNull(i)
                            val isAnimSlot = animSlotIndex == i
                            val rotationDegrees by animateFloatAsState(
                                targetValue = if (isAnimSlot) 360f else 0f,
                                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                finishedListener = { if (animSlotIndex == i) animSlotIndex = null },
                                label = "slotRotation"
                            )
                            val scaleValue by animateFloatAsState(
                                targetValue = if (isAnimSlot) 1.25f else 1f,
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                label = "slotScale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .graphicsLayer {
                                        rotationZ = rotationDegrees
                                        scaleX = scaleValue
                                        scaleY = scaleValue
                                    }
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.2.dp, if (slotB64 != null) Color(0xFF4CAF50) else pinkColor, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (slotB64 != null) {
                                            val updated = puzzleSlots.toMutableList()
                                            updated[i] = null
                                            puzzleSlots = updated
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (slotB64 != null) {
                                    Base64Thumbnail(
                                        base64Str = slotB64,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        artifactId = selectedBagArtifactId,
                                        pieceIndex = i
                                    )
                                } else {
                                    Text("Mảnh ${i + 1}\n(Chạm mảnh trên)", fontSize = 8.5.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        for (i in 2..3) {
                            val slotB64 = puzzleSlots.getOrNull(i)
                            val isAnimSlot = animSlotIndex == i
                            val rotationDegrees by animateFloatAsState(
                                targetValue = if (isAnimSlot) 360f else 0f,
                                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                finishedListener = { if (animSlotIndex == i) animSlotIndex = null },
                                label = "slotRotation"
                            )
                            val scaleValue by animateFloatAsState(
                                targetValue = if (isAnimSlot) 1.25f else 1f,
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                label = "slotScale"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(80.dp)
                                    .graphicsLayer {
                                        rotationZ = rotationDegrees
                                        scaleX = scaleValue
                                        scaleY = scaleValue
                                    }
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.2.dp, if (slotB64 != null) Color(0xFF4CAF50) else pinkColor, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (slotB64 != null) {
                                            val updated = puzzleSlots.toMutableList()
                                            updated[i] = null
                                            puzzleSlots = updated
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (slotB64 != null) {
                                    Base64Thumbnail(
                                        base64Str = slotB64,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                        artifactId = selectedBagArtifactId,
                                        pieceIndex = i
                                    )
                                } else {
                                    Text("Mảnh ${i + 1}\n(Chạm mảnh trên)", fontSize = 8.5.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Input Guess Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = darkCard),
            border = BorderStroke(1.2.dp, pinkColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = pinkColor, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Dự Đoán Danh Tính Hiện Vật Bí Ẩn",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = pinkColor
                    )
                }

                Text(
                    text = "Chọn từ danh sách hiện vật thuộc nhóm bí ẩn tương ứng dưới đây (gõ lọc nhanh) để tự động điền chính xác tên có dấu tiếng Việt:",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )

                // Search field for the artifacts list
                OutlinedTextField(
                    value = artifactSearchQuery,
                    onValueChange = { artifactSearchQuery = it },
                    placeholder = { Text("Lọc nhanh hiện vật trong nhóm...", color = Color.Gray, fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = cyanColor, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (artifactSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { artifactSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = cyanColor,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                val numCleared = state.completedArtifactIds.size

                val all15Candidates = listOf(
                    "Bom Mỹ", "Bệ đạn tên lửa", "Ghe xuồng thuyền", "Lu hầm bí mật",
                    "Máy cán tôn", "Máy in Pédal", "Mỏ neo tàu", "Pháo",
                    "Súng thần công", "Trục máy B52", "Tàu tuần tiễu PCF", "Xe bọc thép",
                    "Xe Peugeot", "Xe tăng", "Máy bay trực thăng"
                )

                val filtered15Candidates = all15Candidates.filter { candidate ->
                    val cleanCandidate = viewModel.stripAccents(candidate).lowercase()
                    val cleanQuery = viewModel.stripAccents(artifactSearchQuery).lowercase()
                    cleanCandidate.contains(cleanQuery) || candidate.lowercase().contains(artifactSearchQuery.lowercase())
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF020617), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    val gridScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(gridScrollState),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (filtered15Candidates.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(top = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Không tìm thấy hiện vật phù hợp!",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        } else {
                             val canGuess = state.decoderLockoutSecondsLeft <= 0 && (
                                 if (numCleared < 15) state.totalScore > 150 else state.guessRemainingAttempts > 0
                             )
                            filtered15Candidates.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    pair.forEach { candidateName ->
                                        val isSelected = guessText == candidateName
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = canGuess) { guessText = candidateName }
                                                .background(
                                                    if (isSelected) Color(0x33FF007F) else Color(0xFF1E293B),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) pinkColor else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .alpha(if (canGuess) 1.0f else 0.4f)
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = candidateName,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) pinkColor else Color.White,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isSelected) pinkColor else Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    if (pair.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (numCleared < 15) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🛡️ Chế độ tích lũy (Dưới 15 chặng):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = cyanColor
                        )
                        Text(
                            text = "• Điều kiện: Cần ghép đủ 4 ảnh Puzzle và có trên 150 điểm.",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "• Hình phạt: Bẻ khóa sai trừ 150 điểm.",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Điểm hiện tại: ${state.totalScore}đ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.totalScore > 150) goldColor else Color(0xFFF87171)
                            )
                            if (state.decoderLockoutSecondsLeft > 0) {
                                Text(
                                    text = "Tạm khóa: ${state.decoderLockoutSecondsLeft}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🎓 Chế độ đỉnh cao (Sau 15 chặng):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = pinkColor
                        )
                        Text(
                            text = "• Không trừ điểm khi bẻ khóa sai.",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "• Yêu cầu ghép đủ 4 ảnh Puzzle & nhận 3 lượt đoán ban đầu.",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val maxAllowed = 3 + (if (state.help1Used) 1 else 0) + (if (state.help2Used) 1 else 0)
                            Text(
                                text = "Lượt đoán còn lại: ${state.guessRemainingAttempts}/$maxAllowed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.guessRemainingAttempts <= 0) Color(0xFFF87171) else goldColor
                            )
                            if (state.decoderLockoutSecondsLeft > 0) {
                                Text(
                                    text = "Tạm khóa: ${state.decoderLockoutSecondsLeft}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (state.decoderLockoutSecondsLeft > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x66EF4444), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚠️ Bị khóa chức năng đoán do chọn sai!\nVui lòng suy nghĩ thêm manh mối và thử lại sau ${state.decoderLockoutSecondsLeft} giây.",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                } else if (numCleared < 15 && state.totalScore <= 150) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❌ BẠN CHƯA ĐỦ ĐIỂM ĐỂ BẺ KHÓA!\nBạn cần có trên 150 điểm (hiện tại: ${state.totalScore} điểm) để trả lời. Hãy tham gia giải đố các chặng để tích lũy thêm điểm số!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171),
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = guessText,
                        onValueChange = { guessText = it },
                        label = { Text("Tên hiện vật dự đoán", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("secret_artifact_guess_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = pinkColor,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        singleLine = true
                    )

                    val buttonGradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1D4ED8),
                            Color(0xFFD97706),
                            Color(0xFFDC2626)
                        )
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            val filledSlots = puzzleSlots.filterNotNull()

                            if (guessText.isBlank()) {
                                val msg = "Vui lòng nhập hoặc chọn tên hiện vật dự đoán!"
                                viewModel.showTopNotification(msg, "warning")
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            } else if (filledSlots.size < 4) {
                                val msg = "⚠️ CHƯA GHÉP ĐỦ 4 MẢNH PUZZLE! Vui lòng chạm các mảnh ảnh ở trên để ghép đủ 4 mảnh theo thứ tự (1 đến 4) vào Bảng Ghép!"
                                viewModel.showTopNotification(msg, "warning")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            } else {
                                val (success, detailMsg) = viewModel.guessSecretArtifact(guessText, puzzleSlots)
                                if (success) {
                                    viewModel.solvePuzzle(selectedBagArtifactId)
                                    viewModel.awardPuzzleBonus(150)
                                    viewModel.showTopNotification(detailMsg, "success")
                                    Toast.makeText(context, detailMsg, Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.showTopNotification(detailMsg, "error")
                                    Toast.makeText(context, detailMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(buttonGradient)
                            .testTag("secret_artifact_guess_submit"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text(
                                text = "BẺ KHÓA NGAY",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EndGameModal(
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    onDismiss: () -> Unit
) {
    val receipt = state.endGameReceipt ?: return
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.dismissEndGameModal() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2091428)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(
                listOf(Color(0xFFEF4444), Color(0xFFFFD700), Color(0xFF00F0FF))
            )),
            elevation = CardDefaults.cardElevation(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with Exit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF3F1212),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.padding(8.dp).size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (receipt.isAutoTimeout) "⏱️ TỰ ĐỘNG KẾT THÚC (5 H)" else "🏁 KẾT THÚC THÁM HIỂM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "BẢO TÀNG QUÂN KHU 9",
                                fontSize = 10.sp,
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.dismissEndGameModal() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Thoát",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Receipt Details Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReceiptRow("👤 Tên khách đăng nhập:", receipt.userName, Color.White)
                            ReceiptRow("🎟️ Mã số vé:", receipt.ticketCode, Color(0xFF00F0FF))
                            ReceiptRow("📧 Email nhận báo cáo:", receipt.userEmail, Color(0xFFFFD700))

                            HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                            ReceiptRow("🏆 Tổng điểm đạt được:", "${receipt.totalScore} điểm", Color(0xFFFFD700))
                            ReceiptRow("⏱️ Thời gian trải nghiệm:", receipt.playTimeFormatted, Color(0xFF38BDF8))
                            ReceiptRow("🟢 Tổng số màn đã tham quan:", "${receipt.totalVisited} / 15 màn", Color(0xFF10B981))
                            ReceiptRow("🟡 Tổng số màn đã bỏ qua:", "${receipt.totalSkipped} / 15 màn", Color(0xFFFBBF24))
                        }
                    }

                    // Email Banner Confirmation & Manual Open Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x3010B981),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MarkEmailRead,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Đã phát thư đến email: ${receipt.userEmail}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA7F3D0),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    com.example.network.EmailService.launchEmailAppIntent(
                                        context = context,
                                        targetEmail = receipt.userEmail,
                                        userName = receipt.userName,
                                        ticketCode = receipt.ticketCode,
                                        totalScore = receipt.totalScore,
                                        playTimeFormatted = receipt.playTimeFormatted,
                                        totalVisited = receipt.totalVisited,
                                        totalSkipped = receipt.totalSkipped,
                                        heroTitle = viewModel.getHeroTitle()
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "MỞ ỨNG DỤNG MAIL ĐỂ GỬI TRỰC TIẾP",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Thank you Message
                    Text(
                        text = receipt.thankYouMessage,
                        fontSize = 11.5.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row: Close / Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.dismissEndGameModal() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("THOÁT / ĐÓNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.confirmEndGameAndReset() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RESET MỚI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.5.sp, color = valueColor, fontWeight = FontWeight.Bold)
    }
}
