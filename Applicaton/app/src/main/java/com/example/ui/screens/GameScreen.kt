package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.activity.compose.BackHandler
import com.example.data.Artifact
import com.example.data.GameType
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MuseumViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.*

// ==========================================
// THESIS ENHANCEMENT: DYNAMIC FREQUENCY SOUND SYNTH (ToneGenerator)
// ==========================================
object SoundSynth {
    private var toneGenerator: ToneGenerator? = null
    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun playTone(freqHz: Int, durationMs: Int) {
        try {
            val toneType = when {
                freqHz > 1200 -> ToneGenerator.TONE_PROP_BEEP
                freqHz > 800 -> ToneGenerator.TONE_CDMA_HIGH_L
                freqHz < 200 -> ToneGenerator.TONE_SUP_ERROR
                else -> ToneGenerator.TONE_PROP_ACK
            }
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            // Sound card fallback
        }
    }
}

// Restoration Cell State Definition
data class RestorationCell(
    val id: Int,
    val isTrap: Boolean = false,
    val isBuff: Boolean = false,
    var scanned: Boolean = false,
    var cryptoChar: String = "bi-stars",
    var answered: Boolean = false,
    var revealed: Boolean = false,
    val hasMasterKey: Boolean = false, // THESIS UPGRADE: Heritage Master Key placement flag
    var isSkipped: Boolean = false
)

data class CornerLock(
    val position: String, // "top-left", "top-right", "bottom-left", "bottom-right"
    var answered: Boolean = false,
    var isSkipped: Boolean = false
)

// Surprise Mission state data class
data class SurpriseMission(
    val id: Int,
    val title: String,
    val description: String,
    val detail: String,
    var statusText: String,
    var isCompleted: Boolean = false
)

// Jigsaw Piece state
data class JigsawPiece(
    val id: Int,
    val bitmap: android.graphics.Bitmap? = null,
    val base64: String? = null,
    val label: String = ""
)

@Composable
fun DecryptionChallengeView(
    artifactId: Int,
    state: com.example.viewmodel.MuseumUiState,
    viewModel: MuseumViewModel,
    jigsawPieces: List<com.example.ui.screens.JigsawPiece>,
    onSolved: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🧩 GHÉP MẢNH PHỤC CHẾ (CHỌN MẢNH ĐÚNG)", color = Color.Cyan, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(jigsawPieces.size) { index ->
                val piece = jigsawPieces[index]
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(if (selectedIndex == index) 2.dp else 0.dp, Color.Yellow, RoundedCornerShape(8.dp))
                        .clickable { selectedIndex = index }
                        .background(Color.Gray)
                ) {
                    if (piece.bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = piece.bitmap.asImageBitmap(),
                            contentDescription = "Piece",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(piece.label, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center, color = Color.White)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (selectedIndex != null) {
                    // Collect ALL images
                    jigsawPieces.forEach { piece ->
                        piece.base64?.let { viewModel.collectImage(artifactId, it) }
                    }
                    onSolved("SECRET_KEY_${artifactId * 7}")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("THU THẬP & XÁC NHẬN")
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = {
                val success = viewModel.spendPointsForRetry()
                if (success) {
                    selectedIndex = null
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BỎ 100 ĐIỂM ĐỂ QUAY LẠI (THỬ LẠI)")
        }
    }
}

fun base64ToBitmap(base64Str: String, artifactId: Int = 1, pieceIndex: Int = 0): android.graphics.Bitmap {
    return try {
        if (!base64Str.startsWith("FALLBACK_") && !base64Str.startsWith("MẢNH_") && base64Str.length >= 50) {
            val cleanStr = base64Str.substringAfter("base64,")
            val decodedBytes = android.util.Base64.decode(cleanStr, android.util.Base64.DEFAULT)
            val bmp = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bmp != null) bmp else generateGamePieceBitmap(base64Str, artifactId, pieceIndex)
        } else {
            generateGamePieceBitmap(base64Str, artifactId, pieceIndex)
        }
    } catch (e: Exception) {
        generateGamePieceBitmap(base64Str, artifactId, pieceIndex)
    }
}

fun generateGamePieceBitmap(base64Str: String, artifactId: Int, pieceIndex: Int): android.graphics.Bitmap {
    val width = 400
    val height = 400
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    val cx = width / 2f
    val cy = height / 2f
    val artifactName = com.example.data.MuseumRepository.artifacts.find { it.id == artifactId }?.name ?: "CỔ VẬT #$artifactId"

    // 1. Dynamic Space / Metallic Dark Canvas
    val bgColors = listOf(
        0xFF021B38.toInt(), 0xFF052319.toInt(), 0xFF2B1800.toInt(), 0xFF1D0E32.toInt(),
        0xFF330909.toInt(), 0xFF0A2246.toInt(), 0xFF082623.toInt(), 0xFF261904.toInt()
    )
    val bg1 = bgColors[artifactId % bgColors.size]
    val bg2 = 0xFF020914.toInt()
    val shader = android.graphics.LinearGradient(
        0f, 0f, width.toFloat(), height.toFloat(),
        bg1, bg2, android.graphics.Shader.TileMode.CLAMP
    )
    paint.shader = shader
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.shader = null

    // 2. Hologram Matrix Grid
    paint.color = 0x1A00E5FF.toInt()
    paint.strokeWidth = 1.5f
    for (i in 0..width step 25) {
        canvas.drawLine(i.toFloat(), 0f, i.toFloat(), height.toFloat(), paint)
        canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), paint)
    }

    // 3. Central Radial Glow Aura Ring
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = 0x2200E5FF.toInt()
    canvas.drawCircle(cx, cy, 140f, paint)
    paint.color = 0x33FFD700.toInt()
    canvas.drawCircle(cx, cy, 100f, paint)

    // 4. Draw Specific 3D Vector Graphic per Artifact
    when (artifactId) {
        1 -> { // Bom (Bomb)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFF2D3748.toInt()
            val bombRect = android.graphics.RectF(cx - 50f, cy - 90f, cx + 50f, cy + 60f)
            canvas.drawOval(bombRect, paint)
            paint.color = 0xFFE53E3E.toInt()
            val nosePath = android.graphics.Path()
            nosePath.moveTo(cx - 35f, cy + 40f)
            nosePath.lineTo(cx, cy + 85f)
            nosePath.lineTo(cx + 35f, cy + 40f)
            nosePath.close()
            canvas.drawPath(nosePath, paint)
            paint.color = 0xFF1A202C.toInt()
            canvas.drawRect(cx - 65f, cy - 105f, cx - 40f, cy - 75f, paint)
            canvas.drawRect(cx + 40f, cy - 105f, cx + 65f, cy - 75f, paint)
            paint.color = 0xFFD69E2E.toInt()
            canvas.drawRect(cx - 48f, cy - 20f, cx + 48f, cy - 5f, paint)
        }
        2 -> { // Bệ & Đạn Tên Lửa (SAM-2 Rocket)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFFCBD5E0.toInt()
            canvas.drawRect(cx - 18f, cy - 110f, cx + 18f, cy + 50f, paint)
            paint.color = 0xFFE53E3E.toInt()
            val tipPath = android.graphics.Path()
            tipPath.moveTo(cx - 18f, cy - 110f)
            tipPath.lineTo(cx, cy - 150f)
            tipPath.lineTo(cx + 18f, cy - 110f)
            tipPath.close()
            canvas.drawPath(tipPath, paint)
            paint.color = 0xFF2B6CB0.toInt()
            val finLeft = android.graphics.Path()
            finLeft.moveTo(cx - 18f, cy + 10f)
            finLeft.lineTo(cx - 55f, cy + 60f)
            finLeft.lineTo(cx - 18f, cy + 50f)
            finLeft.close()
            canvas.drawPath(finLeft, paint)
            val finRight = android.graphics.Path()
            finRight.moveTo(cx + 18f, cy + 10f)
            finRight.lineTo(cx + 55f, cy + 60f)
            finRight.lineTo(cx + 18f, cy + 50f)
            finRight.close()
            canvas.drawPath(finRight, paint)
            paint.color = 0xFF4A5568.toInt()
            canvas.drawRect(cx - 60f, cy + 55f, cx + 60f, cy + 75f, paint)
            paint.color = 0xFFDD6B20.toInt()
            canvas.drawCircle(cx, cy + 70f, 22f, paint)
            paint.color = 0xFFF6AD55.toInt()
            canvas.drawCircle(cx, cy + 70f, 12f, paint)
        }
        3 -> { // Ghe, xuồng, thuyền (Sampan Boat)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFF7B341E.toInt()
            val hull = android.graphics.Path()
            hull.moveTo(cx - 110f, cy)
            hull.cubicTo(cx - 60f, cy + 55f, cx + 60f, cy + 55f, cx + 110f, cy)
            hull.lineTo(cx + 80f, cy + 10f)
            hull.lineTo(cx - 80f, cy + 10f)
            hull.close()
            canvas.drawPath(hull, paint)
            paint.color = 0xFFD69E2E.toInt()
            paint.strokeWidth = 6f
            paint.style = android.graphics.Paint.Style.STROKE
            canvas.drawLine(cx - 30f, cy - 30f, cx + 40f, cy + 30f, paint)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xAA3182CE.toInt()
            canvas.drawRect(cx - 130f, cy + 25f, cx + 130f, cy + 85f, paint)
        }
        4 -> { // Lu hầm bí mật (Secret Urn Jar)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFFC05621.toInt()
            canvas.drawCircle(cx, cy, 75f, paint)
            paint.color = 0xFF7B341E.toInt()
            canvas.drawRect(cx - 45f, cy - 90f, cx + 45f, cy - 70f, paint)
            paint.color = 0xFF2F855A.toInt()
            canvas.drawRoundRect(cx - 50f, cy - 100f, cx + 50f, cy - 85f, 8f, 8f, paint)
            paint.color = 0xFFFFD700.toInt()
            canvas.drawCircle(cx, cy + 10f, 25f, paint)
        }
        8, 9 -> { // Pháo / Súng thần công (Cannon)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFF975A16.toInt()
            canvas.drawCircle(cx - 40f, cy + 30f, 45f, paint)
            canvas.drawCircle(cx + 40f, cy + 30f, 45f, paint)
            paint.color = 0xFF1A202C.toInt()
            canvas.drawCircle(cx - 40f, cy + 30f, 25f, paint)
            canvas.drawCircle(cx + 40f, cy + 30f, 25f, paint)
            paint.color = if (artifactId == 9) 0xFFD69E2E.toInt() else 0xFF4A5568.toInt()
            val barrel = android.graphics.Path()
            barrel.moveTo(cx - 70f, cy + 20f)
            barrel.lineTo(cx + 80f, cy - 40f)
            barrel.lineTo(cx + 90f, cy - 20f)
            barrel.lineTo(cx - 60f, cy + 40f)
            barrel.close()
            canvas.drawPath(barrel, paint)
        }
        10, 15 -> { // Aircraft / Helicopter
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFF2D3748.toInt()
            val planePath = android.graphics.Path()
            planePath.moveTo(cx, cy - 100f)
            planePath.lineTo(cx + 25f, cy - 20f)
            planePath.lineTo(cx + 120f, cy + 20f)
            planePath.lineTo(cx + 25f, cy + 40f)
            planePath.lineTo(cx + 35f, cy + 90f)
            planePath.lineTo(cx, cy + 70f)
            planePath.lineTo(cx - 35f, cy + 90f)
            planePath.lineTo(cx - 25f, cy + 40f)
            planePath.lineTo(cx - 120f, cy + 20f)
            planePath.lineTo(cx - 25f, cy - 20f)
            planePath.close()
            canvas.drawPath(planePath, paint)
            paint.color = 0xFF00E5FF.toInt()
            canvas.drawCircle(cx, cy - 40f, 18f, paint)
            if (artifactId == 15) {
                paint.color = 0xFFFFD700.toInt()
                paint.strokeWidth = 6f
                paint.style = android.graphics.Paint.Style.STROKE
                canvas.drawLine(cx - 130f, cy - 40f, cx + 130f, cy - 40f, paint)
                canvas.drawLine(cx, cy - 130f, cx, cy + 50f, paint)
            }
        }
        13, 14 -> { // Car / Tank
            paint.style = android.graphics.Paint.Style.FILL
            if (artifactId == 14) { // Tank
                paint.color = 0xFF1A202C.toInt()
                canvas.drawRoundRect(cx - 100f, cy + 10f, cx + 100f, cy + 65f, 25f, 25f, paint)
                paint.color = 0xFF2F855A.toInt()
                canvas.drawRoundRect(cx - 80f, cy - 35f, cx + 80f, cy + 25f, 15f, 15f, paint)
                canvas.drawCircle(cx - 10f, cy - 30f, 38f, paint)
                paint.color = 0xFF2D3748.toInt()
                canvas.drawRect(cx + 15f, cy - 40f, cx + 120f, cy - 25f, paint)
                paint.color = 0xFFE53E3E.toInt()
                canvas.drawCircle(cx - 10f, cy - 30f, 14f, paint)
            } else { // Peugeot Classic Car
                paint.color = 0xFFC53030.toInt()
                canvas.drawRoundRect(cx - 90f, cy - 15f, cx + 90f, cy + 45f, 20f, 20f, paint)
                canvas.drawRoundRect(cx - 50f, cy - 55f, cx + 40f, cy, 15f, 15f, paint)
                paint.color = 0xFF1A202C.toInt()
                canvas.drawCircle(cx - 55f, cy + 45f, 24f, paint)
                canvas.drawCircle(cx + 55f, cy + 45f, 24f, paint)
                paint.color = 0xFFEDF2F7.toInt()
                canvas.drawCircle(cx - 55f, cy + 45f, 12f, paint)
                canvas.drawCircle(cx + 55f, cy + 45f, 12f, paint)
            }
        }
        else -> { // Generic Heritage Relic (Bronze Drum / Crown / Trophy)
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFFD69E2E.toInt()
            canvas.drawCircle(cx, cy, 70f, paint)
            paint.color = 0xFF020914.toInt()
            canvas.drawCircle(cx, cy, 55f, paint)
            paint.color = 0xFF00E5FF.toInt()
            val path = android.graphics.Path()
            val outerR = 40f
            val innerR = 18f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = i * Math.PI / 5 - Math.PI / 2
                val x = (cx + r * Math.cos(angle)).toFloat()
                val y = (cy + r * Math.sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            canvas.drawPath(path, paint)
        }
    }

    // 5. Quadrant Highlight Overlay for the Piece
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 6f
    paint.color = 0xFFFFD700.toInt()
    
    val left = if (pieceIndex % 2 == 0) 10f else cx
    val top = if (pieceIndex < 2) 10f else cy
    val right = if (pieceIndex % 2 == 0) cx else width - 10f
    val bottom = if (pieceIndex < 2) cy else height - 10f
    
    canvas.drawRect(left, top, right, bottom, paint)
    
    // Outer Neon Border
    paint.strokeWidth = 8f
    paint.color = 0xFF00F0FF.toInt()
    canvas.drawRect(5f, 5f, width - 5f, height - 5f, paint)

    // Sleek Header Banner Badge
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = 0xDD041225.toInt()
    canvas.drawRoundRect(15f, 12f, width - 15f, 60f, 12f, 12f, paint)

    paint.color = 0xFFF3C623.toInt()
    paint.textSize = 20f
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    paint.textAlign = android.graphics.Paint.Align.CENTER
    canvas.drawText("MẢNH GIẢI MÃ #${pieceIndex + 1}/4", cx, 42f, paint)

    // Bottom Artifact Name Pill Badge
    paint.color = 0xDD041225.toInt()
    canvas.drawRoundRect(15f, height - 60f, width - 15f, height - 12f, 12f, 12f, paint)

    paint.color = 0xFF00E5FF.toInt()
    paint.textSize = 16f
    paint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    canvas.drawText(artifactName.uppercase(), cx, height - 28f, paint)

    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GameScreen(
    artifactId: Int,
    viewModel: MuseumViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val artifact = state.artifacts.find { it.id == artifactId }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Futuristic Cosmic UI Colors
    val goldColor = Color(0xFFF3C623)
    val darkBackground = Color(0xFF030D1B)
    val emeraldColor = Color(0xFF0D5C3A)
    val alertRedColor = Color(0xFFFF4848)
    val neonCyanColor = Color(0xFF00E5FF)

    // GPS real-time calculations
    val distance = if (artifact != null) viewModel.getDistanceToArtifact(artifact) else 0.0
    val isWithinRange = if (artifact != null) viewModel.isWithinRange(artifact) else false

    // Sound synth shortcuts
    fun playClickSound() = SoundSynth.playTone(880, 80)
    fun playSuccessSound() = SoundSynth.playTone(1200, 180)
    fun playFailureSound() = SoundSynth.playTone(150, 300)
    fun playRadarSound() = SoundSynth.playTone(600, 120)
    fun playKeyFoundSound() = SoundSynth.playTone(1500, 400)

    // 2. Grid & Corner initialization with THESIS UPGRADE: Hidden Heritage Master Key
    val pieceCellMapping = remember(artifactId) {
        (0..8).shuffled().take(4)
    }

    val cells = remember(artifactId) {
        val nonPieceIndices = (0..8).filter { it !in pieceCellMapping }
        val masterKeyCellIndex = if (nonPieceIndices.isNotEmpty()) nonPieceIndices.random() else -1
        mutableStateListOf(
            RestorationCell(id = 0, isTrap = false, isBuff = false, cryptoChar = "🛡️", hasMasterKey = masterKeyCellIndex == 0),
            RestorationCell(id = 1, isTrap = false, isBuff = false, cryptoChar = "🚩", hasMasterKey = masterKeyCellIndex == 1),
            RestorationCell(id = 2, isTrap = true, isBuff = false, cryptoChar = "⚠️", hasMasterKey = masterKeyCellIndex == 2),
            RestorationCell(id = 3, isTrap = false, isBuff = false, cryptoChar = "📖", hasMasterKey = masterKeyCellIndex == 3),
            RestorationCell(id = 4, isTrap = false, isBuff = true, cryptoChar = "💎", hasMasterKey = masterKeyCellIndex == 4),
            RestorationCell(id = 5, isTrap = false, isBuff = false, cryptoChar = "🔭", hasMasterKey = masterKeyCellIndex == 5),
            RestorationCell(id = 6, isTrap = true, isBuff = false, cryptoChar = "⚠️", hasMasterKey = masterKeyCellIndex == 6),
            RestorationCell(id = 7, isTrap = false, isBuff = false, cryptoChar = "🏛️", hasMasterKey = masterKeyCellIndex == 7),
            RestorationCell(id = 8, isTrap = false, isBuff = false, cryptoChar = "🏆", hasMasterKey = masterKeyCellIndex == 8)
        )
    }

    val corners = remember(artifactId) {
        mutableStateListOf(
            CornerLock("top-left"),
            CornerLock("top-right"),
            CornerLock("bottom-left"),
            CornerLock("bottom-right")
        )
    }

    // Interactive Game Stats
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var keys by remember { mutableStateOf(3) }
    var combo by remember { mutableStateOf(0) }
    var xrayActive by remember { mutableStateOf(false) }
    val locatedImageCells = remember(artifactId) { mutableStateListOf<Int>() }

    var showBriefingScreen by remember(artifactId) { mutableStateOf(true) }
    var scoreGainAnimationText by remember { mutableStateOf("") }
    var scoreGainAnimActive by remember { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        showBackConfirmDialog = true
    }

    LaunchedEffect(scoreGainAnimActive) {
        if (scoreGainAnimActive) {
            delay(1500)
            scoreGainAnimActive = false
        }
    }

    // 15 SURPRISE MISSIONS SYSTEM STATES (THESIS SPECIAL)
    val activeSurpriseMissions = remember(artifactId) {
        val all = listOf(
            SurpriseMission(1, "Chế độ Giải Mật", "Quét trích xuất mã QR & tọa độ vệ tinh.", "Giải mã ảnh stego trích xuất mã QR và tọa độ GPS của di sản bảo tàng.", "Chờ kích hoạt", false),
            SurpriseMission(2, "Nhúng Stego AI", "Mã hóa câu nói mật của riêng bạn vào ảnh.", "Tự tạo một file ảnh mật chứa thông điệp riêng bằng kỹ thuật pixel LSB.", "Chưa thực hiện", false),
            SurpriseMission(3, "La Bàn Săn Tìm", "Tìm tọa độ Xe Tăng T54-B ẩn giấu.", "Sử dụng radar định hướng và radar quân sự để tiến cận rương cổ vật.", "Đang dò tìm", false),
            SurpriseMission(4, "Căn Phòng Escape", "Mở khóa điện tử bảo vệ căn hầm chứa cổ vật.", "Vượt khóa mật mã bảo mật bằng năm lịch sử Điện Biên Phủ.", "Đang khóa 🔒", false),
            SurpriseMission(5, "Giải Mã Đa Tầng", "Vượt 3 lớp bảo mật Caesar + Base64 di sản.", "Trích xuất mã nhị phân cốt lõi qua các giai đoạn.", "Mức bảo mật: Cao", false),
            SurpriseMission(6, "AI Trợ Lý Pixel", "AI phân tích độ sáng để phát hiện watermark ẩn.", "Nhờ trợ lý AI RAG quét toàn diện lớp pixel của bức họa.", "Sẵn sàng", false),
            SurpriseMission(7, "Quay Điểm Thưởng", "Nhân đôi điểm khảo cổ bằng vòng quay may mắn.", "Tính điểm dựa trên thời gian và nhân hệ số ngẫu nhiên.", "Khả dụng x1.0", false),
            SurpriseMission(8, "Thách Đấu Co-op", "Giải mã mã thử thách chia sẻ từ đồng đội.", "Kiểm thử truyền tin nội bộ bảo tàng thông qua mã Base64.", "Nhập mã...", false),
            SurpriseMission(9, "Thị Giác Máy Tính", "AI yêu cầu phân loại hiện vật trước khi giải.", "Xác thực ảnh qua camera: Nhận diện pháo cao xạ hay xe tăng.", "Chờ camera...", false),
            SurpriseMission(10, "Định Vị Nhiệm Vụ", "Đứng đúng tọa độ hiện vật để đồng bộ AR.", "Xác thực vệ tinh không gian trưng bày để giải phóng thông tin.", "Chưa định vị", false),
            SurpriseMission(11, "Mảnh Ghép Bí Mật", "Thu thập 6 ký tự bí mật ghép chữ MUSEAI.", "Tìm kiếm các ký tự di sản rải rác. Ghép đúng từ khóa.", "Mảnh: 0/6", false),
            SurpriseMission(12, "Hologram Xếp Hạng", "Cập nhật thành tích của bạn lên bảng vàng.", "Ghi danh bảng xếp hạng mật mã học toàn cầu của bảo tàng.", "Hạng: Chưa rõ", false),
            SurpriseMission(13, "AI Challenge Creator", "AI tự động sinh đề bài từ Gemini RAG.", "Trực tiếp yêu cầu AI sinh ra câu đố stego ngẫu nhiên kèm đáp án.", "Sẵn sàng", false),
            SurpriseMission(14, "Thành Tựu Danh Giá", "Nhận các huy hiệu vinh danh đặc vụ bảo tàng.", "Bảng vàng danh vọng cho các chuyên gia am hiểu lịch sử.", "Tiến trình: 0%", false),
            SurpriseMission(15, "Điều Chỉnh Độ Khó", "Chọn mức Easy, Medium, Hard, Expert để chơi.", "Thay đổi cơ chế lưới, chướng ngại vật và hệ số thưởng.", "Cấp: Dễ", false)
        )
        mutableStateListOf<SurpriseMission>().apply {
            addAll(all.shuffled().take(5))
        }
    }
    var selectedMissionIdForDialog by remember { mutableStateOf<Int?>(null) }

    // Mission 1 states
    var showM1ScanResult by remember { mutableStateOf(false) }

    // Mission 2 states
    var m2MsgInput by remember { mutableStateOf("") }
    var m2PassInput by remember { mutableStateOf("") }
    var m2HintInput by remember { mutableStateOf("") }
    var isM2Encrypting by remember { mutableStateOf(false) }
    var m2SuccessResult by remember { mutableStateOf(false) }

    // Mission 3 states
    var m3Distance by remember { mutableStateOf(42) }

    // Mission 4 states
    var m4CodeInput by remember { mutableStateOf("") }
    var m4Unlocked by remember { mutableStateOf(false) }

    // Mission 5 states
    var m5Step by remember { mutableStateOf(1) }
    var m5Input1 by remember { mutableStateOf("") }
    var m5Input2 by remember { mutableStateOf("") }
    var m5Solved by remember { mutableStateOf(false) }

    // Mission 6 states
    var m6HintsReceived by remember { mutableStateOf(false) }

    // Mission 7 states
    var m7Spun by remember { mutableStateOf(false) }
    var m7Multiplier by remember { mutableStateOf(1.0f) }

    // Mission 8 states
    var m8CodeText by remember { mutableStateOf("") }
    var m8Solved by remember { mutableStateOf(false) }

    // Mission 9 states
    var m9SelectedOption by remember { mutableStateOf("") }
    var m9Solved by remember { mutableStateOf(false) }

    // Mission 10 states
    var m10Synced by remember { mutableStateOf(false) }

    // Mission 11 states
    var m11Count by remember { mutableStateOf(0) }
    var m11WordInput by remember { mutableStateOf("") }
    var m11Solved by remember { mutableStateOf(false) }

    // Mission 12 states
    var m12NameInput by remember { mutableStateOf("") }
    var m12Registered by remember { mutableStateOf(false) }

    // Mission 13 states
    var m13GeneratedQuestion by remember { mutableStateOf("") }
    var m13AnswerInput by remember { mutableStateOf("") }
    var m13Generating by remember { mutableStateOf(false) }
    var m13Solved by remember { mutableStateOf(false) }

    // Mission 14 states
    var m14Claimed by remember { mutableStateOf(false) }

    // Mission 15 states
    var m15Level by remember { mutableStateOf("Easy") }

    // Master Key finding states
    var showMasterKeyDialog by remember { mutableStateOf(false) }
    var hasObtainedMasterKey by remember { mutableStateOf(false) }

    // CNN AI verification states
    var coreArtifactUploaded by remember { mutableStateOf(false) }
    var coreSuccess by remember { mutableStateOf(false) }
    var coreImageUri by remember { mutableStateOf<Uri?>(null) }
    var isVerifyingCore by remember { mutableStateOf(false) }

    // Jigsaw state variables
    val jigsawPieces = remember { mutableStateListOf<JigsawPiece>() }
    val currentPiecePositions = remember { mutableStateListOf<Int>() }
    var selectedPieceIndex by remember { mutableStateOf<Int?>(null) }
    var isJigsawSolved by remember { mutableStateOf(false) }
    var isFetchingDecryptImages by remember { mutableStateOf(false) }
    var decryptApiError by remember { mutableStateOf<String?>(null) }
    var decryptedSecretCode by remember { mutableStateOf("") }

    // Real Camera capture classifier
    var showCoreImageSourceDialog by remember { mutableStateOf(false) }

    val coreCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            coreArtifactUploaded = true
            isVerifyingCore = true
            coroutineScope.launch {
                try {
                    val tempFile = File(context.cacheDir, "core_camera_capture.jpg")
                    java.io.FileOutputStream(tempFile).use { out ->
                        it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    coreImageUri = Uri.fromFile(tempFile)
                    
                    val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                    val res = com.example.network.LocalClient.predictApiService.predict(body)
                    val responseString = res.string()

                    val jsonObject = org.json.JSONObject(responseString)
                    val resultsArray = jsonObject.optJSONArray("results")
                    val firstResult = resultsArray?.optJSONObject(0)
                    val detectedId = firstResult?.opt("cnn_id")?.toString()?.toIntOrNull()

                    if (detectedId == artifactId) {
                        coreSuccess = true
                        score *= 2
                        playSuccessSound()
                        val msg = "✅ XÁC THỰC LÕI THÀNH CÔNG: Nhân đôi toàn bộ điểm chặng!"
                        viewModel.showTopNotification(msg, "success")
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    } else {
                        coreSuccess = false
                        playFailureSound()
                        val msg = "Ảnh không trùng khớp cổ vật hiện tại. Bạn có thể dùng chế độ mô phỏng!"
                        viewModel.showTopNotification(msg, "warning")
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    coreSuccess = false
                    playFailureSound()
                    val msg = "Lỗi kết nối máy chủ nhận dạng. Thử lại hoặc dùng chế độ mô phỏng trực tiếp!"
                    viewModel.showTopNotification(msg, "error")
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } finally {
                    isVerifyingCore = false
                }
            }
        }
    }

    LaunchedEffect(artifactId) {
        // Fetch 4 images/challenges for every artifact
        isFetchingDecryptImages = true
        decryptApiError = null
        
        val artifactConfig = when (artifactId) {
            14 -> "decrypt-xetang" to ("20260608_223506" to "D:/LUAN_VAN/MaHoa/runs/XeTang/game_images")
            13 -> "decrypt-xepeugeot" to ("20260609_022932" to "D:/LUAN_VAN/MaHoa/runs/xepeugeot/game_images")
            12 -> "decrypt-xebocthep" to ("20260609_022856" to "D:/LUAN_VAN/MaHoa/runs/xebocthep/game_images")
            11 -> "decrypt-tautuantieupcf" to ("20260609_022801" to "D:/LUAN_VAN/MaHoa/runs/tautuantieupcf/game_images")
            10 -> "decrypt-trucmaybayb52" to ("20260609_022829" to "D:/LUAN_VAN/MaHoa/runs/trucmaybayb52/game_images")
            9  -> "decrypt-sungthancong" to ("20260609_022727" to "D:/LUAN_VAN/MaHoa/runs/sungthancong/game_images")
            8  -> "decrypt-phao" to ("20260609_022700" to "D:/LUAN_VAN/MaHoa/runs/phao/game_images")
            7  -> "decrypt-moneotau" to ("20260609_022632" to "D:/LUAN_VAN/MaHoa/runs/moneotau/game_images")
            6  -> "decrypt-mayinpedal" to ("20260609_022516" to "D:/LUAN_VAN/MaHoa/runs/mayinpedal/game_images")
            5  -> "decrypt-maycantol" to ("20260609_022252" to "D:/LUAN_VAN/MaHoa/runs/maycantol/game_images")
            4  -> "decrypt-luhambimat" to ("20260609_021843" to "D:/LUAN_VAN/MaHoa/runs/luhambimat/game_images")
            3  -> "decrypt-ghexuongthuyen" to ("20260609_021659" to "D:/LUAN_VAN/MaHoa/runs/ghexuongthuyen/game_images")
            2  -> "decrypt-bevadantenlua" to ("20260609_025816" to "D:/LUAN_VAN/MaHoa/runs/bevadantenlua/game_images")
            1  -> "decrypt-bom" to ("20260609_021522" to "D:/LUAN_VAN/MaHoa/runs/Bom/game_images")
            15 -> "decrypt-maybaytructhang" to ("20260609_021952" to "D:/LUAN_VAN/MaHoa/runs/maybaytructhang/game_images")
            else -> "decrypt-xetang" to ("20260608_223506" to "D:/LUAN_VAN/MaHoa/runs/XeTang/game_images")
        }
        
        val slug = artifactConfig.first
        val runId = artifactConfig.second.first
        val wmDir = artifactConfig.second.second
        
        try {
            val response = com.example.network.LocalClient.decryptApiService.decrypt(
                slug = slug,
                request = com.example.network.DecryptRequest(runId = runId, wmDir = wmDir)
            )
            val answers = response.answers
            if (answers != null && answers.size >= 4) {
                jigsawPieces.clear()
                val base64s = answers.take(4).map { it.image }
                base64s.forEachIndexed { index, b64 ->
                    val bmp = base64ToBitmap(b64, artifactId, index)
                    jigsawPieces.add(
                        JigsawPiece(
                            id = index,
                            bitmap = bmp,
                            base64 = b64,
                            label = "MẢNH ẢNH #${index + 1}"
                        )
                    )
                }
                decryptedSecretCode = "SECRET_HEX_${runId.take(8)}"
            } else {
                throw Exception("Không đủ 4 ảnh từ server API!")
            }
        } catch (e: Exception) {
            decryptApiError = e.localizedMessage
            jigsawPieces.clear()
            val fallbackBase64s = mutableListOf<String>()
            for (i in 0..3) {
                val token = "FALLBACK_${artifactId}_PIECE_${i + 1}"
                fallbackBase64s.add(token)
                jigsawPieces.add(
                    JigsawPiece(
                        id = i,
                        bitmap = base64ToBitmap(token, artifactId, i),
                        base64 = token,
                        label = "MẢNH ẢNH #${i + 1}"
                    )
                )
            }
            decryptedSecretCode = "HERITAGE_DECRYPT_CODE_$artifactId"
        } finally {
            val list = (0..3).toList().shuffled()
            currentPiecePositions.clear()
            currentPiecePositions.addAll(list)
            isFetchingDecryptImages = false
        }
    }

    // Active Dialogs & Question details
    var showQuestionDialog by remember { mutableStateOf(false) }
    var showInQuestionChatbot by remember { mutableStateOf(false) }
    var showWrongAnswerChoiceModal by remember { mutableStateOf(false) }
    var activeCellIndex by remember { mutableStateOf(-1) }
    var activeCornerPosition by remember { mutableStateOf("") }
    var isCornerQuestion by remember { mutableStateOf(false) }

    // Firewall & Time limit count downs
    var activeFirewall by remember { mutableStateOf(false) }
    var firewallTimeLeft by remember { mutableStateOf(12) }

    // Question content state
    var currentQuestionText by remember { mutableStateOf(artifact?.question ?: "") }
    var currentOptions by remember(artifactId) { mutableStateOf((artifact?.options ?: emptyList()).shuffled()) }
    var currentCorrectAnswer by remember { mutableStateOf(artifact?.correctAnswer ?: "") }
    var isLoadingQuestion by remember { mutableStateOf(false) }
    val disabledOptions = remember { mutableStateListOf<String>() }
    var fiftyFiftyUseCount by remember(artifactId) { mutableStateOf(0) }
    val current5050Cost = (50 * (1.0 + 0.15 * fiftyFiftyUseCount)).toInt()

    fun useFiftyFiftyHelp() {
        if (disabledOptions.isNotEmpty()) return
        if (score < current5050Cost && state.totalScore < current5050Cost) {
            Toast.makeText(context, "❌ Không đủ điểm! Cần $current5050Cost điểm.", Toast.LENGTH_SHORT).show()
            return
        }
        if (score >= current5050Cost) {
            score -= current5050Cost
        } else {
            viewModel.skipStageWithPoints(artifactId, current5050Cost)
        }
        val wrong = currentOptions.filter { it != currentCorrectAnswer }
        disabledOptions.addAll(wrong.shuffled().take(2))
        fiftyFiftyUseCount++
        Toast.makeText(context, "💡 Đã dùng $current5050Cost điểm trợ giúp 50/50!", Toast.LENGTH_SHORT).show()
    }

    // Interactive selections
    var selectedAnswerOption by remember { mutableStateOf("") }
    var answerResultText by remember { mutableStateOf("") }
    var showExplanationText by remember { mutableStateOf(false) }
    var hasAnswerError by remember { mutableStateOf(false) }

    // Manual inputs
    var yearInputText by remember { mutableStateOf("") }
    var codeInputText by remember { mutableStateOf("") }

    // General state routing: "playing", "victory", "gameover"
    var gameState by remember { mutableStateOf("playing") }

    // Dynamic guide rule info modal
    var showGuideRulesPopup by remember { mutableStateOf(false) }

    // Continuous Cryptographic Glyph Shuffling to simulate high-tech security decryption
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            val glyphs = listOf("🛡️", "✨", "💎", "🏆", "🏛️", "🚩", "🔭", "🔑", "🔍", "🛸", "🔬", "🔋")
            while (gameState == "playing") {
                delay(1500)
                cells.forEachIndexed { idx, cell ->
                    if (!cell.answered && !cell.revealed) {
                        cells[idx] = cell.copy(cryptoChar = glyphs.random())
                    }
                }
            }
        }
    }

    // Firewall timer countdown trigger (with warning audio beeps)
    LaunchedEffect(activeFirewall, firewallTimeLeft) {
        if (activeFirewall && firewallTimeLeft > 0) {
            delay(1000)
            firewallTimeLeft--
            if (firewallTimeLeft <= 4 && firewallTimeLeft > 0) {
                SoundSynth.playTone(950, 60)
            }
            if (firewallTimeLeft == 0) {
                activeFirewall = false
                playFailureSound()
                lives = (lives - 1).coerceAtLeast(0)
                score = (score - 10).coerceAtLeast(0)
                combo = 0
                Toast.makeText(context, "Quá thời gian quét! Hệ thống phòng thủ phong tỏa dữ liệu.", Toast.LENGTH_LONG).show()
                showQuestionDialog = false
                if (lives <= 0) gameState = "gameover"
            }
        }
    }

    // Verifies stage completion
    fun verifyWin() {
        val allCellsAnswered = cells.all { it.answered || it.isSkipped }
        val allCornersAnswered = corners.all { it.answered || it.isSkipped }
        if (allCellsAnswered && allCornersAnswered) {
            gameState = "victory"
            playSuccessSound()
            viewModel.completeGame(artifactId, score)
        }
    }

    // Bypasses the level instantly using the found Heritage Master Key
    fun useMasterKeyToPassLevel() {
        playSuccessSound()
        showMasterKeyDialog = false
        gameState = "victory"
        viewModel.completeGame(artifactId, score)
        val msg = "🔑 CHÌA KHÓA VẠN NĂNG KÍCH HOẠT: Thông quan toàn chặng!"
        viewModel.showTopNotification(msg, "success")
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    // Bypasses the level instantly by spending 100 points (from current stage score or total accumulated score)
    fun spendPointsToPassLevel() {
        val availablePoints = score + state.totalScore
        if (availablePoints >= 100) {
            playSuccessSound()
            if (score >= 100) {
                score -= 100
                viewModel.completeGame(artifactId, score)
            } else {
                val remainder = 100 - score
                score = 0
                viewModel.skipStageWithPoints(artifactId, remainder)
            }
            gameState = "victory"
            val msg = "💸 ĐÃ ĐỔI 100 ĐIỂM: Thông quan ải phục chế thành công!"
            viewModel.showTopNotification(msg, "success")
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        } else {
            playFailureSound()
            val msg = "Không đủ điểm! Cần tối thiểu 100 điểm (từ ải này hoặc điểm tích lũy) để đổi."
            viewModel.showTopNotification(msg, "error")
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Spends 100 points to highlight/locate 1 cell containing an artifact image piece
    fun locateImageTile() {
        val unrevealedPieceCells = pieceCellMapping.filter { idx ->
            idx !in locatedImageCells && !cells[idx].answered && !cells[idx].revealed && !cells[idx].isSkipped
        }

        if (unrevealedPieceCells.isEmpty()) {
            val allRevealed = pieceCellMapping.all { idx -> cells[idx].answered || cells[idx].revealed || cells[idx].isSkipped }
            if (allRevealed) {
                Toast.makeText(context, "Tất cả các ô chứa mảnh ảnh trong chặng này đều đã được mở!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Ô chứa mảnh ảnh còn lại đã được định vị!", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val availablePoints = score + state.totalScore
        if (availablePoints < 100) {
            playFailureSound()
            val msg = "Không đủ điểm! Cần 100 điểm (từ ải này hoặc điểm tích lũy) để định vị ô có ảnh."
            viewModel.showTopNotification(msg, "error")
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            return
        }

        val success = if (score >= 100) {
            score -= 100
            true
        } else {
            val remainder = 100 - score
            score = 0
            viewModel.deductPointsForHint(remainder)
        }

        if (success) {
            playRadarSound()
            val targetIdx = unrevealedPieceCells.random()
            locatedImageCells.add(targetIdx)
            scoreGainAnimationText = "-100 ĐIỂM: ĐỊNH VỊ Ô CÓ ẢNH"
            scoreGainAnimActive = true
            val msg = "🎯 ĐÃ ĐỊNH VỊ: Ô số ${targetIdx + 1} đang phát sáng chứa mảnh ảnh di sản!"
            viewModel.showTopNotification(msg, "success")
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Loads trivia questions from API or offline fallback
    fun loadQuestion(id: Int, isCorner: Boolean, position: String = "") {
        isLoadingQuestion = true
        showQuestionDialog = true
        isCornerQuestion = isCorner
        activeCornerPosition = position
        activeCellIndex = if (isCorner) -1 else id
        selectedAnswerOption = ""
        answerResultText = ""
        showExplanationText = false
        hasAnswerError = false
        yearInputText = ""
        codeInputText = ""
        disabledOptions.clear()

        val bankItem = com.example.data.QuestionBank.getQuestionForArtifact(artifactId, id, isCorner, position)
        val offlineQuestion = bankItem.question
        val offlineOptions = bankItem.options
        val offlineAnswer = bankItem.correctAnswer

        coroutineScope.launch {
            try {
                val slug = when (artifactId) {
                    1 -> "bom"
                    2 -> "bevadantenlua"
                    3 -> "ghexuongthuyen"
                    4 -> "luhambimat"
                    5 -> "maycantol"
                    6 -> "mayinpedal"
                    7 -> "moneotau"
                    8 -> "phao"
                    9 -> "sungthancong"
                    10 -> "trucmaybayb52"
                    11 -> "tautuantieupcf"
                    12 -> "xebocthep"
                    13 -> "xepeugeot"
                    14 -> "xetang"
                    15 -> "maybaytructhang"
                    else -> "bom"
                }

                val response = com.example.network.LocalClient.questionApiService.getRandomQuestion(
                    com.example.network.LocalClient.getQuestionUrl(slug)
                )
                val apiQ = response.question
                if (apiQ != null && !apiQ.question.isBlank() && apiQ.question != "null") {
                    currentQuestionText = apiQ.question
                    if (!apiQ.options.isNullOrEmpty()) {
                        currentOptions = apiQ.options.shuffled()
                    } else {
                        currentOptions = offlineOptions.shuffled()
                    }
                    currentCorrectAnswer = apiQ.answer
                } else {
                    currentQuestionText = offlineQuestion
                    currentOptions = offlineOptions.shuffled()
                    currentCorrectAnswer = offlineAnswer
                }
            } catch (e: Exception) {
                currentQuestionText = offlineQuestion
                currentOptions = offlineOptions.shuffled()
                currentCorrectAnswer = offlineAnswer
            } finally {
                isLoadingQuestion = false
            }
        }
    }

    // Handles clicking cell in 3x3 matrix
    fun handleCellClick(index: Int) {
        val cell = cells[index]
        if (cell.answered || cell.isSkipped || gameState != "playing") return
        playClickSound()

        // Reveal the cell
        cells[index] = cell.copy(revealed = true)

        // Check if Master Key is found inside this cell!
        if (cell.hasMasterKey && !hasObtainedMasterKey) {
            playKeyFoundSound()
            hasObtainedMasterKey = true
            showMasterKeyDialog = true // Trigger special key dialog shortcut!
            return
        }

        // Check for Buff (Perfect artifacts)
        if (cell.isBuff) {
            playSuccessSound()
            cells[index] = cells[index].copy(answered = true, revealed = true)
            if (lives < 3) lives++
            score += 50
            scoreGainAnimationText = "+50 CỔ VẬT NGUYÊN BẢN"
            scoreGainAnimActive = true
            Toast.makeText(context, "✨ CỔ VẬT NGUYÊN BẢN: +1 Tim phục hồi và 50 điểm khảo cổ!", Toast.LENGTH_LONG).show()
            verifyWin()
            return
        }

        // Check for Traps
        if (cell.isTrap) {
            activeFirewall = true
            firewallTimeLeft = 12
            Toast.makeText(context, "⚠️ CẢNH BÁO BẪY: Kích hoạt Firewall bảo mật. Trả lời gấp trong 12 giây!", Toast.LENGTH_LONG).show()
        } else {
            activeFirewall = false
        }

        loadQuestion(index, isCorner = false)
    }

    // Handles clicking corner locks
    fun handleCornerClick(corner: CornerLock) {
        if (corner.answered || corner.isSkipped || gameState != "playing") return
        playClickSound()
        loadQuestion(-1, isCorner = true, position = corner.position)
    }

    // Evaluates answer submissions
    fun checkAnswer(option: String) {
        val isCorrect = option.trim() == currentCorrectAnswer.trim()

        if (isCorrect) {
            playSuccessSound()
            activeFirewall = false
            combo++
            var points = 30
            if (combo > 1) points += combo * 5
            if (coreSuccess) points *= 2 // CNN bonus

            score += points
            scoreGainAnimationText = "+$points ĐIỂM TRI THỨC"
            scoreGainAnimActive = true
            showExplanationText = true
            answerResultText = "CHÍNH XÁC! Bạn đã thu hồi thành công thông tin (+ $points điểm tri thức)."

            if (isCornerQuestion) {
                val cIdx = corners.indexOfFirst { it.position == activeCornerPosition }
                if (cIdx != -1) {
                    corners[cIdx] = corners[cIdx].copy(answered = true)
                }
            } else {
                if (activeCellIndex != -1) {
                    cells[activeCellIndex] = cells[activeCellIndex].copy(answered = true)
                    if (score > 0 && score % 30 == 0) {
                        keys++
                        Toast.makeText(context, "Thăng cấp khảo cổ: Nhận được 1 Thẻ Bản Đồ!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            coroutineScope.launch {
                delay(1600)
                showQuestionDialog = false // Hide question dialog first
                delay(200) // Brief pause after dialog disappears
                
                // Collect decrypted image piece ONLY after question dialog is closed!
                val pIdx = pieceCellMapping.indexOf(activeCellIndex)
                if (pIdx >= 0) {
                    val collectedPieceB64 = jigsawPieces.getOrNull(pIdx)?.base64 ?: "MẢNH_ẢNH_DI_SẢN_${artifactId}_${pIdx + 1}"
                    viewModel.collectImage(artifactId, collectedPieceB64)
                }
                
                verifyWin()
            }
        } else {
            playFailureSound()
            hasAnswerError = true
            combo = 0
            lives = (lives - 1).coerceAtLeast(0)
            score = (score - 10).coerceAtLeast(0)
            answerResultText = "SAI LỆCH DỮ LIỆU DI SẢN: Bị trừ 10 điểm khảo cổ và 1 Tim!"

            if (lives <= 0) {
                gameState = "gameover"
                coroutineScope.launch {
                    delay(1500)
                    showQuestionDialog = false
                }
            } else {
                showWrongAnswerChoiceModal = true
            }
        }
    }

    // Tactical Radar Scan: Reveal all traps/buff locations permanently
    fun triggerRadarScan() {
        if (keys <= 0 || gameState != "playing") return
        playRadarSound()
        keys--
        cells.forEachIndexed { i, cell ->
            cells[i] = cell.copy(scanned = true)
        }
        val msg = "🛰️ QUÉT RADAR VỆ TINH: Định vị toàn bộ Trap và Buff hoàn hảo!"
        viewModel.showTopNotification(msg, "info")
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    // X-Ray Vision: Reveal entire matrix structure for 4 seconds
    fun activateXrayVision() {
        if (keys <= 0 || gameState != "playing") return
        playRadarSound()
        keys--
        xrayActive = true
        val msg = "🔍 THẤU KÍNH X-RAY KÍCH HOẠT: Ma trận phục chế hiển thị trong 4 giây!"
        viewModel.showTopNotification(msg, "info")
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        coroutineScope.launch {
            delay(4000)
            xrayActive = false
        }
    }

    // 50/50 hint selection (eliminate 2 wrong options)
    fun useKeyToEliminate() {
        if (keys <= 0 || !showQuestionDialog || currentOptions.size <= 2) return
        playRadarSound()
        keys--
        val wrongOptions = currentOptions.filter { it != currentCorrectAnswer }
        val toEliminate = wrongOptions.shuffled().take(2)
        disabledOptions.addAll(toEliminate)
        val msg = "💡 THẺ KHẢO CỔ: Đã lọc bỏ 2 đáp án sai lệch!"
        viewModel.showTopNotification(msg, "success")
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    // Camera action launcher for ML classifier
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                coreImageUri = uri
                coreArtifactUploaded = true
                isVerifyingCore = true

                coroutineScope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val tempFile = File.createTempFile("core_upload", ".jpg", context.cacheDir)
                        val outputStream = FileOutputStream(tempFile)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()

                        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                        val res = com.example.network.LocalClient.predictApiService.predict(body)
                        val responseString = res.string()

                        val jsonObject = org.json.JSONObject(responseString)
                        val resultsArray = jsonObject.optJSONArray("results")
                        val firstResult = resultsArray?.optJSONObject(0)
                        val detectedId = firstResult?.opt("cnn_id")?.toString()?.toIntOrNull()

                        if (detectedId == artifactId) {
                            coreSuccess = true
                            score *= 2
                            playSuccessSound()
                            Toast.makeText(context, "✅ XÁC THỰC LÕI THÀNH CÔNG: Nhân đôi toàn bộ điểm chặng!", Toast.LENGTH_LONG).show()
                        } else {
                            coreSuccess = false
                            playFailureSound()
                            Toast.makeText(context, "Ảnh không trùng khớp cổ vật hiện tại. Vui lòng chụp rõ góc cạnh hơn!", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        coreSuccess = false
                        playFailureSound()
                        Toast.makeText(context, "Lỗi kết nối máy chủ nhận dạng. Thử lại sau!", Toast.LENGTH_LONG).show()
                    } finally {
                        isVerifyingCore = false
                    }
                }
            }
        }
    )

    if (artifact == null) {
        Box(modifier = Modifier.fillMaxSize().background(darkBackground), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy thông tin cổ vật chặng này!", color = Color.White)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ẢI PHỤC CHẾ: ${artifact.name.uppercase()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showBackConfirmDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = goldColor)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.deferStage(artifactId)
                            viewModel.navigateTo(AppScreen.Dashboard)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706), contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PASS ẢI (ĐỂ SAU)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showGuideRulesPopup = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Hướng dẫn", tint = neonCyanColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF07192C))
            )
        },
        containerColor = darkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Brush.verticalGradient(listOf(Color(0xFF07192C), Color(0xFF02070F))))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🏛️ 3D MUSEUM STAGE HERO BANNER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF06182E)),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(goldColor, neonCyanColor))),
                shape = RoundedCornerShape(16.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // 3D Museum Seal Emblem
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF030D1A))
                                .border(1.2.dp, goldColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_museum_seal_3d),
                                contentDescription = "3D Museum Seal",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = goldColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.8.dp, goldColor)
                                ) {
                                    Text(
                                        text = "ẢI CỔ VẬT #${artifactId}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = goldColor,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = neonCyanColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.8.dp, neonCyanColor)
                                ) {
                                    Text(
                                        text = "BẢO TÀNG 3D",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = neonCyanColor,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = artifact.name.uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Mã số di sản: ARTIFACT-3D-0${artifactId}",
                                fontSize = 10.sp,
                                color = Color(0xFFBACDDF),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 3D Radar Badge Icon
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_radar_badge_3d),
                        contentDescription = "3D Radar Badge",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, neonCyanColor, CircleShape)
                    )
                }
            }

            // Compact Blue Stats HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0F2B48), Color(0xFF081C30))))
                    .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Stars, contentDescription = "Tổng điểm", tint = Color(0xFFFFD700), modifier = Modifier.size(15.dp))
                    Text("Tổng: ${state.totalScore}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F0FF))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Điểm chặng", tint = Color(0xFFFFD700), modifier = Modifier.size(15.dp))
                    Text("Chặng: $score", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = "Máu", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                    Text("Máu: $lives/3", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // AI Double Points Bonus Banner (Feature 8)
            if (coreSuccess) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x2200E5FF)),
                    border = BorderStroke(1.dp, neonCyanColor.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "AI Bonus",
                            tint = neonCyanColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "X2 AI BONUS: GẤP ĐÔI ĐIỂM SỐ MỌI CÂU HỎI CHÍNH XÁC!",
                            color = neonCyanColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // F1: Restoration Completeness Progress Bar
            val totalItems = cells.size + corners.size
            val totalAnswered = cells.count { it.answered } + corners.count { it.answered }
            val progressPercent = if (totalItems > 0) (totalAnswered * 100) / totalItems else 0

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2246)),
                border = BorderStroke(1.dp, goldColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (xrayActive) "🧬 QUÉT LÕI X-RAY HOẠT ĐỘNG..." else "TIẾN TRÌNH PHỤC CHẾ DI SẢN SỐ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (xrayActive) neonCyanColor else goldColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$progressPercent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = goldColor,
                        trackColor = Color(0xFF041225)
                    )
                }
            }

            // F2: Hardware GPS Live Warning Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isWithinRange) Color(0xFF0A311D) else Color(0xFF421015)),
                border = BorderStroke(1.2.dp, if (isWithinRange) Color(0xFF4CAF50) else alertRedColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = if (isWithinRange) Icons.Default.LocationOn else Icons.Default.LocationSearching,
                            contentDescription = "GPS Status",
                            tint = if (isWithinRange) Color(0xFF81C784) else alertRedColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isWithinRange) "HỆ THỐNG ĐỊNH VỊ: ĐÃ LIÊN KẾT TRẠM" else "ĐỊNH VỊ: NGOÀI VÙNG PHỦ SÓNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isWithinRange) Color(0xFF81C784) else alertRedColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Vĩ độ: ${state.userLatitude} | Kinh độ: ${state.userLongitude}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFBACDDF)
                    )
                    Text(
                        text = "Khoảng cách đến cổ vật thực tế: ${String.format(Locale.US, "%.1f", distance)}m (Yêu cầu: <=50m).",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!isWithinRange) {
                        Text(
                            text = "⚠️ Bạn đang ở ngoài phạm vi hoạt động. Vui lòng di chuyển tới vị trí thực tế của hiện vật bảo tàng (cần <= 50m) để kích hoạt hệ thống giải mã.",
                            fontSize = 11.sp,
                            color = alertRedColor,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 🌌 5 SURPRISE MISSIONS SECTION (NGẪU NHIÊN 5 THỨ)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF041225)),
                border = BorderStroke(1.5.dp, neonCyanColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = neonCyanColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "5 NHIỆM VỤ NGẪU NHIÊN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = neonCyanColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        // Show completion progress
                        val compCount = activeSurpriseMissions.count { it.isCompleted }
                        Text(
                            text = "ĐẠT: $compCount/5",
                            color = if (compCount == 5) Color.Green else goldColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    Text(
                        text = "Mỗi chặng khảo cổ sẽ tự động bốc ngẫu nhiên 5 trong số 15 tính năng mật học cao cấp để đem lại sự bất ngờ và thú vị tối đa.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    
                    activeSurpriseMissions.forEachIndexed { index, mission ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mission.isCompleted) Color(0x1500FF00) else Color(0xFF07192C))
                                .clickable {
                                    playClickSound()
                                    selectedMissionIdForDialog = mission.id
                                }
                                .border(
                                    1.dp,
                                    if (mission.isCompleted) Color.Green.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (mission.isCompleted) Color.Green else neonCyanColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        text = mission.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = mission.description,
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = mission.statusText,
                                    color = if (mission.isCompleted) Color.Green else goldColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Icon(
                                    imageVector = if (mission.isCompleted) Icons.Default.CheckCircle else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (mission.isCompleted) Color.Green else goldColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 🔍 SEARCH BANNER NOTE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, neonCyanColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = goldColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tìm kiếm 4 mảnh ghép trong 9 ô câu hỏi bên dưới",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = neonCyanColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // F3: Main 3x3 Restoration Matrix Frame (Grid & Corner Locks)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF031022), shape = RoundedCornerShape(16.dp))
                    .border(1.5.dp, goldColor.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Render 4 corner protection nodes
                    corners.forEach { corner ->
                        val alignment = when (corner.position) {
                            "top-left" -> Alignment.TopStart
                            "top-right" -> Alignment.TopEnd
                            "bottom-left" -> Alignment.BottomStart
                            else -> Alignment.BottomEnd
                        }
                        Box(
                            modifier = Modifier
                                .align(alignment)
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        corner.isSkipped -> Color(0xFF3F1212)
                                        corner.answered -> emeraldColor
                                        else -> Color(0xFF1E1407)
                                    }
                                )
                                .border(
                                    1.2.dp,
                                    when {
                                        corner.isSkipped -> alertRedColor
                                        corner.answered -> Color(0xFF4CAF50)
                                        else -> Color(0xFFE65100)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = !corner.answered && !corner.isSkipped) { handleCornerClick(corner) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    corner.isSkipped -> Icons.Default.Close
                                    corner.answered -> Icons.Default.LockOpen
                                    else -> Icons.Default.Lock
                                },
                                contentDescription = "Corner Lock",
                                tint = when {
                                    corner.isSkipped -> alertRedColor
                                    corner.answered -> Color.White
                                    else -> Color(0xFFFFA726)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Central matrix grid (3x3 layout containing 4 decrypted artifact images)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(0.85f)
                            .padding(6.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(9) { idx ->
                                val cell = cells[idx]
                                val showDetails = xrayActive || cell.scanned
                                val isLocated = locatedImageCells.contains(idx) && !cell.answered && !cell.revealed

                                val imgIndex = pieceCellMapping.indexOf(idx)
                                val piece = if (imgIndex >= 0) jigsawPieces.getOrNull(imgIndex) else null

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                cell.isSkipped -> Color(0xFF3F1212)
                                                cell.answered -> emeraldColor
                                                cell.revealed && cell.isTrap -> Color(0xFF4E161C)
                                                isLocated -> Color(0xFF0A3A52)
                                                else -> Color(0xFF061B35)
                                            }
                                        )
                                        .border(
                                            if (isLocated) 2.dp else 1.dp,
                                            when {
                                                cell.isSkipped -> alertRedColor
                                                cell.answered -> Color(0xFF4CAF50)
                                                cell.scanned && cell.isTrap -> alertRedColor
                                                cell.scanned && cell.isBuff -> Color(0xFF4CAF50)
                                                isLocated -> Color(0xFF00E5FF)
                                                else -> goldColor.copy(alpha = 0.4f)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = !cell.answered && !cell.isSkipped) { handleCellClick(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cell.isSkipped) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Skipped",
                                                tint = alertRedColor,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Text(
                                                text = "ĐÃ BỎ QUA",
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = alertRedColor
                                            )
                                        }
                                    } else if (piece != null && (cell.answered || cell.revealed || xrayActive)) {
                                        val pieceImageUrl = remember(piece.base64, artifactId) {
                                            com.example.network.LocalClient.resolveImageUrl(piece.base64 ?: "", artifactId)
                                        }

                                        if (pieceImageUrl != null) {
                                            coil.compose.SubcomposeAsyncImage(
                                                model = pieceImageUrl,
                                                contentDescription = piece.label,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                loading = {
                                                    if (piece.bitmap != null) {
                                                        androidx.compose.foundation.Image(
                                                            bitmap = piece.bitmap.asImageBitmap(),
                                                            contentDescription = piece.label,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                },
                                                error = {
                                                    if (piece.bitmap != null) {
                                                        androidx.compose.foundation.Image(
                                                            bitmap = piece.bitmap.asImageBitmap(),
                                                            contentDescription = piece.label,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                            )
                                        } else if (piece.bitmap != null) {
                                            androidx.compose.foundation.Image(
                                                bitmap = piece.bitmap.asImageBitmap(),
                                                contentDescription = piece.label,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    } else if (cell.answered) {
                                        Icon(Icons.Default.Check, contentDescription = "Succeeded", tint = Color.White)
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (isLocated) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = "Located Image Tile",
                                                    tint = Color(0xFF00E5FF),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Text(
                                                    text = "CÓ ẢNH",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFF00E5FF)
                                                )
                                            } else if (showDetails) {
                                                Icon(
                                                    imageVector = when {
                                                        cell.isTrap -> Icons.Default.Warning
                                                        cell.isBuff -> Icons.Default.CardGiftcard
                                                        else -> Icons.Default.Help
                                                    },
                                                    contentDescription = "Scanned",
                                                    tint = when {
                                                        cell.isTrap -> alertRedColor
                                                        cell.isBuff -> Color(0xFF81C784)
                                                        else -> goldColor
                                                    },
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = if (cell.isTrap) "TRAP" else if (cell.isBuff) "BUFF" else "?",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            } else {
                                                Text(
                                                    text = cell.cryptoChar,
                                                    fontSize = 18.sp
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

            // F4: CNN Camera Classifier Verification Slot
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF041225)),
                border = BorderStroke(1.2.dp, if (coreSuccess) Color(0xFF4CAF50) else goldColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF030D1B))
                            .border(1.2.dp, goldColor, shape = RoundedCornerShape(12.dp))
                            .clickable {
                                if (!isVerifyingCore) {
                                    playClickSound()
                                    showCoreImageSourceDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVerifyingCore) {
                            CircularProgressIndicator(color = goldColor, modifier = Modifier.size(28.dp))
                        } else if (coreImageUri != null) {
                            AsyncImage(
                                model = coreImageUri,
                                contentDescription = "Core Preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = goldColor, modifier = Modifier.size(28.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "XÁC THỰC LÕI ẢNH CHỤP (CNN X2 ĐIỂM)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = goldColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (coreSuccess) "ĐÃ XÁC THỰC: NHÂN ĐÔI (X2) ĐIỂM SỐ TRONG CHẶNG!" else "Tải lên ảnh cổ vật tương ứng ngoài đời để nhận ngay gấp đôi điểm tri thức.",
                            fontSize = 11.sp,
                            color = if (coreSuccess) Color(0xFF81C784) else Color(0xFFBACDDF)
                        )

                    }
                }
            }

            // Locate image tile hint card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2239)),
                border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎯 ĐỔI 100 ĐIỂM ĐỊNH VỊ Ô CÓ ẢNH",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "Phát sáng 1 ô đang giấu mảnh ảnh di sản để bạn dễ tìm",
                            fontSize = 10.5.sp,
                            color = Color(0xFFBACDDF)
                        )
                    }
                    Button(
                        onClick = { locateImageTile() },
                        enabled = (score + state.totalScore) >= 100 && gameState == "playing",
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), disabledContainerColor = Color(0xFF1E293B), contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("ĐỊNH VỊ ẢNH", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // F5: Point exchange bypass card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                border = BorderStroke(1.2.dp, goldColor.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ ĐỔI 100 ĐIỂM BỎ QUA CHẶNG",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldColor
                        )
                        Text(
                            text = "Sử dụng 100 điểm tích lũy để hoàn thành ngay chặng này",
                            fontSize = 10.5.sp,
                            color = Color(0xFFBACDDF)
                        )
                    }
                    Button(
                        onClick = { spendPointsToPassLevel() },
                        enabled = (score + state.totalScore) >= 100 && gameState == "playing",
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = goldColor, disabledContainerColor = Color(0xFF1E293B), contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("ĐỔI QUA ẢI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // F6: Mini stats HUD toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lifes count
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { idx ->
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Life",
                            tint = if (idx < lives) alertRedColor else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Accumulated game score
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1407))) {
                    Text(
                        text = "🏆 ĐIỂM CHẶNG: $score",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Combo indicators
                if (combo > 1) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2C1D))) {
                        Text(
                            text = "COMBO x$combo",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF81C784),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // F7: Equipment Tools bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { triggerRadarScan() },
                    enabled = keys > 0 && gameState == "playing",
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F365C), disabledContainerColor = Color.DarkGray)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("THẤU KÍNH SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = goldColor)
                        Text("🔑 Thẻ: $keys", fontSize = 9.sp, color = Color.White)
                    }
                }

                Button(
                    onClick = { activateXrayVision() },
                    enabled = keys > 0 && gameState == "playing",
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F365C), disabledContainerColor = Color.DarkGray)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KÍNH LÚP X-RAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = goldColor)
                        Text("🔑 Thẻ: $keys", fontSize = 9.sp, color = Color.White)
                    }
                }
            }

            // F8: Map progress nodes router list
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF05111F)),
                border = BorderStroke(1.2.dp, goldColor.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "📍 SƠ ĐỒ LỘ TRÌNH PHÒNG TRƯNG BÀY HỒNG BÀNG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        fontFamily = FontFamily.Monospace
                    )

                    val activeNodeId = viewModel.artifactIdToNodeId(artifactId)
                    val lockedPath = state.lockedHamiltonPath
                    val activePathIdx = state.currentPathIndex

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        lockedPath.forEachIndexed { idx, nodeId ->
                            val art = viewModel.getArtifactByNodeId(nodeId)
                            if (art != null && (idx == activePathIdx || idx == activePathIdx - 1 || idx == activePathIdx + 1)) {
                                val status = when {
                                    idx < activePathIdx -> "ĐÃ PHỤC CHẾ"
                                    idx == activePathIdx -> "ĐANG KHẢO CỔ SỐ HÓA"
                                    else -> "CHƯA KHAI THÁC"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (idx < activePathIdx) Icons.Default.CheckCircle else if (idx == activePathIdx) Icons.Default.DirectionsRun else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (idx < activePathIdx) Color(0xFF81C784) else if (idx == activePathIdx) goldColor else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = art.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (idx == activePathIdx) FontWeight.Bold else FontWeight.Normal,
                                            color = if (idx == activePathIdx) Color.White else Color.Gray
                                        )
                                    }
                                    Text(
                                        text = status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx < activePathIdx) Color(0xFF81C784) else if (idx == activePathIdx) goldColor else Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // F9: Direct Backwards navigation
            Button(
                onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = goldColor),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, goldColor)
            ) {
                Text("QUAY LẠI BẢN ĐỒ")
            }
        }
    }

    // F10: THESIS SPECIAL FEATURE - Heritage Master Key Unearthing Popup
    if (showMasterKeyDialog) {
        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF041225)),
                border = BorderStroke(2.dp, goldColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Master Key",
                        tint = goldColor,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "🔑 TÌM THẤY CHÌA KHÓA VẠN NĂNG!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = goldColor,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Bạn đã vô cùng may mắn khai quật thành công [Chìa Khóa Vạn Năng Di Sản] ẩn giấu dưới ma trận khảo cổ! Bạn có muốn kích hoạt chìa khóa để thông quan chặng này ngay lập tức không?",
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { useMasterKeyToPassLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("KÍCH HOẠT THÔNG QUAN", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { showMasterKeyDialog = false },
                        border = BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("ĐÓNG LẠI & TỰ GIẢI ĐÁP")
                    }
                }
            }
        }
    }

    // F11: Interactive Trivia Question dialog
    if (showQuestionDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .pointerInput(Unit) { detectTapGestures(onTap = { /* consume taps */ }) },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07192C)),
                border = BorderStroke(1.5.dp, if (activeFirewall) alertRedColor else goldColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCornerQuestion) "KHÓA PHÒNG THỦ GÓC" else "PHÂN MẢNH DI SẢN",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = goldColor,
                            fontFamily = FontFamily.Monospace
                        )
                        if (activeFirewall) {
                            Text(
                                text = "⏳ FIREWALL: ${firewallTimeLeft}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = alertRedColor,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (activeFirewall) {
                        LinearProgressIndicator(
                            progress = { firewallTimeLeft / 12f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = alertRedColor,
                            trackColor = Color(0xFF421015)
                        )
                        Text(
                            text = "PHÒNG THỦ KHẨN CẤP: Vui lòng giải mã gấp để tránh cạn kiệt năng lượng phục chế!",
                            fontSize = 11.sp,
                            color = alertRedColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isLoadingQuestion) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = goldColor)
                        }
                    } else {
                        Text(
                            text = currentQuestionText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 20.sp
                        )

                        if (currentOptions.size > 2) {
                            Button(
                                onClick = { useFiftyFiftyHelp() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B324D)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 38.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = goldColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GỢI Ý 50/50: Loại 2 đáp án sai (-${current5050Cost}đ)", fontSize = 11.sp, color = goldColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Answer options for all stages
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentOptions.forEach { option ->
                                val isEliminated = disabledOptions.contains(option)
                                val isSelected = selectedAnswerOption == option
                                Button(
                                    onClick = {
                                        selectedAnswerOption = option
                                        checkAnswer(option)
                                    },
                                    enabled = !isEliminated,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) goldColor else Color(0xFF041225),
                                        contentColor = if (isSelected) Color.Black else Color.White,
                                        disabledContainerColor = Color(0xFF02070F),
                                        disabledContentColor = Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                                    border = BorderStroke(1.dp, if (isEliminated) Color.Transparent else goldColor.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 17.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 5,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        if (answerResultText.isNotEmpty()) {
                            Text(
                                text = answerResultText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasAnswerError) alertRedColor else Color(0xFF81C784),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (showExplanationText) {
                            Text(
                                text = "💡 Góc Di Sản: " + artifact.funFact,
                                fontSize = 11.sp,
                                color = Color(0xFFC8E6C9),
                                modifier = Modifier
                                    .background(Color(0xFF0F2C1D), shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal when user answers incorrectly: Option to Try Again or Skip (mark X)
    if (showWrongAnswerChoiceModal) {
        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.5.dp, alertRedColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3F1212),
                        border = BorderStroke(1.dp, alertRedColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = null,
                            tint = alertRedColor,
                            modifier = Modifier.padding(10.dp).size(28.dp)
                        )
                    }

                    Text(
                        text = "❌ RẤT TIẾC, CÂU TRẢ LỜI CHƯA CHÍNH XÁC!",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Dữ liệu trả lời chưa phù hợp với hồ sơ di sản (-10 điểm, -1 Tim). Bạn muốn trả lời lại hay bỏ qua ô này?",
                        fontSize = 11.5.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showWrongAnswerChoiceModal = false
                                answerResultText = ""
                                hasAnswerError = false
                                selectedAnswerOption = ""
                                yearInputText = ""
                                codeInputText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("🔄 Trả lời lại", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F0FF))
                        }

                        Button(
                            onClick = {
                                showWrongAnswerChoiceModal = false
                                showQuestionDialog = false

                                if (isCornerQuestion) {
                                    val cIdx = corners.indexOfFirst { it.position == activeCornerPosition }
                                    if (cIdx != -1) {
                                        corners[cIdx] = corners[cIdx].copy(isSkipped = true, answered = true)
                                    }
                                } else if (activeCellIndex != -1) {
                                    cells[activeCellIndex] = cells[activeCellIndex].copy(isSkipped = true, answered = true)
                                }

                                viewModel.deferStage(artifactId)
                                Toast.makeText(context, "❌ Ô này đã bị đánh dấu X và khóa không thể thao tác!", Toast.LENGTH_SHORT).show()

                                verifyWin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                            border = BorderStroke(1.dp, alertRedColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("⏭️ Bỏ qua (Khóa X)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // F12: Guide rule list popup
    if (showGuideRulesPopup) {
        Dialog(onDismissRequest = { showGuideRulesPopup = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07192C)),
                border = BorderStroke(1.2.dp, goldColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📖 CẨM NĂNG PHÒNG TRƯNG BÀY HỒNG BÀNG",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Chào mừng nhà khảo cổ học! Dưới đây là 20 tính năng đỉnh cao hỗ trợ phục chế bảo vật quốc gia:\n\n" +
                                "- 🔑 **Chìa khóa vạn năng**: Ẩn giấu ngẫu nhiên dưới 9 ô phục chế, mở ra để thông quan ngay.\n" +
                                "- 💸 **Đổi điểm thông quan**: Sử dụng 100 điểm tổng thu tích lũy để vượt nhanh ải khó.\n" +
                                "- 🛰️ **Thấu kính Scan & Kính lúp X-Ray**: Gợi ý các ô bẫy nguy hiểm và bảo vật nguyên bản.\n" +
                                "- 🛡️ **Hệ thống cảnh báo GPS**: Xác thực vị trí khảo cổ thực tế bằng sóng vệ tinh điện thoại.\n" +
                                "- 📸 **Nhận dạng ảnh chụp AI**: Chụp cổ vật thật bằng mô hình CNN để nhân đôi điểm số.",
                        fontSize = 11.sp,
                        color = Color(0xFFBACDDF),
                        lineHeight = 16.sp
                    )
                    Button(
                        onClick = { showGuideRulesPopup = false },
                        colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("BẮT ĐẦU NGHIÊN CỨU", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCoreImageSourceDialog) {
        Dialog(onDismissRequest = { showCoreImageSourceDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.5.dp, neonCyanColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "VUI LÒNG CHỌN NGUỒN ẢNH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = neonCyanColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Chụp ảnh cổ vật trực tiếp bằng camera thiết bị hoặc tải tệp tin từ thư viện hình ảnh có sẵn:",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Button(
                        onClick = {
                            showCoreImageSourceDialog = false
                            coreCameraLauncher.launch(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chụp ảnh", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            showCoreImageSourceDialog = false
                            cameraLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tải ảnh", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { showCoreImageSourceDialog = false },
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

    // F13: End of Stage GameOver or Victory Overlay
    if (gameState == "victory") {
        var countdownSec by remember { mutableStateOf(5) }
        var autoAdvancing by remember { mutableStateOf(false) }
        val animScale = remember { androidx.compose.animation.core.Animatable(0.15f) }
        val animRotation = remember { androidx.compose.animation.core.Animatable(0f) }
        val animAlpha = remember { androidx.compose.animation.core.Animatable(0f) }

        LaunchedEffect(gameState) {
            SoundSynth.playTone(1200, 300)
            kotlinx.coroutines.coroutineScope {
                launch {
                    animAlpha.animateTo(1.0f, tween(600))
                }
                launch {
                    animScale.animateTo(1.0f, tween(1400, easing = FastOutSlowInEasing))
                }
                launch {
                    animRotation.animateTo(360f, tween(1800, easing = LinearOutSlowInEasing))
                }
            }

            for (sec in 5 downTo 1) {
                countdownSec = sec
                kotlinx.coroutines.delay(1000)
            }
            countdownSec = 0
            if (!autoAdvancing) {
                autoAdvancing = true
                viewModel.advanceNextStageAndNavigate()
            }
        }

        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF040D1A)),
                border = BorderStroke(2.5.dp, Brush.horizontalGradient(listOf(goldColor, neonCyanColor, goldColor))),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .graphicsLayer { alpha = animAlpha.value }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Radial Glowing Background Aura
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        neonCyanColor.copy(alpha = 0.45f),
                                        goldColor.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Zooming & 360-degree Rotating Neon Logo
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .graphicsLayer {
                                    scaleX = animScale.value
                                    scaleY = animScale.value
                                    rotationZ = animRotation.value
                                }
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFF071224))
                                .border(2.5.dp, neonCyanColor, RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_museum_logo_neon),
                                contentDescription = "Museum Victory Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        // Glowing Text
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "✨ CHÚC MỪNG BẠN! ✨",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = goldColor,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ĐÃ GIẢI MÃ VÀ PHỤC CHẾ THÀNH CÔNG!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neonCyanColor,
                                letterSpacing = 1.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Artifact Name Banner
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF091E3A)),
                            border = BorderStroke(1.dp, goldColor.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = artifact.name.uppercase(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }

                        // 5-second Progress Bar & Countdown
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { countdownSec / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = neonCyanColor,
                                trackColor = Color(0xFF0F2B48)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (countdownSec > 0) "Tự động chuyển chặng kế trong $countdownSec giây..." else "Đang chuyển chặng kế...",
                                fontSize = 11.5.sp,
                                color = Color(0xFFBACDDF),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Button to proceed immediately
                        Button(
                            onClick = {
                                if (!autoAdvancing) {
                                    autoAdvancing = true
                                    viewModel.advanceNextStageAndNavigate()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = emeraldColor, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.DoubleArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TIẾP TỤC NGAY (CHUYỂN ẢI 3D)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    } else if (gameState == "gameover") {
        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07192C)),
                border = BorderStroke(2.dp, alertRedColor),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentVeryDissatisfied,
                        contentDescription = "End state icon",
                        tint = alertRedColor,
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = "MẤT KẾT NỐI KHẢO CỔ!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = alertRedColor,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Năng lượng phục chế đã cạn kiệt. Vui lòng nạp lại và thử giải mã lại chặng này hoặc sử dụng chức năng ĐỔI ĐIỂM ở menu chính.",
                        fontSize = 12.sp,
                        color = Color(0xFFBACDDF),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { spendPointsToPassLevel() },
                        enabled = (score + state.totalScore) >= 100,
                        colors = ButtonDefaults.buttonColors(containerColor = neonCyanColor, disabledContainerColor = Color.DarkGray, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CỨU NẠN: ĐỔI 100 ĐIỂM QUA ẢI", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            score = 0
                            lives = 3
                            keys = 3
                            combo = 0
                            coreArtifactUploaded = false
                            coreSuccess = false
                            coreImageUri = null
                            hasObtainedMasterKey = false
                            gameState = "playing"
                            cells.forEachIndexed { idx, cell ->
                                cells[idx] = cell.copy(answered = false, revealed = false, scanned = false)
                            }
                            corners.forEachIndexed { idx, corner ->
                                corners[idx] = corner.copy(answered = false)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black)
                    ) {
                        Text("TẢI LẠI TRẠM KHẢO CỔ", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.Dashboard) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = goldColor),
                        border = BorderStroke(1.dp, goldColor)
                    ) {
                        Text("QUAY LẠI SƠ ĐỒ BẢN ĐỒ")
                    }
                }
            }
        }
    }


        // Feature 3: Stage Loading Overlay / Pre-game Briefing Screen
        if (showBriefingScreen) {
            Dialog(onDismissRequest = { }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF041225)),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(goldColor, neonCyanColor))),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 3D Museum Seal Badge
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF020914))
                                .border(1.5.dp, goldColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_museum_seal_3d),
                                contentDescription = "3D Museum Seal Emblem",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = "THIẾT LẬP PHIÊN PHỤC CHẾ BẢO TÀNG 3D",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = neonCyanColor,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ẢI CỔ VẬT: ${artifact.name.uppercase()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        Text(
                            text = "Tọa độ GIS: (${artifact.latitude}, ${artifact.longitude})\n\nHướng dẫn: Bạn cần giải mã tất cả các ô trong mạng lưới bằng cách trả lời đúng các câu hỏi tri thức lịch sử để tái cấu trúc di sản này.",
                            fontSize = 11.5.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Start,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                SoundSynth.playTone(1000, 150)
                                showBriefingScreen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = goldColor, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("BẮT ĐẦU PHỤC CHẾ (3D)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Feature 5: Confetti / Particle Animation Overlay
        if (gameState == "victory") {
            var tick by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(32)
                    tick++
                }
            }
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val numParticles = 30
                for (i in 0 until numParticles) {
                    val angle = (tick + i * 15) * 0.05f
                    val px = (size.width * (i.toFloat() / numParticles) + kotlin.math.sin(angle) * 30f) % size.width
                    val py = ((tick * 4f + i * 40f) % size.height)
                    drawCircle(
                        color = if (i % 2 == 0) goldColor else neonCyanColor,
                        radius = 6f + (i % 3) * 2f,
                        center = androidx.compose.ui.geometry.Offset(px, py),
                        alpha = 0.7f
                    )
                }
            }
        }

        // Feature 6: Score Gain Floating Popups
        if (scoreGainAnimActive && scoreGainAnimationText.isNotEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE0B1C33)),
                    border = BorderStroke(1.5.dp, goldColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = goldColor, modifier = Modifier.size(24.dp))
                        Text(
                            text = scoreGainAnimationText,
                            color = goldColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Feature 7: Stage-exit Confirmation Dialog
        if (showBackConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showBackConfirmDialog = false },
                title = {
                    Text(
                        "RỜI PHIÊN KHẢO CỔ?",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = alertRedColor
                    )
                },
                text = {
                    Text(
                        "Tiến trình khôi phục cổ vật chặng này sẽ bị thiết lập lại. Bạn có chắc chắn muốn thoát ra bản đồ chính không?",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showBackConfirmDialog = false
                            viewModel.dismissCardCollectAnimation()
                            viewModel.closeCartDialog()
                            if (gameState != "victory") {
                                viewModel.markStageIncomplete(artifactId)
                                Toast.makeText(context, "⚠️ Ải cổ vật #${artifactId} báo chưa hoàn thành!", Toast.LENGTH_SHORT).show()
                            }
                            viewModel.navigateTo(AppScreen.Dashboard)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = alertRedColor)
                    ) {
                        Text("Thoát", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackConfirmDialog = false }) {
                        Text("Ở lại", color = Color.White)
                    }
                },
                containerColor = Color(0xFF041225)
            )
        }

        // 1. GPS warning overlay (sliding/pulsing banner at the top)
        AnimatedVisibility(
            visible = distance > 40.0 && distance <= 50.0,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF5C3E0D)),
                border = BorderStroke(1.5.dp, Color(0xFFFFB300)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "blink")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Cảnh báo khoảng cách",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(24.dp).graphicsLayer(alpha = alpha)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚠️ CẢNH BÁO KHOẢNG CÁCH GẦN QUÁ GIỚI HẠN",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFFFFB300),
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Khoảng cách hiện tại là ${String.format(Locale.US, "%.1f", distance)}m. Vui lòng di chuyển quay lại trong phạm vi 40m để tránh bị khóa hệ thống!",
                            fontSize = 10.sp,
                            color = Color.White,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // 2. GPS dynamic lockout overlay
        AnimatedVisibility(
            visible = distance > 50.0,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE6020A15))
                    .pointerInput(Unit) {
                        detectTapGestures { /* Eat all gestures */ }
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF041225)),
                    border = BorderStroke(2.dp, alertRedColor),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(alertRedColor.copy(alpha = 0.15f), shape = CircleShape)
                                .border(2.dp, alertRedColor, CircleShape)
                                .graphicsLayer(scaleX = scale, scaleY = scale),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Khóa GPS",
                                tint = alertRedColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "HỆ THỐNG ĐÃ KHÓA 🔒",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = alertRedColor,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "TÍN HIỆU ĐỊNH VỊ BỊ NGẮT KẾT NỐI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(color = alertRedColor.copy(alpha = 0.3f), thickness = 1.dp)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bảo tàng phát hiện bạn cách hiện vật:",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .background(Color(0xFF1E0A0D), shape = RoundedCornerShape(8.dp))
                                    .border(1.dp, alertRedColor.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", distance)}m",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = alertRedColor,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "/ tối đa 50.0m",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Vui lòng di chuyển lại gần hiện vật [${artifact.name.uppercase()}] dưới 50m để tiếp tục giải mã khóa thông tin mật ẩn giấu dưới lớp ảnh.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.navigateTo(AppScreen.Dashboard)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("QUAY LẠI BẢN ĐỒ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Floating chat box overlay (floats on top of all dialogs & question screens)
        FloatingChatBox(
            artifactId = artifactId,
            artifactName = artifact.name,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 72.dp, end = 16.dp)
        )
    }
}

// ==========================================
// THESIS ENHANCEMENT: FLOATING DRAGGABLE CHAT BOX
// ==========================================
data class ChatMessage(
    val sender: String, // "bot" or "user"
    val text: String,
    val suggestions: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingChatBox(
    artifactId: Int,
    artifactName: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isChatOpen by remember { mutableStateOf(false) }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var chatInput by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }
    
    // Position state of the floating chat bubble / window
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Sound synth shortcuts
    fun playClickSound() = SoundSynth.playTone(880, 80)
    fun playReceiveSound() = SoundSynth.playTone(950, 100)

    val listState = rememberLazyListState()

    // Initialize messages
    LaunchedEffect(artifactId) {
        chatMessages.clear()
        chatMessages.add(
            ChatMessage(
                sender = "bot",
                text = "Xin chào! Tôi là thuyết minh viên quân đội tại Bảo tàng Quân khu 9. Bạn có thắc mắc gì về hiện vật [${artifactName}] không?",
                suggestions = listOf("Lịch sử của ${artifactName}", "Chiến công nổi bật", "Thông số kỹ thuật")
            )
        )
        // Reset position to default when artifact changes
        offsetX = 0f
        offsetY = 0f
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val neonCyanColor = Color(0xFF00E5FF)
    val goldColor = Color(0xFFF3C623)
    val darkBg = Color(0xFF0B1528)

    fun sendChatMessage(messageText: String) {
        if (messageText.isBlank() || isChatLoading) return
        
        chatMessages.add(ChatMessage(sender = "user", text = messageText))
        isChatLoading = true
        
        coroutineScope.launch {
            var answerText = ""
            var suggestionsList = emptyList<String>()
            try {
                val response = com.example.network.LocalClient.chatApiService.chat_2(
                    com.example.network.LocalChatRequest(message = messageText, label = artifactId)
                )
                val botAnswers = response.responses
                if (!botAnswers.isNullOrEmpty()) {
                    val responseData = botAnswers[0]
                    answerText = responseData.answer ?: ""
                    suggestionsList = responseData.suggestions ?: emptyList()
                }
            } catch (e: Exception) {
                // Fallback to Gemini
            }

            if (answerText.isBlank()) {
                val currentArtName = com.example.data.MuseumRepository.artifacts.find { it.id == artifactId }?.name ?: "Bảo tàng QK9"
                val sysPrompt = "Bạn là Trợ lý AI Hướng dẫn viên tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ). Đang giải đáp về cổ vật $currentArtName. YÊU CẦU: Trả lời ĐÚNG TRỌNG TÂM, KHÔNG dài dòng rườm rà. Nêu 2-3 ý chính dùng thẻ <strong>. Tối đa 3 câu."
                answerText = com.example.network.GeminiClient.getChatbotResponse(messageText, sysPrompt)
                suggestionsList = listOf("Ý nghĩa lịch sử?", "Giới thiệu Bảo tàng QK9?", "Địa chỉ & Giờ mở cửa?")
            }

            chatMessages.add(
                ChatMessage(
                    sender = "bot",
                    text = answerText,
                    suggestions = suggestionsList
                )
            )
            playReceiveSound()
            isChatLoading = false
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        if (isChatOpen) {
            // Drag-enabled expanded chat window card
            Card(
                colors = CardDefaults.cardColors(containerColor = darkBg),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, neonCyanColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .width(320.dp)
                    .height(420.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF040A15))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = goldColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "THUYẾT MINH VIÊN AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = neonCyanColor,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Hỏi đáp về: $artifactName",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                        
                        // Close/Minimize button
                        IconButton(
                            onClick = {
                                playClickSound()
                                isChatOpen = false
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloseFullscreen,
                                contentDescription = "Thu nhỏ",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Body: Message list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isUser = msg.sender == "user"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                // Message bubble
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 240.dp)
                                        .background(
                                            color = if (isUser) Color(0xFF0F3661) else Color(0xFF091424),
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 12.dp
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isUser) neonCyanColor.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 12.dp
                                            )
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        lineHeight = 15.sp
                                    )
                                }
                                
                                // Suggestions under bot messages
                                if (!isUser && msg.suggestions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        msg.suggestions.forEach { suggestion ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF0F1E36), shape = RoundedCornerShape(20.dp))
                                                    .border(0.8.dp, goldColor.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
                                                    .clickable {
                                                        playClickSound()
                                                        sendChatMessage(suggestion)
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = suggestion,
                                                    fontSize = 8.5.sp,
                                                    color = goldColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isChatLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = neonCyanColor,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI đang suy nghĩ...", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Input field & send button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF040A15))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Hỏi về hiện vật...", fontSize = 10.sp, color = Color.Gray) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = darkBg,
                                unfocusedContainerColor = darkBg,
                                focusedBorderColor = neonCyanColor,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                            )
                        )
                        
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    playClickSound()
                                    sendChatMessage(chatInput)
                                    chatInput = ""
                                }
                            },
                            enabled = chatInput.isNotBlank() && !isChatLoading,
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (chatInput.isNotBlank() && !isChatLoading) neonCyanColor else Color.DarkGray,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Gửi",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Drag-enabled minimized floating chat bubble
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(neonCyanColor, Color(0xFF0D5679))
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, goldColor, CircleShape)
                    .clickable {
                        playClickSound()
                        isChatOpen = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Mở hộp thoại hỏi đáp thuyết minh viên AI",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmbeddedQuestionChatBox(
    artifactId: Int,
    artifactName: String,
    onCloseChat: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var chatInput by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val neonCyanColor = Color(0xFF00E5FF)
    val darkBg = Color(0xFF030D1A)

    fun playReceiveSound() = SoundSynth.playTone(950, 100)

    LaunchedEffect(artifactId) {
        if (chatMessages.isEmpty()) {
            chatMessages.add(
                ChatMessage(
                    sender = "bot",
                    text = "Xin chào! Tôi là Trợ lý AI thuyết minh. Bạn có thể hỏi bất kỳ thắc mắc nào liên quan đến hiện vật [$artifactName] hoặc nhờ gợi ý trả lời!",
                    suggestions = listOf("Gợi ý cho câu hỏi này", "Lịch sử $artifactName", "Thông số kỹ thuật")
                )
            )
        }
    }

    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun sendChatMessage(messageText: String) {
        if (messageText.isBlank() || isChatLoading) return
        chatMessages.add(ChatMessage(sender = "user", text = messageText))
        isChatLoading = true

        coroutineScope.launch {
            var answerText = ""
            var suggestionsList = emptyList<String>()
            try {
                val response = com.example.network.LocalClient.chatApiService.chat_2(
                    com.example.network.LocalChatRequest(message = messageText, label = artifactId)
                )
                val botAnswers = response.responses
                if (!botAnswers.isNullOrEmpty()) {
                    val responseData = botAnswers[0]
                    answerText = responseData.answer ?: ""
                    suggestionsList = responseData.suggestions ?: emptyList()
                }
            } catch (e: Exception) {
                // Fallback to Gemini
            }

            if (answerText.isBlank()) {
                val currentArtName = com.example.data.MuseumRepository.artifacts.find { it.id == artifactId }?.name ?: "Bảo tàng QK9"
                val sysPrompt = "Bạn là Trợ lý AI Hướng dẫn viên tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ). Đang giải đáp về cổ vật $currentArtName. YÊU CẦU: Trả lời ĐÚNG TRỌNG TÂM, KHÔNG dài dòng. Dùng thẻ <strong> cho các điểm chính. Tối đa 3 câu."
                answerText = com.example.network.GeminiClient.getChatbotResponse(messageText, sysPrompt)
                suggestionsList = listOf("Ý nghĩa lịch sử QK9?", "Địa chỉ & Giờ mở cửa?", "Các hiện vật tiêu biểu?")
            }

            chatMessages.add(
                ChatMessage(
                    sender = "bot",
                    text = answerText,
                    suggestions = suggestionsList
                )
            )
            playReceiveSound()
            isChatLoading = false
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = darkBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, neonCyanColor),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = neonCyanColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🤖 Trợ lý AI - Ải $artifactName",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onCloseChat,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("✖ Thoát Chat", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF020914), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages) { msg ->
                    val isBot = msg.sender == "bot"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isBot) Alignment.Start else Alignment.End
                    ) {
                        Surface(
                            color = if (isBot) Color(0xFF0F2642) else Color(0xFF1E3A8A),
                            shape = RoundedCornerShape(
                                topStart = 10.dp,
                                topEnd = 10.dp,
                                bottomStart = if (isBot) 2.dp else 10.dp,
                                bottomEnd = if (isBot) 10.dp else 2.dp
                            ),
                            border = BorderStroke(0.5.dp, if (isBot) neonCyanColor.copy(alpha = 0.5f) else Color.Transparent)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(6.dp),
                                lineHeight = 15.sp
                            )
                        }

                        if (isBot && msg.suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                msg.suggestions.forEach { suggestion ->
                                    SuggestionChip(
                                        onClick = { sendChatMessage(suggestion) },
                                        label = { Text(suggestion, fontSize = 9.sp, color = neonCyanColor) },
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = neonCyanColor.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = neonCyanColor, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đang phân tích...", fontSize = 9.5.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Hỏi AI...", fontSize = 10.5.sp, color = Color.Gray) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyanColor,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedContainerColor = Color(0xFF020914),
                        unfocusedContainerColor = Color(0xFF020914)
                    ),
                    modifier = Modifier.weight(1f).height(40.dp)
                )

                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            sendChatMessage(chatInput)
                            chatInput = ""
                        }
                    },
                    enabled = chatInput.isNotBlank() && !isChatLoading,
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (chatInput.isNotBlank()) neonCyanColor else Color(0xFF1E293B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        tint = if (chatInput.isNotBlank()) Color.Black else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
