package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Artifact
import com.example.data.MuseumRepository
import com.example.network.GeminiClient
import com.example.network.LocalChatRequest
import com.example.network.LocalClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

sealed interface AppScreen {
    object Welcome : AppScreen
    object MuseumInfo : AppScreen
    object Login : AppScreen
    object Dashboard : AppScreen
    data class Game(val artifactId: Int) : AppScreen
}

data class ChatMessage(
    val sender: MessageSender,
    val text: String,
    val image: String? = null,
    val suggestions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

data class StationHistory(
    val timeSpent: Int,
    val clearedAt: String,
    val scoreSpent: Int = 0
)

data class EndGameReceipt(
    val userName: String = "",
    val ticketCode: String = "",
    val userEmail: String = "",
    val totalScore: Int = 0,
    val playTimeFormatted: String = "",
    val totalVisited: Int = 0,
    val totalSkipped: Int = 0,
    val thankYouMessage: String = "",
    val isAutoTimeout: Boolean = false
)

data class MuseumUiState(
    val currentScreen: AppScreen = AppScreen.Welcome,
    val isLoggedIn: Boolean = false,
    val loggedInUser: String = "",
    val userEmail: String = "khach@baotangqk9.vn",
    val ticketCode: String = "VIP-2026",
    val sessionStartTimeMs: Long = 0L,
    val sessionElapsedSeconds: Int = 0,
    val showEndGameModal: Boolean = false,
    val endGameReceipt: EndGameReceipt? = null,
    val artifacts: List<Artifact> = MuseumRepository.artifacts,
    val completedArtifactIds: Set<Int> = emptySet(),
    val deferredArtifactIds: Set<Int> = emptySet(), // Operation QK9 - "Để sau" list
    val discoveredArtifactIds: Set<Int> = emptySet(), // Operation QK9 - Discovered list
    val userLatitude: Double = 10.030194,
    val userLongitude: Double = 105.771444,
    val mockLocationEnabled: Boolean = false,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            MessageSender.AI,
            "Chào mừng bạn đến với hệ thống thử thách. Vui lòng tải ảnh hiện vật lên để kích hoạt định vị điểm khởi hành và lập lộ trình!"
        )
    ),
    val isChatLoading: Boolean = false,
    val currentScore: Int = 0,
    val activeTab: Int = 0,

    // Vue exact game states
    val gameLocked: Boolean = false,
    val lockedHamiltonPath: List<Int> = emptyList(), // Store as Node IDs (0-14)
    val currentPathIndex: Int = 0,
    val currentArtifactId: Int? = null, // Active artifact ID (1-15)
    val currentArtifactName: String = "",
    val currentStageCleared: Boolean = false,
    val timeLeft: Int = 300,
    val timeOutReached: Boolean = false,
    val stationHistoryLog: Map<Int, StationHistory> = emptyMap(), // Map of NodeId (0-14) -> StationHistory
    val totalScore: Int = 0,
    val totalElapsedTime: Int = 0,
    val uploading: Boolean = false,
    val isMinimized: Boolean = false,
    val showRankRules: Boolean = false,
    val showRouteModal: Boolean = false,
    val showLeaderboardModal: Boolean = false,
    val showSummaryModal: Boolean = false,
    val showHistoryLog: Boolean = false,
    val secondsElapsedInStage: Int = 0,
    val isSpeaking: Boolean = false,
    val activeSpeechText: String = "",
    val chatSelectedImageSrc: String? = null,
    val showGpsBlockWarning: Boolean = false,
    val blockedArtifactName: String = "",
    val blockedArtifactDistance: Double = 0.0,
    val showImageSourceDialog: Boolean = false,
    val sourceDialogTarget: String = "start",
    // Upgraded Group and Guess features
    val secretArtifactId: Int = 8, // Fixed to Súng thần công (index 8, id = 9)
    val clueStageIndices: List<Int> = emptyList(),
    val collectedClues: Map<Int, String> = emptyMap(),
    val secretGuessCorrect: Boolean = false,
    val secretGuessMultiplier: Int = 1,
    val secretGuessScoreBonus: Int = 0,
    val collectedLettersByGroup: Map<Int, String> = mapOf(1 to "", 2 to "", 3 to "", 4 to "", 5 to ""),
    val secretGroupLettersMap: Map<Int, String> = emptyMap(),
    val collectedGroupLetters: Set<String> = emptySet(),
    val secretGroupTargetId: Int = 5,
    val decoderSecondsElapsed: Int = 0,
    val decoderThinkingSecondsLeft: Int = 60,
    val help1Used: Boolean = false,
    val help2Used: Boolean = false,
    val help1Shift: Int = 0,
    val help1CaesarText: String = "",
    val help2MaskedText: String = "",
    val showCaesarAlphabet: Boolean = false,
    val decoderLockoutSecondsLeft: Int = 0,
    val guessRemainingAttempts: Int = 999,
    val incorrectGuessesCount: Int = 0,
    val post15GuessesUsed: Int = 0,
    // THESIS UPGRADE: Collected Images Container
    val collectedImages: Map<Int, List<String>> = emptyMap(),
    // THESIS UPGRADE: Decrypted images for the current level
    val currentLevelImages: List<String> = emptyList(),
    // THESIS UPGRADE: Puzzle solved status
    val puzzleSolved: Map<Int, Boolean> = emptyMap(),
    // THESIS UPGRADE: Active puzzle artifact
    val puzzleArtifactId: Int? = null,
    // SYSTEM UPGRADE: Top Slide-Down Notification Banner
    val topNotificationMessage: String? = null,
    val topNotificationType: String = "info",
    // GAMIFICATION: 360-Degree Card Collect Animation State
    val activeCollectedCard: CardCollectData? = null,
    val showCartDialog: Boolean = false,
    // MULTI-OBJECT AI DETECTION
    val showMultiObjectSelectionDialog: Boolean = false,
    val detectedObjectsList: List<DetectedObjectItem> = emptyList(),
    val multiObjectDetectionImageBase64: String? = null,
    val multiObjectSourceTarget: String = "start",
    val pendingChatImageText: String = ""
)

data class DetectedObjectItem(
    val yoloLabel: String = "",
    val yoloConfidence: Float = 0f,
    val cnnId: Int = 0,
    val cnnLabel: String = "",
    val cnnConfidence: Float = 0f,
    val bbox: List<Int> = emptyList(),
    val artifactId: Int = 1,
    val artifactName: String = "",
    val artifactDescription: String = ""
)

data class CardCollectData(
    val imageStr: String = "",
    val artifactName: String = "",
    val pieceIndex: Int = 0,
    val artifactId: Int = 1
)

class MuseumViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MuseumUiState())
    val uiState: StateFlow<MuseumUiState> = _uiState.asStateFlow()

    fun setGpsBlockWarning(show: Boolean, name: String = "", dist: Double = 0.0) {
        _uiState.value = _uiState.value.copy(
            showGpsBlockWarning = show,
            blockedArtifactName = name,
            blockedArtifactDistance = dist
        )
    }

    fun setImageSourceDialog(show: Boolean, target: String = "start") {
        _uiState.value = _uiState.value.copy(
            showImageSourceDialog = show,
            sourceDialogTarget = target
        )
    }

    init {
        val sharedPrefs = application.getSharedPreferences("museum_prefs", android.content.Context.MODE_PRIVATE)
        val savedIp = sharedPrefs.getString("api_base_ip", "http://10.158.209.106") ?: "http://10.158.209.106"
        val portChat = sharedPrefs.getString("api_port_chat", "8000") ?: "8000"
        val portPredict = sharedPrefs.getString("api_port_predict", "8001") ?: "8001"
        val portDecrypt = sharedPrefs.getString("api_port_decrypt", "8003") ?: "8003"
        val portQuestion = sharedPrefs.getString("api_port_question", "8004") ?: "8004"
        val portImage = sharedPrefs.getString("api_port_image", "8005") ?: "8005"
        val portLeaderboard = sharedPrefs.getString("api_port_leaderboard", "8006") ?: "8006"
        LocalClient.updateApiSettings(savedIp, portChat, portPredict, portDecrypt, portQuestion, portImage, portLeaderboard)

        // Clean slate on init: clear any leftover persisted state so user starts with 0 collected pieces and 0 score
        val editor = sharedPrefs.edit()
        editor.remove("saved_total_score")
        editor.remove("saved_completed_ids")
        for (i in 1..25) {
            editor.remove("saved_collected_pieces_$i")
        }
        editor.apply()

        _uiState.value = _uiState.value.copy(
            totalScore = 0,
            currentScore = 0,
            completedArtifactIds = emptySet(),
            collectedImages = emptyMap(),
            deferredArtifactIds = emptySet(),
            discoveredArtifactIds = emptySet()
        )
    }

    private fun persistState() {
        try {
            val state = _uiState.value
            val sharedPrefs = getApplication<Application>().getSharedPreferences("museum_prefs", android.content.Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.putInt("saved_total_score", state.totalScore)
            editor.putString("saved_completed_ids", state.completedArtifactIds.joinToString(","))
            for (i in 1..25) {
                editor.remove("saved_collected_pieces_$i")
            }
            for (entry in state.collectedImages) {
                editor.putString("saved_collected_pieces_${entry.key}", entry.value.joinToString("|||"))
            }
            editor.apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun updateServerIp(newIp: String) {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("museum_prefs", android.content.Context.MODE_PRIVATE)
        val portChat = sharedPrefs.getString("api_port_chat", "8000") ?: "8000"
        val portPredict = sharedPrefs.getString("api_port_predict", "8001") ?: "8001"
        val portDecrypt = sharedPrefs.getString("api_port_decrypt", "8003") ?: "8003"
        val portQuestion = sharedPrefs.getString("api_port_question", "8004") ?: "8004"
        val portImage = sharedPrefs.getString("api_port_image", "8005") ?: "8005"
        val portLeaderboard = sharedPrefs.getString("api_port_leaderboard", "8006") ?: "8006"
        sharedPrefs.edit().putString("api_base_ip", newIp).apply()
        LocalClient.updateApiSettings(newIp, portChat, portPredict, portDecrypt, portQuestion, portImage, portLeaderboard)
    }

    fun updateServerSettings(
        newIp: String,
        newPortChat: String,
        newPortPredict: String,
        newPortDecrypt: String,
        newPortQuestion: String,
        newPortImage: String = "8005",
        newPortLeaderboard: String = "8006"
    ) {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("museum_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("api_base_ip", newIp)
            .putString("api_port_chat", newPortChat)
            .putString("api_port_predict", newPortPredict)
            .putString("api_port_decrypt", newPortDecrypt)
            .putString("api_port_question", newPortQuestion)
            .putString("api_port_image", newPortImage)
            .putString("api_port_leaderboard", newPortLeaderboard)
            .apply()
        LocalClient.updateApiSettings(newIp, newPortChat, newPortPredict, newPortDecrypt, newPortQuestion, newPortImage, newPortLeaderboard)
    }

    private var timerJob: Job? = null
    private var decoderTimerJob: Job? = null

    // Hamilton static graph structure from original Vue code
    private val staticEdges = listOf(
        Pair(0, 11), Pair(0, 14), Pair(1, 8), Pair(1, 11), Pair(1, 12), Pair(2, 3), Pair(2, 12), Pair(3, 7), Pair(3, 13),
        Pair(4, 5), Pair(4, 8), Pair(4, 9), Pair(5, 6), Pair(5, 9), Pair(6, 13), Pair(6, 14), Pair(7, 10), Pair(7, 11),
        Pair(7, 14), Pair(8, 11), Pair(9, 12), Pair(9, 13), Pair(10, 12), Pair(10, 13), Pair(13, 14)
    )

    private val cryptoKeysPool = mapOf(
        0 to "SAM7", 1 to "BOM9", 2 to "GHE2", 3 to "LUH3", 4 to "MAY4",
        5 to "TOL5", 6 to "IND6", 7 to "NEO7", 8 to "PHA8", 9 to "SUN9",
        10 to "PCF1", 11 to "B522", 12 to "XEB3", 13 to "PEU4", 14 to "XET5"
    )

    fun navigateTo(screen: AppScreen) {
        var updatedScore = _uiState.value.totalScore
        if (screen is AppScreen.Game) {
            if (updatedScore >= 100) {
                updatedScore -= 100
                showTopNotification("💸 Đã dùng 100 điểm tri thức khi vào màn chơi!", "info")
            } else if (updatedScore > 0) {
                showTopNotification("💸 Đã dùng toàn bộ $updatedScore điểm hiện có khi vào màn chơi!", "info")
                updatedScore = 0
            } else {
                showTopNotification("⚠️ Tiến vào màn chơi với 0 điểm tri thức!", "info")
            }
        }
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            totalScore = updatedScore,
            currentScore = updatedScore,
            activeCollectedCard = null,
            showCartDialog = false
        )
    }

    private var sessionTimerJob: Job? = null

    fun login(username: String, email: String = "", ticket: String = "") {
        val finalUser = if (username.isBlank()) "Khách Tham Quan" else username.trim()
        val finalEmail = if (email.isBlank()) "khach@museai.vn" else email.trim()
        val dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"))
        val finalTicket = if (ticket.isBlank()) "MUSEAI-$dateStr" else ticket.trim()

        try {
            val sharedPrefs = getApplication<Application>().getSharedPreferences("museum_prefs", android.content.Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.remove("saved_total_score")
            editor.remove("saved_completed_ids")
            for (i in 1..25) {
                editor.remove("saved_collected_pieces_$i")
            }
            editor.apply()
        } catch (_: Exception) {}

        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            loggedInUser = finalUser,
            userEmail = finalEmail,
            ticketCode = finalTicket,
            totalScore = 0,
            currentScore = 0,
            completedArtifactIds = emptySet(),
            collectedImages = emptyMap(),
            deferredArtifactIds = emptySet(),
            discoveredArtifactIds = emptySet(),
            sessionStartTimeMs = System.currentTimeMillis(),
            sessionElapsedSeconds = 0,
            showEndGameModal = false,
            endGameReceipt = null,
            currentScreen = AppScreen.Dashboard
        )
        startGlobalSessionTimer()
    }

    fun startGlobalSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value
                if (current.isLoggedIn && !current.showEndGameModal) {
                    val newElapsed = current.sessionElapsedSeconds + 1
                    if (newElapsed >= 18000) { // 5 hours timeout (5 * 3600s)
                        endGameAndSendMail(isAutoTimeout = true)
                        break
                    } else {
                        _uiState.value = _uiState.value.copy(sessionElapsedSeconds = newElapsed)
                    }
                } else {
                    break
                }
            }
        }
    }

    fun endGameAndSendMail(isAutoTimeout: Boolean = false) {
        val state = _uiState.value
        val elapsedSec = state.sessionElapsedSeconds
        val hours = elapsedSec / 3600
        val mins = (elapsedSec % 3600) / 60
        val secs = elapsedSec % 60
        val timeFormatted = if (hours > 0) {
            "${hours} giờ ${mins} phút ${secs} giây"
        } else {
            "${mins} phút ${secs} giây"
        }

        val targetEmail = state.userEmail.ifBlank { "khach@baotangqk9.vn" }

        val receipt = EndGameReceipt(
            userName = state.loggedInUser.ifBlank { "Du Khách Tham Quan" },
            ticketCode = state.ticketCode.ifBlank { "QK9-2026-8888" },
            userEmail = targetEmail,
            totalScore = state.totalScore,
            playTimeFormatted = timeFormatted,
            totalVisited = state.completedArtifactIds.size,
            totalSkipped = state.deferredArtifactIds.size,
            thankYouMessage = "Ban Quản Lý Bảo Tàng Quân Khu 9 xin chân thành cảm ơn Quý khách ${state.loggedInUser} đã dành thời gian tham quan và hoàn thành hành trình thám hiểm di sản! Báo cáo kết quả và thư cảm ơn đã được tự động gửi về địa chỉ: $targetEmail. Kính chúc Quý khách nhiều sức khỏe!",
            isAutoTimeout = isAutoTimeout
        )

        _uiState.value = _uiState.value.copy(
            showEndGameModal = true,
            endGameReceipt = receipt
        )

        // Send real email & Submit to API port 8006 & Database
        viewModelScope.launch {
            try {
                com.example.network.EmailService.sendRealEmail(
                    targetEmail = targetEmail,
                    userName = state.loggedInUser,
                    ticketCode = state.ticketCode.ifBlank { "QK9-2026-8888" },
                    totalScore = state.totalScore,
                    playTimeFormatted = timeFormatted,
                    totalVisited = state.completedArtifactIds.size,
                    totalSkipped = state.deferredArtifactIds.size,
                    heroTitle = getHeroTitle()
                )
            } catch (_: Exception) {}

            try {
                com.example.data.LeaderboardRepository.submitUserScore(
                    userName = state.loggedInUser,
                    heroTitle = getHeroTitle(),
                    score = state.totalScore,
                    clearedStages = state.completedArtifactIds.size,
                    userEmail = targetEmail,
                    ticketCode = state.ticketCode
                )
            } catch (_: Exception) {}
        }

        showTopNotification("📧 Báo cáo kết quả & Thư cảm ơn đã được gửi tới email người dùng: $targetEmail!", "success")
    }

    fun dismissEndGameModal() {
        _uiState.value = _uiState.value.copy(showEndGameModal = false)
    }

    fun confirmEndGameAndReset() {
        sessionTimerJob?.cancel()
        timerJob?.cancel()
        decoderTimerJob?.cancel()

        val lastUser = _uiState.value.loggedInUser
        val lastEmail = _uiState.value.userEmail

        _uiState.value = MuseumUiState(
            isLoggedIn = true,
            loggedInUser = lastUser,
            userEmail = lastEmail,
            ticketCode = "QK9-2026-${(1000..9999).random()}",
            currentScreen = AppScreen.Dashboard
        )
        showTopNotification("🔄 Đã kết thúc phiên thám hiểm & làm mới dữ liệu trò chơi!", "info")
        startGlobalSessionTimer()
    }

    fun logout() {
        sessionTimerJob?.cancel()
        timerJob?.cancel()
        decoderTimerJob?.cancel()
        _uiState.value = MuseumUiState()
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(activeTab = index)
        if (index == 3) {
            startDecoderTimer()
        } else {
            decoderTimerJob?.cancel()
        }
    }

    fun startDecoderTimer() {
        decoderTimerJob?.cancel()
        decoderTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value
                if (!current.secretGuessCorrect) {
                    var newThinkingLeft = current.decoderThinkingSecondsLeft
                    var newSecondsElapsed = current.decoderSecondsElapsed
                    var newTotalElapsed = current.totalElapsedTime
                    var newLockout = current.decoderLockoutSecondsLeft
                    
                    if (newLockout > 0) {
                        newLockout -= 1
                    }
                    
                    if (current.help1Used || current.help2Used) {
                        newThinkingLeft = 0
                        newSecondsElapsed += 1
                        newTotalElapsed += 1
                    } else if (newThinkingLeft > 0) {
                        newThinkingLeft -= 1
                    } else {
                        newSecondsElapsed += 1
                        newTotalElapsed += 1
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        decoderThinkingSecondsLeft = newThinkingLeft,
                        decoderSecondsElapsed = newSecondsElapsed,
                        totalElapsedTime = newTotalElapsed,
                        decoderLockoutSecondsLeft = newLockout
                    )
                } else {
                    break
                }
            }
        }
    }

    fun stripAccents(input: String): String {
        val src = "ÁÀẢÃẠĂẮẰẲẴẶÂẤẦẨẪẬÉÈẺẼẸÊẾỀỂỄỆÍÌỈĨỊÓÒỎÕỌÔỐỒỔỖỘƠỚỜỞỠỢÚÙỦŨỤƯỨỪỬỮỰÝỲỶỸYĐáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹyd"
        val dest = "AAAAAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYYDAAAAAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYYD"
        val sb = StringBuilder()
        for (char in input) {
            val idx = src.indexOf(char)
            if (idx != -1) {
                sb.append(dest[idx])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun encryptCaesar(text: String, n: Int): String {
        val sb = StringBuilder()
        for (char in text) {
            if (char in 'A'..'Z') {
                val originalIndex = char - 'A'
                val shiftedIndex = (originalIndex - n + 26) % 26
                val shiftedChar = ('A'.code + shiftedIndex).toChar()
                sb.append(shiftedChar)
            } else if (char in 'a'..'z') {
                val originalIndex = char - 'a'
                val shiftedIndex = (originalIndex - n + 26) % 26
                val shiftedChar = ('a'.code + shiftedIndex).toChar()
                sb.append(shiftedChar)
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun useDecoderHelp1() {
        val state = _uiState.value
        if (state.completedArtifactIds.size < 13) {
            showTopNotification("🔒 Bạn cần vượt qua ít nhất 13 ải mới được kích hoạt Trợ giúp 1!", "warning")
            return
        }
        if (state.help1Used) return
        val candidate = secretCandidatesList.getOrNull(state.secretArtifactId) ?: return
        
        val n = (1..25).random()
        val plainText = stripAccents(candidate.name).uppercase(Locale.ROOT)
        val encryptedText = encryptCaesar(plainText, n)
        
        val newScore = (state.totalScore * 0.85).toInt()
        
        val newState = state.copy(
            help1Used = true,
            help1Shift = n,
            help1CaesarText = encryptedText,
            totalScore = newScore,
            currentScore = newScore,
            decoderThinkingSecondsLeft = 0
        )
        _uiState.value = updateGuessAttempts(newState)
        showTopNotification("💡 Đã kích hoạt Trợ giúp 1 (Mật mã Caesar)! Trừ 15% tổng điểm.", "info")
        playFeedbackTone(350, 400)
    }

    fun useDecoderHelp2() {
        val state = _uiState.value
        if (state.completedArtifactIds.size < 13) {
            showTopNotification("🔒 Bạn cần vượt qua ít nhất 13 ải mới được kích hoạt Trợ giúp 2!", "warning")
            return
        }
        if (state.help2Used) return
        val candidate = secretCandidatesList.getOrNull(state.secretArtifactId) ?: return
        
        val answerName = candidate.name
        val nonSpaceIndices = answerName.indices.filter { answerName[it] != ' ' }
        val percentage = (50..70).random()
        val revealCount = (nonSpaceIndices.size * percentage / 100).coerceAtLeast(1)
        val indicesToReveal = nonSpaceIndices.shuffled().take(revealCount).toSet()
        
        val masked = StringBuilder()
        for (i in answerName.indices) {
            val char = answerName[i]
            if (char == ' ') {
                masked.append(' ')
            } else if (indicesToReveal.contains(i)) {
                masked.append(char)
            } else {
                masked.append('_')
            }
        }
        
        val newScore = (state.totalScore * 0.35).toInt()
        
        val newState = state.copy(
            help2Used = true,
            help2MaskedText = masked.toString(),
            totalScore = newScore,
            currentScore = newScore,
            decoderThinkingSecondsLeft = 0
        )
        _uiState.value = updateGuessAttempts(newState)
        showTopNotification("💡 Đã kích hoạt Trợ giúp 2 (Hiển thị 50-70% chữ cái)! Trừ 65% tổng điểm.", "info")
        playFeedbackTone(250, 600)
    }

    fun toggleCaesarAlphabet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCaesarAlphabet = show)
    }

    private fun playFeedbackTone(freqHz: Int, durationMs: Int) {
        try {
            val toneGenerator = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)
            val toneType = when {
                freqHz > 1200 -> android.media.ToneGenerator.TONE_PROP_BEEP
                freqHz > 800 -> android.media.ToneGenerator.TONE_CDMA_HIGH_L
                freqHz < 300 -> android.media.ToneGenerator.TONE_SUP_ERROR
                else -> android.media.ToneGenerator.TONE_PROP_ACK
            }
            toneGenerator.startTone(toneType, durationMs)
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            userLatitude = latitude,
            userLongitude = longitude
        )
    }

    fun toggleMockLocation(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(mockLocationEnabled = enabled)
    }

    // Haversine formula
    fun getDistanceToArtifact(artifact: Artifact): Double {
        val lat1 = _uiState.value.userLatitude
        val lon1 = _uiState.value.userLongitude
        val r = 6371000.0 // Earth's radius in meters

        if (artifact.locations.isEmpty()) {
            val lat2 = artifact.latitude
            val lon2 = artifact.longitude
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        return artifact.locations.minOf { loc ->
            val dLat = Math.toRadians(loc.latitude - lat1)
            val dLon = Math.toRadians(loc.longitude - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(loc.latitude)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            r * c
        }
    }

    fun isWithinRange(artifact: Artifact): Boolean {
        return getDistanceToArtifact(artifact) <= 50.0
    }

    fun teleportTo(artifact: Artifact) {
        _uiState.value = _uiState.value.copy(
            userLatitude = artifact.latitude,
            userLongitude = artifact.longitude
        )
    }

    fun teleportAndNavigateTo(artifactId: Int) {
        val artifact = _uiState.value.artifacts.find { it.id == artifactId }
        if (artifact != null) {
            _uiState.value = _uiState.value.copy(
                userLatitude = artifact.latitude,
                userLongitude = artifact.longitude
            )
            val nodeId = artifactIdToNodeId(artifactId)
            val pathIdx = _uiState.value.lockedHamiltonPath.indexOf(nodeId)
            if (pathIdx != -1) {
                _uiState.value = _uiState.value.copy(
                    currentPathIndex = pathIdx,
                    currentStageCleared = false
                )
                setupCurrentStage(nodeId)
            }
            navigateTo(AppScreen.Game(artifactId))
        }
    }

    fun moveAwayFrom(artifact: Artifact, offsetInMeters: Double) {
        val deltaLat = offsetInMeters / 111111.0
        _uiState.value = _uiState.value.copy(
            userLatitude = artifact.latitude + deltaLat,
            userLongitude = artifact.longitude
        )
    }

    // 1-to-1 conversion between Artifact ID (1-15) and Hamilton Graph Node ID (0-14)
    fun artifactIdToNodeId(artId: Int): Int {
        return when (artId) {
            1 -> 1; 2 -> 0; 3 -> 2; 4 -> 3; 5 -> 5; 6 -> 6; 7 -> 7; 8 -> 8
            9 -> 9; 10 -> 11; 11 -> 10; 12 -> 12; 13 -> 13; 14 -> 14; 15 -> 4
            else -> 0
        }
    }

    fun nodeIdToArtifactId(nodeId: Int): Int {
        return when (nodeId) {
            0 -> 2; 1 -> 1; 2 -> 3; 3 -> 4; 4 -> 15; 5 -> 5; 6 -> 6; 7 -> 7
            8 -> 8; 9 -> 9; 10 -> 11; 11 -> 10; 12 -> 12; 13 -> 13; 14 -> 14
            else -> 1
        }
    }

    fun getArtifactByNodeId(nodeId: Int): Artifact? {
        val id = nodeIdToArtifactId(nodeId)
        return _uiState.value.artifacts.find { it.id == id }
    }

    // Find Hamilton Path or DFS backup path
    fun findHamiltonPath(startNodeId: Int): List<Int>? {
        val numNodes = 15
        val adj = List(numNodes) { mutableListOf<Int>() }
        for (edge in staticEdges) {
            adj[edge.first].add(edge.second)
            adj[edge.second].add(edge.first)
        }

        val path = mutableListOf<Int>()
        path.add(startNodeId)
        val visited = BooleanArray(numNodes)
        visited[startNodeId] = true

        fun search(current: Int): Boolean {
            if (path.size == numNodes) {
                return true
            }
            for (neighbor in adj[current]) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true
                    path.add(neighbor)
                    if (search(neighbor)) return true
                    path.removeAt(path.size - 1)
                    visited[neighbor] = false
                }
            }
            return false
        }

        if (search(startNodeId)) {
            return path
        }
        return null
    }

    fun findBackupDFSPath(startId: Int): List<Int> {
        val numNodes = 15
        val adj = List(numNodes) { mutableListOf<Int>() }
        for (edge in staticEdges) {
            adj[edge.first].add(edge.second)
            adj[edge.second].add(edge.first)
        }

        val path = mutableListOf<Int>()
        val visited = BooleanArray(numNodes)

        fun dfs(node: Int) {
            visited[node] = true
            path.add(node)
            for (neighbor in adj[node]) {
                if (!visited[neighbor]) {
                    dfs(neighbor)
                }
            }
        }

        dfs(startId)

        // Ensure all unvisited are included
        for (i in 0 until numNodes) {
            if (!visited[i]) {
                path.add(i)
            }
        }
        return path
    }

    // Initialize routing path based on detected start node
    fun initializeGamePath(startNodeId: Int) {
        val path = findHamiltonPath(startNodeId) ?: findBackupDFSPath(startNodeId)
        val fixedSecretIdx = 8 // Fixed candidate index for "Súng thần công" (id = 9)
        
        val secretCandidate = secretCandidatesList.getOrNull(fixedSecretIdx) ?: secretCandidatesList[0]
        val secretArtRealId = secretCandidate.id
        val groupId = getGroupIdForArtifactId(secretArtRealId)
        val puzzleName = getGroupPuzzleName(groupId)
        val uniqueLetters = puzzleName.filter { it.isLetter() }.uppercase(Locale.ROOT).toSet().toList().shuffled()
        val shuffledStages = (0..14).shuffled()
        val lettersMap = mutableMapOf<Int, String>()
        for (i in 0..14) {
            val stageIdx = shuffledStages[i]
            val letter = uniqueLetters[i % uniqueLetters.size].toString()
            lettersMap[stageIdx] = letter
        }
        val possibleIndices = (0..14).shuffled().take(5).sorted()
        
        val newState = _uiState.value.copy(
            lockedHamiltonPath = path,
            gameLocked = true,
            currentPathIndex = 0,
            secretArtifactId = fixedSecretIdx,
            clueStageIndices = possibleIndices,
            collectedClues = emptyMap(),
            secretGuessCorrect = false,
            secretGuessMultiplier = 1,
            secretGuessScoreBonus = 0,
            post15GuessesUsed = 0,
            collectedLettersByGroup = mapOf(1 to "", 2 to "", 3 to "", 4 to "", 5 to ""),
            secretGroupTargetId = groupId,
            secretGroupLettersMap = lettersMap,
            collectedGroupLetters = emptySet()
        )
        _uiState.value = updateGuessAttempts(newState)
        setupCurrentStage(path[0])
    }

    // Setup active stage, reset timers, and trigger RAG greetings
    fun setupCurrentStage(nodeId: Int) {
        val artifact = getArtifactByNodeId(nodeId)
        val artifactId = artifact?.id ?: 1
        val artifactName = artifact?.name ?: ""

        _uiState.value = _uiState.value.copy(
            currentArtifactId = artifactId,
            currentArtifactName = artifactName,
            currentStageCleared = false,
            timeOutReached = false,
            timeLeft = 300,
            secondsElapsedInStage = 0,
            userLatitude = artifact?.latitude ?: _uiState.value.userLatitude,
            userLongitude = artifact?.longitude ?: _uiState.value.userLongitude,
            chatMessages = _uiState.value.chatMessages + ChatMessage(
                MessageSender.AI,
                "Định vị thành công. Chặng hiện tại của bạn là: <strong>[$artifactName]</strong>. Hãy đặt câu hỏi tìm hiểu lịch sử, lựa chọn đính kèm ảnh hiện vật hoặc tham gia trò chơi để tìm mã mở khoá!"
            )
        )
        startStageTimer()
        triggerRAGAutoGreeting()
    }

    private fun startStageTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value
                if (current.gameLocked && !current.currentStageCleared) {
                    val newTimeLeft = (current.timeLeft - 1).coerceAtLeast(0)
                    val newElapsed = current.secondsElapsedInStage + 1
                    val isTimeout = newTimeLeft <= 0
                    _uiState.value = _uiState.value.copy(
                        timeLeft = newTimeLeft,
                        timeOutReached = isTimeout,
                        secondsElapsedInStage = newElapsed
                    )
                } else {
                    break
                }
            }
        }
    }

    // Dynamic auto-greeting from RAG / Gemini
    fun triggerRAGAutoGreeting() {
        val state = _uiState.value
        val currentName = state.currentArtifactName
        val currentId = state.currentArtifactId ?: 1
        var contextPrompt = "Giới thiệu về hiện vật bảo tàng: $currentName"

        if (state.currentPathIndex > 0) {
            val prevNodeId = state.lockedHamiltonPath[state.currentPathIndex - 1]
            val prevName = getArtifactByNodeId(prevNodeId)?.name ?: ""
            contextPrompt += ". Kết nối lộ trình tiếp nối từ chặng trước đó có tên là: \"$prevName\" để dẫn dắt một cách mượt mà cho học viên."
        }

        _uiState.value = _uiState.value.copy(isChatLoading = true)

        viewModelScope.launch {
            var reply = ""
            var suggestions = listOf<String>()
            try {
                // Try localhost RAG server first
                val res = LocalClient.chatApiService.chat(LocalChatRequest(contextPrompt, currentId))
                val item = res.responses?.firstOrNull()
                reply = item?.answer ?: ""
                suggestions = item?.suggestions ?: emptyList()
            } catch (e: Exception) {
                // Fail silently, fallback to Gemini AI direct
            }

            if (reply.isEmpty()) {
                val systemPrompt = """
                    Bạn là Trợ lý AI Hướng dẫn viên chính thức tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ).
                    Khách tham quan vừa di chuyển tới trạm cổ vật: $currentName.
                    YÊU CẦU TRẢ LỜI:
                    - ĐÚNG TRỌNG TÂM, KHÔNG dài dòng rườm rà.
                    - Nêu 2-3 ý chính dùng thẻ <strong>: <strong>🏛️ Hiện vật:</strong> $currentName, <strong>📜 Lịch sử & Ý nghĩa QK9:</strong>.
                    - Xuất 3 câu hỏi gợi ý ngắn gọn ở cuối (mỗi câu trên 1 dòng).
                """.trimIndent()
                val response = GeminiClient.getChatbotResponse(contextPrompt, systemPrompt)
                
                // Parse suggestions
                val parts = response.split("\n").map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("-") && !it.startsWith("*") }
                if (parts.size >= 4) {
                    reply = parts.take(parts.size - 3).joinToString("\n")
                    suggestions = parts.takeLast(3).map { it.replace("?", "").replace(":", "") }
                } else {
                    reply = response
                    suggestions = listOf("Ý nghĩa lịch sử của $currentName?", "Sự thật thú vị về $currentName?", "Cổ vật này làm bằng gì?")
                }
            }

            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + ChatMessage(
                    MessageSender.AI,
                    reply,
                    suggestions = suggestions
                ),
                isChatLoading = false
            )
        }
    }

    // Handles user sending message or decrypted core key
    fun sendChatMessage(text: String, imageUri: String? = null, gameScore: Int = 0) {
        if (text.isBlank() && imageUri == null) return

        val userMsg = ChatMessage(MessageSender.USER, text, image = imageUri)
        val currentMsgs = _uiState.value.chatMessages + userMsg

        _uiState.value = _uiState.value.copy(
            chatMessages = currentMsgs,
            isChatLoading = true
        )

        val activeNodeId = _uiState.value.lockedHamiltonPath.getOrNull(_uiState.value.currentPathIndex) ?: 0
        val correctKey = cryptoKeysPool[activeNodeId] ?: ""

        // Check for correct decryption key
        if (text.trim().uppercase(Locale.ROOT) == correctKey || text.trim().uppercase(Locale.ROOT).contains(correctKey)) {
            val elapsed = _uiState.value.secondsElapsedInStage
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = dateFormat.format(Date())

            val updatedHistory = _uiState.value.stationHistoryLog.toMutableMap()
            updatedHistory[activeNodeId] = StationHistory(timeSpent = elapsed, clearedAt = timeString, scoreSpent = gameScore)

            val currentCompleted = _uiState.value.completedArtifactIds
            val curArtifactId = nodeIdToArtifactId(activeNodeId)
            val newCompleted = currentCompleted + curArtifactId

            // Recalculate totals preserving accumulated score
            val totalScore = maxOf(_uiState.value.totalScore + gameScore, updatedHistory.values.sumOf { it.scoreSpent })
            val totalTime = updatedHistory.values.sumOf { it.timeSpent } + _uiState.value.decoderSecondsElapsed

            _uiState.value = _uiState.value.copy(
                currentStageCleared = true,
                stationHistoryLog = updatedHistory,
                completedArtifactIds = newCompleted,
                currentScore = totalScore,
                totalScore = totalScore,
                totalElapsedTime = totalTime,
                chatMessages = _uiState.value.chatMessages + ChatMessage(
                    MessageSender.AI,
                    "<strong>XÁC THỰC THÀNH CÔNG!</strong> Mật mã lõi <code>$correctKey</code> chính xác. Bạn đã mất đúng <strong>$elapsed giây</strong> nghiên cứu tại trạm này. Dữ liệu của trạm <strong>[${_uiState.value.currentArtifactName}]</strong> đã được đồng bộ hóa hoàn toàn."
                ),
                isChatLoading = false
            )

            onStageCleared(curArtifactId)

            // Auto-trigger completion popup if it was the last station
            if (_uiState.value.currentPathIndex >= _uiState.value.lockedHamiltonPath.size - 1) {
                _uiState.value = _uiState.value.copy(showSummaryModal = true)
            }
            return
        }

        // Regular chat response
        val activeId = _uiState.value.currentArtifactId ?: 1
        viewModelScope.launch {
            var answer = ""
            var suggestions = listOf<String>()

            if (imageUri != null) {
                var detectedList = mutableListOf<DetectedObjectItem>()
                var encodedImageBase64: String? = null
                var errorDetail: String? = null
                try {
                    val context = getApplication<Application>()
                    val tempFile = File.createTempFile("chat_upload", ".jpg", context.cacheDir)
                    context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { input ->
                        tempFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                    val res = LocalClient.predictApiService.predict(body)
                    val responseString = res.string()
                    android.util.Log.d("MuseumViewModel", "Chat Predict raw response: $responseString")
                    
                    val jsonObject = org.json.JSONObject(responseString)
                    val resultsArray = jsonObject.optJSONArray("results")
                    encodedImageBase64 = jsonObject.optString("image", "").ifEmpty { null }

                    if (resultsArray != null && resultsArray.length() > 0) {
                        for (i in 0 until resultsArray.length()) {
                            val itemObj = resultsArray.optJSONObject(i) ?: continue
                            val cnnIdObj = itemObj.opt("cnn_id")
                            val cnnId = cnnIdObj?.toString()?.toIntOrNull() ?: 0
                            val cnnLabel = itemObj.optString("cnn_label", "")
                            val cnnConf = itemObj.optDouble("cnn_confidence", 0.0).toFloat()
                            val yoloLabel = itemObj.optString("yolo_label", "")
                            val yoloConf = itemObj.optDouble("yolo_confidence", 0.0).toFloat()
                            
                            val bboxArray = itemObj.optJSONArray("bbox")
                            val bbox = mutableListOf<Int>()
                            if (bboxArray != null) {
                                for (j in 0 until bboxArray.length()) {
                                    bbox.add(bboxArray.optInt(j))
                                }
                            }

                            val artId = nodeIdToArtifactId(cnnId)
                            val artifact = _uiState.value.artifacts.find { it.id == artId }
                            val finalName = if (artifact != null) artifact.name else if (cnnLabel.isNotBlank()) cnnLabel else "Hiện vật #$artId"
                            val desc = artifact?.description ?: ""

                            detectedList.add(
                                DetectedObjectItem(
                                    yoloLabel = yoloLabel,
                                    yoloConfidence = yoloConf,
                                    cnnId = cnnId,
                                    cnnLabel = if (cnnLabel.isNotBlank()) cnnLabel else finalName,
                                    cnnConfidence = cnnConf,
                                    bbox = bbox,
                                    artifactId = artId,
                                    artifactName = finalName,
                                    artifactDescription = desc
                                )
                            )
                        }
                    } else {
                        errorDetail = jsonObject.optString("message", "Không tìm thấy đối tượng nào trong ảnh.")
                    }
                } catch (e: retrofit2.HttpException) {
                    val code = e.code()
                    val errorBody = e.response()?.errorBody()?.string() ?: ""
                    android.util.Log.e("MuseumViewModel", "Chat Prediction HTTP error: $code - $errorBody", e)
                    errorDetail = "HTTP Lỗi $code: ${if (errorBody.length > 100) errorBody.take(100) + "..." else errorBody}"
                } catch (e: java.net.ConnectException) {
                    android.util.Log.e("MuseumViewModel", "Chat Prediction connect error", e)
                    errorDetail = "Không thể kết nối đến Máy chủ (Connection Refused) tại địa chỉ ${LocalClient.getBaseIp()}:8001. Hãy kiểm tra Wifi/máy chủ."
                } catch (e: java.net.SocketTimeoutException) {
                    android.util.Log.e("MuseumViewModel", "Chat Prediction timeout error", e)
                    errorDetail = "Quá hạn kết nối đến Máy chủ (Timeout) tại địa chỉ ${LocalClient.getBaseIp()}:8001"
                } catch (e: Exception) {
                    android.util.Log.e("MuseumViewModel", "Chat predict failed: ", e)
                    errorDetail = e.localizedMessage ?: e.toString()
                }

                if (detectedList.size > 1) {
                    // Multiple objects in chat -> trigger multi-object selection dialog
                    _uiState.value = _uiState.value.copy(
                        showMultiObjectSelectionDialog = true,
                        detectedObjectsList = detectedList,
                        multiObjectDetectionImageBase64 = encodedImageBase64,
                        multiObjectSourceTarget = "chat",
                        pendingChatImageText = text,
                        isChatLoading = false
                    )
                    return@launch
                }

                val detectedItem = detectedList.firstOrNull()
                if (detectedItem != null) {
                    val artId = detectedItem.artifactId
                    val detectedName = detectedItem.artifactName
                    val chatMessageText = "Tôi vừa gửi ảnh chụp của hiện vật: \"$detectedName\". Hãy thuyết minh chi tiết cho tôi về lịch sử và ý nghĩa của hiện vật này." +
                            (if (text.isNotBlank()) " Ngoài ra, tôi muốn hỏi thêm: $text" else "")

                    try {
                        val res = LocalClient.chatApiService.chat(LocalChatRequest(chatMessageText, activeId))
                        val item = res.responses?.firstOrNull()
                        val rawAns = item?.answer ?: ""
                        answer = "<strong>Hệ thống nhận diện ảnh:</strong> Đây là <strong>$detectedName</strong>.<br><br>$rawAns"
                        suggestions = item?.suggestions ?: emptyList()
                    } catch (e: Exception) {
                        android.util.Log.e("MuseumViewModel", "Chat service failed for image message: ", e)
                    }

                    if (answer.isEmpty()) {
                        val systemPrompt = """
                            Bạn là Trợ lý AI Hướng dẫn viên chính thức tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ).
                            Đang trả lời về hiện vật được nhận diện qua ảnh: $detectedName.
                            YÊU CẦU: Trả lời ĐÚNG TRỌNG TÂM, KHÔNG dài dòng rườm rà. Dùng thẻ <strong> cho các ý chính (Lịch sử, Đặc điểm, Ý nghĩa QK9). Tối đa 3-4 câu.
                        """.trimIndent()
                        val rawAns = GeminiClient.getChatbotResponse(chatMessageText, systemPrompt)
                        answer = "<strong>Hệ thống nhận diện ảnh:</strong> Đây là <strong>$detectedName</strong>.<br><br>$rawAns"
                        suggestions = listOf("Ý nghĩa lịch sử QK9?", "Đặc điểm nổi bật?", "Vị trí trưng bày?")
                    }
                } else {
                    answer = "<strong>Hệ thống không nhận dạng được:</strong> Không thể phân tích ảnh hiện vật bạn vừa gửi.<br><br>" +
                            (if (errorDetail != null) "<strong>Chi tiết lỗi kết nối:</strong> <code>$errorDetail</code><br><br>" else "") +
                            "Vui lòng chụp cận cảnh, đủ sáng và rõ nét hơn, hoặc kiểm tra cấu hình địa chỉ IP máy chủ của bạn."
                    suggestions = emptyList()
                }
            } else {
                // Image uri is null, regular text-only message
                try {
                    val res = LocalClient.chatApiService.chat(LocalChatRequest(text, activeId))
                    val item = res.responses?.firstOrNull()
                    answer = item?.answer ?: ""
                    suggestions = item?.suggestions ?: emptyList()
                } catch (e: Exception) {
                    android.util.Log.e("MuseumViewModel", "Chat service failed for text-only: ", e)
                }

                if (answer.isEmpty()) {
                    val systemPrompt = """
                        Bạn là Trợ lý AI Hướng dẫn viên chính thức tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ).
                        Đang trả lời câu hỏi về hiện vật: ${_uiState.value.currentArtifactName}.
                        YÊU CẦU:
                        - Trả lời ĐÚNG TRỌNG TÂM, trực tiếp vào câu hỏi của khách tham quan, KHÔNG dài dòng rườm rà.
                        - Trình bày rõ ràng bằng thẻ <strong> cho thông tin quan trọng.
                        - Ngắn gọn 2-4 câu súc tích.
                    """.trimIndent()
                    answer = GeminiClient.getChatbotResponse(text, systemPrompt)
                    suggestions = listOf("Giới thiệu Bảo tàng QK9?", "Lịch sử ${_uiState.value.currentArtifactName}?", "Giờ mở cửa tham quan?")
                }
            }

            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + ChatMessage(
                    MessageSender.AI,
                    answer,
                    suggestions = suggestions
                ),
                isChatLoading = false
            )
        }
    }

    // Process chat commentary when user picks a specific detected artifact from multi-object box
    fun processChatForDetectedArtifact(detectedNodeId: Int, userText: String) {
        _uiState.value = _uiState.value.copy(isChatLoading = true)
        viewModelScope.launch {
            val artId = nodeIdToArtifactId(detectedNodeId)
            val detectedArtifact = _uiState.value.artifacts.find { it.id == artId }
            val detectedName = detectedArtifact?.name ?: "Hiện vật chưa xác định"
            val chatMessageText = "Tôi vừa gửi ảnh chụp của hiện vật: \"$detectedName\". Hãy thuyết minh chi tiết cho tôi về lịch sử và ý nghĩa của hiện vật này." +
                    (if (userText.isNotBlank()) " Ngoài ra, tôi muốn hỏi thêm: $userText" else "")

            var answer = ""
            var suggestions = listOf<String>()
            try {
                val res = LocalClient.chatApiService.chat(LocalChatRequest(chatMessageText, artId))
                val item = res.responses?.firstOrNull()
                val rawAns = item?.answer ?: ""
                answer = "<strong>Hệ thống nhận diện ảnh:</strong> Đây là <strong>$detectedName</strong>.<br><br>$rawAns"
                suggestions = item?.suggestions ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("MuseumViewModel", "Chat service failed for multi-object image: ", e)
            }

            if (answer.isEmpty()) {
                val systemPrompt = """
                    Bạn là Trợ lý AI Hướng dẫn viên chính thức tại Bảo tàng Quân khu 9 - Cần Thơ (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ).
                    Đang trả lời về hiện vật được nhận diện qua ảnh: $detectedName.
                    YÊU CẦU: Trả lời ĐÚNG TRỌNG TÂM, KHÔNG dài dòng rườm rà. Dùng thẻ <strong> cho các ý chính (Lịch sử, Đặc điểm, Ý nghĩa QK9). Tối đa 3-4 câu.
                """.trimIndent()
                val rawAns = GeminiClient.getChatbotResponse(chatMessageText, systemPrompt)
                answer = "<strong>Hệ thống nhận diện ảnh:</strong> Đây là <strong>$detectedName</strong>.<br><br>$rawAns"
                suggestions = listOf("Ý nghĩa lịch sử QK9?", "Đặc điểm nổi bật?", "Vị trí trưng bày?")
            }

            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + ChatMessage(
                    MessageSender.AI,
                    answer,
                    suggestions = suggestions
                ),
                isChatLoading = false
            )
        }
    }

    // Handles prediction of uploaded image (mocked or real)
    fun uploadAndPredictImage(imageFile: File?, simulatedNodeId: Int? = null) {
        _uiState.value = _uiState.value.copy(uploading = true)

        viewModelScope.launch {
            var detectedList = mutableListOf<DetectedObjectItem>()
            var encodedImageBase64: String? = null
            var errorDetail: String? = null

            if (simulatedNodeId != null) {
                val artId = nodeIdToArtifactId(simulatedNodeId)
                val art = _uiState.value.artifacts.find { it.id == artId }
                val name = art?.name ?: "Hiện vật #$artId"
                detectedList.add(
                    DetectedObjectItem(
                        yoloLabel = "simulated",
                        yoloConfidence = 1.0f,
                        cnnId = simulatedNodeId,
                        cnnLabel = name,
                        cnnConfidence = 1.0f,
                        artifactId = artId,
                        artifactName = name,
                        artifactDescription = art?.description ?: ""
                    )
                )
            } else if (imageFile != null) {
                try {
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
                    val res = LocalClient.predictApiService.predict(body)
                    val responseString = res.string()
                    android.util.Log.d("MuseumViewModel", "CNN Predict raw response: $responseString")
                    
                    val jsonObject = org.json.JSONObject(responseString)
                    val resultsArray = jsonObject.optJSONArray("results")
                    encodedImageBase64 = jsonObject.optString("image", "").ifEmpty { null }

                    if (resultsArray != null && resultsArray.length() > 0) {
                        for (i in 0 until resultsArray.length()) {
                            val itemObj = resultsArray.optJSONObject(i) ?: continue
                            val cnnIdObj = itemObj.opt("cnn_id")
                            val cnnId = cnnIdObj?.toString()?.toIntOrNull() ?: 0
                            val cnnLabel = itemObj.optString("cnn_label", "")
                            val cnnConf = itemObj.optDouble("cnn_confidence", 0.0).toFloat()
                            val yoloLabel = itemObj.optString("yolo_label", "")
                            val yoloConf = itemObj.optDouble("yolo_confidence", 0.0).toFloat()
                            
                            val bboxArray = itemObj.optJSONArray("bbox")
                            val bbox = mutableListOf<Int>()
                            if (bboxArray != null) {
                                for (j in 0 until bboxArray.length()) {
                                    bbox.add(bboxArray.optInt(j))
                                }
                            }

                            val artId = nodeIdToArtifactId(cnnId)
                            val artifact = _uiState.value.artifacts.find { it.id == artId }
                            val finalName = if (artifact != null) artifact.name else if (cnnLabel.isNotBlank()) cnnLabel else "Hiện vật #$artId"
                            val desc = artifact?.description ?: ""

                            detectedList.add(
                                DetectedObjectItem(
                                    yoloLabel = yoloLabel,
                                    yoloConfidence = yoloConf,
                                    cnnId = cnnId,
                                    cnnLabel = if (cnnLabel.isNotBlank()) cnnLabel else finalName,
                                    cnnConfidence = cnnConf,
                                    bbox = bbox,
                                    artifactId = artId,
                                    artifactName = finalName,
                                    artifactDescription = desc
                                )
                            )
                        }
                    } else {
                        errorDetail = jsonObject.optString("message", "Không tìm thấy đối tượng nào trong ảnh.")
                    }
                } catch (e: retrofit2.HttpException) {
                    val code = e.code()
                    val errorBody = e.response()?.errorBody()?.string() ?: ""
                    android.util.Log.e("MuseumViewModel", "CNN Prediction HTTP error: $code - $errorBody", e)
                    errorDetail = "HTTP Lỗi $code: ${if (errorBody.length > 100) errorBody.take(100) + "..." else errorBody}"
                } catch (e: java.net.ConnectException) {
                    android.util.Log.e("MuseumViewModel", "CNN Prediction connect error", e)
                    errorDetail = "Không thể kết nối đến Máy chủ (Connection Refused). Địa chỉ IP: ${LocalClient.getBaseIp()}:8001. Hãy kiểm tra xem máy chủ có đang chạy và điện thoại có kết nối cùng mạng Wifi hay không."
                } catch (e: java.net.SocketTimeoutException) {
                    android.util.Log.e("MuseumViewModel", "CNN Prediction timeout error", e)
                    errorDetail = "Kết nối đến Máy chủ bị quá hạn (Timeout). Địa chỉ IP: ${LocalClient.getBaseIp()}:8001. Vui lòng kiểm tra đường truyền."
                } catch (e: java.net.UnknownHostException) {
                    android.util.Log.e("MuseumViewModel", "CNN Prediction unknown host error", e)
                    errorDetail = "Địa chỉ máy chủ không hợp lệ (Unknown Host): ${LocalClient.getBaseIp()}"
                } catch (e: Exception) {
                    android.util.Log.e("MuseumViewModel", "CNN Prediction failed with exception: ", e)
                    errorDetail = e.localizedMessage ?: e.toString()
                }
            }

            _uiState.value = _uiState.value.copy(uploading = false)

            if (detectedList.size == 1) {
                // Single object detected - proceed directly as normal
                val singleItem = detectedList[0]
                initializeGamePath(singleItem.cnnId)
                showTopNotification("🎯 Đã nhận diện: ${singleItem.artifactName}! Khởi tạo lộ trình thành công.", "success")
            } else if (detectedList.size > 1) {
                // Multiple objects detected - prompt user to select
                _uiState.value = _uiState.value.copy(
                    showMultiObjectSelectionDialog = true,
                    detectedObjectsList = detectedList,
                    multiObjectDetectionImageBase64 = encodedImageBase64,
                    multiObjectSourceTarget = "start"
                )
                showTopNotification("🔍 Phát hiện ${detectedList.size} hiện vật trong ảnh. Vui lòng chọn hiện vật!", "info")
            } else {
                val failMsg = if (errorDetail != null) {
                    "<strong>LỖI KẾT NỐI MÁY CHỦ:</strong> Không thể nhận dạng được hiện vật trong ảnh.<br><br><strong>Chi tiết lỗi:</strong> <code>$errorDetail</code><br><br>Vui lòng kiểm tra cấu hình IP máy chủ và kết nối mạng để thử lại!"
                } else {
                    "Hệ thống không nhận dạng được hiện vật trong bức ảnh hoặc không kết nối được máy chủ định vị. Vui lòng chụp rõ góc cạnh hơn và chụp ở môi trường đủ sáng!"
                }
                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + ChatMessage(
                        MessageSender.AI,
                        failMsg
                    )
                )
                showTopNotification("Không nhận diện được hiện vật. Vui lòng thử lại!", "error")
            }
        }
    }

    // User chooses an object from the multi-object dialog
    fun selectDetectedObject(item: DetectedObjectItem) {
        _uiState.value = _uiState.value.copy(showMultiObjectSelectionDialog = false)
        if (_uiState.value.multiObjectSourceTarget == "start") {
            initializeGamePath(item.cnnId)
            showTopNotification("🎯 Đã chọn: ${item.artifactName}. Khởi tạo lộ trình tham quan thành công!", "success")
        } else {
            processChatForDetectedArtifact(item.cnnId, _uiState.value.pendingChatImageText)
            showTopNotification("🎯 Đang thuyết minh về: ${item.artifactName}", "info")
        }
    }

    // Dismiss the multi-object dialog
    fun dismissMultiObjectDialog() {
        _uiState.value = _uiState.value.copy(showMultiObjectSelectionDialog = false)
    }

    // Retry capture or upload from multi-object dialog
    fun retryMultiObjectCapture() {
        val target = _uiState.value.multiObjectSourceTarget
        _uiState.value = _uiState.value.copy(showMultiObjectSelectionDialog = false)
        setImageSourceDialog(true, target)
    }

    fun completeGame(artifactId: Int, gameScore: Int = 0) {
        val currentCompleted = _uiState.value.completedArtifactIds
        val currentDeferred = _uiState.value.deferredArtifactIds
        val currentDiscovered = _uiState.value.discoveredArtifactIds
        
        val newCompleted = currentCompleted + artifactId
        val newDeferred = currentDeferred - artifactId
        val newDiscovered = currentDiscovered + artifactId

        _uiState.value = _uiState.value.copy(
            completedArtifactIds = newCompleted,
            deferredArtifactIds = newDeferred,
            discoveredArtifactIds = newDiscovered
        )
        persistState()
        
        // Also mark current stage cleared if playing game for active artifact
        val activeNodeId = _uiState.value.lockedHamiltonPath.getOrNull(_uiState.value.currentPathIndex) ?: -1
        val activeArtifactId = nodeIdToArtifactId(activeNodeId)
        if (activeArtifactId == artifactId) {
            val correctKey = cryptoKeysPool[activeNodeId] ?: "SUCCESS"
            sendChatMessage(correctKey, gameScore = gameScore)
        } else {
            onStageCleared(artifactId)
        }
    }

    // THESIS UPGRADE: Collected Images
    fun collectImage(artifactId: Int, base64Image: String) {
        val current = _uiState.value
        val artifactImages = current.collectedImages[artifactId] ?: emptyList()
        if (artifactImages.size < 4) {
            val cleanImage = base64Image.ifBlank { "MẢNH_ẢNH_DI_SẢN_${artifactId}_${artifactImages.size + 1}" }
            if (!artifactImages.contains(cleanImage)) {
                val newImages = artifactImages + cleanImage
                _uiState.value = current.copy(
                    collectedImages = current.collectedImages + (artifactId to newImages)
                )
                persistState()
                triggerCardCollectAnimation(
                    imageStr = cleanImage,
                    artifactName = getArtifactNameById(artifactId),
                    pieceIndex = newImages.size - 1,
                    artifactId = artifactId
                )
            }
        }
    }

    fun triggerCardCollectAnimation(imageStr: String, artifactName: String, pieceIndex: Int, artifactId: Int) {
        _uiState.value = _uiState.value.copy(
            activeCollectedCard = CardCollectData(imageStr, artifactName, pieceIndex, artifactId)
        )
    }

    fun dismissCardCollectAnimation() {
        _uiState.value = _uiState.value.copy(activeCollectedCard = null)
    }

    fun openCartDialog() {
        _uiState.value = _uiState.value.copy(showCartDialog = true)
    }

    fun closeCartDialog() {
        _uiState.value = _uiState.value.copy(showCartDialog = false)
    }

    fun resetCart() {
        _uiState.value = _uiState.value.copy(collectedImages = emptyMap())
        persistState()
    }

    fun resetGameSession() {
        _uiState.value = _uiState.value.copy(
            completedArtifactIds = emptySet(),
            deferredArtifactIds = emptySet(),
            discoveredArtifactIds = emptySet(),
            collectedImages = emptyMap(),
            stationHistoryLog = emptyMap(),
            currentPathIndex = 0,
            currentStageCleared = false,
            totalScore = 0,
            currentScore = 0
        )
        persistState()
        showTopNotification("🔄 Đã reset toàn bộ hành trình & giỏ hàng về ban đầu!", "info")
    }

    fun markStageIncomplete(artifactId: Int) {
        val current = _uiState.value
        val newCompleted = current.completedArtifactIds - artifactId
        _uiState.value = current.copy(
            completedArtifactIds = newCompleted,
            currentStageCleared = false
        )
        persistState()
    }

    fun unlockAndPlayStageWithPoints(artifactId: Int): Boolean {
        val current = _uiState.value
        val totalAvailable = current.currentScore + current.totalScore
        val isAlreadyCompleted = current.completedArtifactIds.contains(artifactId)
        if (totalAvailable >= 100 || isAlreadyCompleted) {
            val cost = if (isAlreadyCompleted) 0 else 100
            val newCurrentScore = (current.currentScore - cost).coerceAtLeast(0)
            val remainderToDeductFromTotal = if (current.currentScore < cost) cost - current.currentScore else 0
            val newTotalScore = (current.totalScore - remainderToDeductFromTotal).coerceAtLeast(0)
            _uiState.value = current.copy(
                totalScore = newTotalScore,
                currentScore = newCurrentScore,
                currentArtifactId = artifactId,
                currentArtifactName = getArtifactNameById(artifactId),
                currentStageCleared = false
            )
            val msg = if (isAlreadyCompleted) {
                "🔄 Kích hoạt chơi lại ải cổ vật #${artifactId} (${getArtifactNameById(artifactId)})!"
            } else {
                "🚀 Đã dùng 100 điểm mở khóa trực tiếp ải cổ vật #${artifactId}!"
            }
            showTopNotification(msg, "success")
            navigateTo(AppScreen.Game(artifactId))
            return true
        } else {
            showTopNotification("⚠️ Bạn cần tối thiểu 100 điểm để mở khóa chơi ải này!", "warning")
            return false
        }
    }

    private fun getArtifactNameById(id: Int): String {
        return _uiState.value.artifacts.find { it.id == id }?.name ?: "Di sản cổ vật #$id"
    }

    fun collectImagesForArtifact(artifactId: Int, base64List: List<String>) {
        if (base64List.isEmpty()) return
        val current = _uiState.value
        _uiState.value = current.copy(
            collectedImages = current.collectedImages + (artifactId to base64List.take(4))
        )
        persistState()
    }

    fun buyArtifactImagesWithPoints(artifactId: Int): Boolean {
        val current = _uiState.value
        if (current.totalScore >= 100) {
            val existing = current.collectedImages[artifactId] ?: emptyList()
            if (existing.size < 4) {
                val placeholderImages = listOf(
                    "MẢNH_1_AUTOGEN", "MẢNH_2_AUTOGEN", "MẢNH_3_AUTOGEN", "MẢNH_4_AUTOGEN"
                )
                _uiState.value = current.copy(
                    totalScore = (current.totalScore - 100).coerceAtLeast(0),
                    currentScore = (current.currentScore - 100).coerceAtLeast(0),
                    collectedImages = current.collectedImages + (artifactId to placeholderImages)
                )
                persistState()
                return true
            }
        }
        return false
    }

    fun setCurrentLevelImages(images: List<String>) {
        _uiState.value = _uiState.value.copy(currentLevelImages = images)
    }

    // THESIS UPGRADE: Puzzle Completion
    fun solvePuzzle(artifactId: Int) {
        _uiState.value = _uiState.value.copy(
            puzzleSolved = _uiState.value.puzzleSolved + (artifactId to true)
        )
    }

    // THESIS UPGRADE: Award Puzzle Bonus Points
    fun awardPuzzleBonus(points: Int) {
        _uiState.value = _uiState.value.copy(
            totalScore = _uiState.value.totalScore + points,
            currentScore = _uiState.value.currentScore + points
        )
    }

    fun setPuzzleArtifactId(artifactId: Int?) {
        _uiState.value = _uiState.value.copy(puzzleArtifactId = artifactId)
    }

    fun showTopNotification(message: String, type: String = "info") {
        _uiState.value = _uiState.value.copy(
            topNotificationMessage = message,
            topNotificationType = type
        )
        viewModelScope.launch {
            delay(4000)
            if (_uiState.value.topNotificationMessage == message) {
                _uiState.value = _uiState.value.copy(topNotificationMessage = null)
            }
        }
    }

    fun dismissTopNotification() {
        _uiState.value = _uiState.value.copy(topNotificationMessage = null)
    }

    // THESIS UPGRADE: Retry stage penalty
    fun spendPointsForRetry(): Boolean {
        val current = _uiState.value
        val totalAvailable = current.currentScore + current.totalScore
        if (totalAvailable >= 100) {
            val newCurrentScore = (current.currentScore - 100).coerceAtLeast(0)
            val remainderToDeductFromTotal = if (current.currentScore < 100) 100 - current.currentScore else 0
            val newTotalScore = (current.totalScore - remainderToDeductFromTotal).coerceAtLeast(0)
            _uiState.value = current.copy(
                totalScore = newTotalScore,
                currentScore = newCurrentScore,
                currentStageCleared = false
            )
            return true
        }
        return false
    }

    fun deferStage(artifactId: Int) {
        val activeNodeId = _uiState.value.lockedHamiltonPath.getOrNull(_uiState.value.currentPathIndex) ?: -1
        val updatedHistory = _uiState.value.stationHistoryLog.toMutableMap()
        if (activeNodeId != -1) {
            val elapsed = _uiState.value.secondsElapsedInStage
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = dateFormat.format(Date())
            updatedHistory[activeNodeId] = StationHistory(timeSpent = elapsed, clearedAt = timeString, scoreSpent = 0)
        }

        val currentDeferred = _uiState.value.deferredArtifactIds
        val currentDiscovered = _uiState.value.discoveredArtifactIds
        val currentCompleted = _uiState.value.completedArtifactIds

        val clue = getClueForArtifact(artifactId)
        val newChatMessage = ChatMessage(
            MessageSender.AI,
            "📡 Tín hiệu thu được: $clue",
            suggestions = listOf("Tiếp tục hành trình", "Kiểm tra nhật ký")
        )

        _uiState.value = _uiState.value.copy(
            deferredArtifactIds = currentDeferred + artifactId,
            discoveredArtifactIds = currentDiscovered + artifactId,
            completedArtifactIds = currentCompleted - artifactId,
            stationHistoryLog = updatedHistory,
            currentStageCleared = true,
            chatMessages = _uiState.value.chatMessages + newChatMessage
        )
        persistState()
        showTopNotification("🟡 Đã tạm qua ải cổ vật #$artifactId! Ải hiển thị màu VÀNG trên sơ đồ bản đồ.", "info")
    }

    fun getClueForArtifact(artifactId: Int): String {
        return when (artifactId) {
            1 -> "...đơn vị rút lui và tránh không kích phá hoại..."
            2 -> "...phối hợp phòng không và bắn hạ pháo đài bay..."
            3 -> "...bằng phương tiện thô sơ xuồng ba lá dọc miền sông nước..."
            4 -> "...thiết lập căn cứ sâu trong hầm bí mật nuôi giấu cán bộ..."
            5 -> "...gia cố chiến hào và nóc mái hầm chống pháo mảnh..."
            6 -> "...bí mật phát tán hàng ngàn tờ truyền đơn kêu gọi kháng chiến..."
            7 -> "...mũi vận tải biển không số tiến công chi viện..."
            8 -> "...kéo hỏa lực pháo binh xuyên dốc núi hiểm trở..."
            9 -> "...đúc vũ khí đồng kiên cố bảo vệ duyên hải cổ xưa..."
            10 -> "...bắn rơi máy bay chiến lược chìm sâu dưới hồ Hữu Tiệp..."
            11 -> "...tuần tra chặn đứng các mũi đột kích sông lạch..."
            12 -> "...đơn vị thiết giáp đột phá thọc sâu phòng tuyến..."
            13 -> "...mật vụ ngụy trang đưa đón cán bộ trong lòng nội thành..."
            14 -> "...húc đổ cổng chính dinh độc lập ngày thống nhất..."
            15 -> "...cho thấy nỗ lực di tản thất bại của địch..."
            else -> "...manh mối di sản quân khu..."
        }
    }

    fun deductPointsForHint(pointsRequired: Int = 100): Boolean {
        val current = _uiState.value
        val totalAvailable = current.currentScore + current.totalScore
        if (totalAvailable < pointsRequired) {
            return false
        }
        val newCurrentScore = (current.currentScore - pointsRequired).coerceAtLeast(0)
        val remainderToDeductFromTotal = if (current.currentScore < pointsRequired) pointsRequired - current.currentScore else 0
        val newTotalScore = (current.totalScore - remainderToDeductFromTotal).coerceAtLeast(0)
        _uiState.value = current.copy(
            totalScore = newTotalScore,
            currentScore = newCurrentScore
        )
        return true
    }

    fun skipStageWithPoints(artifactId: Int, pointsRequired: Int = 100): Boolean {
        val current = _uiState.value
        val totalAvailable = current.currentScore + current.totalScore
        if (totalAvailable < pointsRequired) {
            return false
        }
        
        val newCurrentScore = (current.currentScore - pointsRequired).coerceAtLeast(0)
        val remainderToDeductFromTotal = if (current.currentScore < pointsRequired) pointsRequired - current.currentScore else 0
        val newTotalScore = (current.totalScore - remainderToDeductFromTotal).coerceAtLeast(0)

        val activeNodeId = current.lockedHamiltonPath.getOrNull(current.currentPathIndex) ?: -1
        val updatedHistory = current.stationHistoryLog.toMutableMap()
        if (activeNodeId != -1) {
            val elapsed = current.secondsElapsedInStage
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = dateFormat.format(Date())
            updatedHistory[activeNodeId] = StationHistory(timeSpent = elapsed, clearedAt = timeString, scoreSpent = pointsRequired)
        }

        _uiState.value = current.copy(
            totalScore = newTotalScore,
            currentScore = newCurrentScore,
            stationHistoryLog = updatedHistory,
            currentStageCleared = true
        )
        
        completeGame(artifactId)
        return true
    }

    fun advanceNextStage() {
        if (_uiState.value.currentPathIndex >= _uiState.value.lockedHamiltonPath.size - 1) {
            _uiState.value = _uiState.value.copy(showSummaryModal = true)
            return
        }

        val nextIndex = _uiState.value.currentPathIndex + 1
        val nextNodeId = _uiState.value.lockedHamiltonPath[nextIndex]
        _uiState.value = _uiState.value.copy(
            currentPathIndex = nextIndex
        )
        setupCurrentStage(nextNodeId)
    }

    fun advanceNextStageAndNavigate() {
        if (_uiState.value.currentPathIndex >= _uiState.value.lockedHamiltonPath.size - 1) {
            _uiState.value = _uiState.value.copy(showSummaryModal = true)
            navigateTo(AppScreen.Dashboard)
            return
        }

        val nextIndex = _uiState.value.currentPathIndex + 1
        val nextNodeId = _uiState.value.lockedHamiltonPath[nextIndex]
        _uiState.value = _uiState.value.copy(
            currentPathIndex = nextIndex
        )
        setupCurrentStage(nextNodeId)
        
        val nextArtifactId = nodeIdToArtifactId(nextNodeId)
        navigateTo(AppScreen.Game(nextArtifactId))
    }

    fun triggerEarlyFinish() {
        val updatedHistory = _uiState.value.stationHistoryLog.toMutableMap()
        val activeNodeId = _uiState.value.lockedHamiltonPath.getOrNull(_uiState.value.currentPathIndex)
        
        // Add current ongoing station if not cleared yet
        if (activeNodeId != null && !updatedHistory.containsKey(activeNodeId)) {
            val elapsed = _uiState.value.secondsElapsedInStage
            val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = dateFormat.format(Date())
            updatedHistory[activeNodeId] = StationHistory(timeSpent = elapsed, clearedAt = timeString, scoreSpent = 0)
        }

        val totalScore = updatedHistory.values.sumOf { it.scoreSpent }
        val totalTime = updatedHistory.values.sumOf { it.timeSpent } + _uiState.value.decoderSecondsElapsed

        _uiState.value = _uiState.value.copy(
            totalScore = totalScore,
            totalElapsedTime = totalTime,
            showSummaryModal = true
        )
        timerJob?.cancel()
    }

    fun restartNewAdventure() {
        timerJob?.cancel()
        _uiState.value = MuseumUiState(
            currentScreen = AppScreen.Dashboard,
            isLoggedIn = true,
            loggedInUser = _uiState.value.loggedInUser
        )
    }

    fun closeSummary() {
        _uiState.value = _uiState.value.copy(showSummaryModal = false)
    }

    fun showRulesModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRankRules = show)
    }

    fun showRouteModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showRouteModal = show)
    }

    fun showLeaderboardModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLeaderboardModal = show)
    }

    fun showHistoryLog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showHistoryLog = show)
    }

    fun setIsMinimized(minimized: Boolean) {
        _uiState.value = _uiState.value.copy(isMinimized = minimized)
    }

    fun toggleSpeaking(speaking: Boolean, text: String = "") {
        _uiState.value = _uiState.value.copy(isSpeaking = speaking, activeSpeechText = text)
    }

    fun setChatSelectedImage(src: String?) {
        _uiState.value = _uiState.value.copy(chatSelectedImageSrc = src)
    }

    // Rank titles algorithm based on page 20-21 of PDF
    fun getHeroTitle(): String {
        val count = _uiState.value.stationHistoryLog.size
        val score = _uiState.value.totalScore
        val time = _uiState.value.totalElapsedTime

        if (count >= 15 && score >= 1200) {
            return "Anh Hùng Di Sản Toàn Lộ Trình"
        }
        if (count >= 10 || score >= 800) {
            return if (time in 1..599) "Huyền Thoại Tốc Biến Di Sản" else "Đại Sứ Di Sản Quân Khu 9"
        }
        if (count >= 5 || score >= 400) {
            return "Sĩ Quan Tham Mưu"
        }
        if (count > 0) {
            return "Chiến Sĩ Trinh Sát"
        }
        return "Tân Binh Nhập Ngũ"
    }

    fun formatTotalTime(seconds: Int): String {
        if (seconds <= 0) return "00p 00s"
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02dp %02ds", m, s)
    }

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    // Upgraded feature support: award letters and check sparse clues
    fun onStageCleared(artifactId: Int) {
        val state = _uiState.value
        val activePathIdx = state.currentPathIndex
        
        // 1. Award letters for the group
        val (groupId, letters) = getGroupAndLettersForArtifact(artifactId)
        val currentLetters = state.collectedLettersByGroup[groupId] ?: ""
        // Keep letters in order of collection or unique list
        val updatedLetters = (currentLetters + letters).toList().distinct().joinToString("")
        
        val newCollectedLetters = state.collectedLettersByGroup.toMutableMap()
        newCollectedLetters[groupId] = updatedLetters
        
        // 2. Also collect letter for the main secret group name puzzle
        val stageLetter = state.secretGroupLettersMap[activePathIdx] ?: ""
        val isNewStageLetter = stageLetter.isNotEmpty() && !state.collectedGroupLetters.contains(stageLetter)
        val newCollectedGroupLetters = if (stageLetter.isNotEmpty()) {
            state.collectedGroupLetters + stageLetter
        } else {
            state.collectedGroupLetters
        }
        
        // 3. Check if this level index is one of the clue stage indices
        val newCollectedClues = state.collectedClues.toMutableMap()
        var newlyCollectedClueText = ""
        if (state.clueStageIndices.contains(activePathIdx)) {
            val candidate = secretCandidatesList.getOrNull(state.secretArtifactId)
            if (candidate != null) {
                val rawClueIdx = state.clueStageIndices.indexOf(activePathIdx)
                val clueIdx = rawClueIdx.coerceIn(0, candidate.clues.size - 1)
                val cluePhrase = candidate.clues.getOrNull(clueIdx) ?: ""
                if (cluePhrase.isNotBlank() && !state.collectedClues.containsKey(activePathIdx)) {
                    newCollectedClues[activePathIdx] = cluePhrase
                    newlyCollectedClueText = cluePhrase
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(
            collectedLettersByGroup = newCollectedLetters,
            collectedGroupLetters = newCollectedGroupLetters,
            collectedClues = newCollectedClues
        )

        // Trigger congratulatory notification box
        if (newlyCollectedClueText.isNotEmpty()) {
            val msg = "🎉 CHÚC MỪNG BẠN ĐÃ THU THẬP ĐƯỢC GỢI Ý DI SẢN BÍ MẬT:\n\"$newlyCollectedClueText\""
            showTopNotification(msg, "success")
        } else if (isNewStageLetter) {
            val msg = "🎉 Chúc mừng bạn đã thu thập được từ khóa/manh mối di sản: '$stageLetter'!"
            showTopNotification(msg, "success")
        }
    }

    // Upgraded feature support: Guess the secret artifact
    fun guessSecretArtifact(guessText: String): Boolean {
        return guessSecretArtifact(guessText, emptyList()).first
    }

    fun guessSecretArtifact(guessText: String, puzzleSlots: List<String?>): Pair<Boolean, String> {
        val state = _uiState.value
        if (state.secretGuessCorrect) return Pair(true, "Đã bẻ khóa thành công từ trước!")
        if (state.decoderLockoutSecondsLeft > 0) return Pair(false, "Hệ thống tạm khóa! Vui lòng chờ ${state.decoderLockoutSecondsLeft}s.")
        
        val numCleared = state.completedArtifactIds.size
        if (numCleared < 15) {
            if (state.totalScore <= 150) {
                return Pair(false, "Cần có trên 150 điểm để thực hiện bẻ khóa!")
            }
        } else {
            val allowedGuesses = 3 + (if (state.help1Used) 1 else 0) + (if (state.help2Used) 1 else 0)
            if (state.post15GuessesUsed >= allowedGuesses) {
                return Pair(false, "Đã hết lượt bẻ khóa quy định!")
            }
        }
        
        val candidate = secretCandidatesList.getOrNull(state.secretArtifactId)
            ?: return Pair(false, "Không tìm thấy dữ liệu hiện vật bí mật!")
        
        // 1. Kiểm tra Từ khóa (tên hiện vật hoặc từ đồng nghĩa)
        val normalizedGuess = guessText.trim().lowercase(Locale.ROOT)
        val cleanGuess = stripAccents(normalizedGuess)
        val isNameCorrect = candidate.synonyms.any {
            stripAccents(it.lowercase(Locale.ROOT)) == cleanGuess || it.lowercase(Locale.ROOT) == normalizedGuess
        } || stripAccents(candidate.name.lowercase(Locale.ROOT)) == cleanGuess || candidate.name.lowercase(Locale.ROOT) == normalizedGuess

        // 2. Kiểm tra 4 tấm ảnh Puzzle ghép đúng thứ tự (mảnh 1, 2, 3, 4)
        val targetImages = state.collectedImages[candidate.id] ?: emptyList()
        val isSlotsFilled = puzzleSlots.size == 4 && puzzleSlots.all { !it.isNullOrEmpty() }
        
        val isImagesCorrect = if (isSlotsFilled && targetImages.size == 4) {
            puzzleSlots[0] == targetImages[0] &&
            puzzleSlots[1] == targetImages[1] &&
            puzzleSlots[2] == targetImages[2] &&
            puzzleSlots[3] == targetImages[3]
        } else {
            false
        }

        // 3. Quy tắc ĐÚNG: Phải ĐỦ 2 ĐIỀU KIỆN (Đúng tên VÀ Đúng 4 mảnh ảnh ghép đúng thứ tự)
        val overallSuccess = isNameCorrect && isImagesCorrect

        if (overallSuccess) {
            val multiplier = when {
                numCleared < 3 -> 5
                numCleared < 6 -> 4
                numCleared < 9 -> 3
                numCleared < 12 -> 2
                else -> 1
            }
            
            val baselineScore = if (state.totalScore > 0) state.totalScore else 100
            val bonusScore = baselineScore * multiplier
            
            val newState = state.copy(
                secretGuessCorrect = true,
                secretGuessMultiplier = multiplier,
                secretGuessScoreBonus = bonusScore,
                totalScore = bonusScore,
                currentScore = bonusScore,
                showSummaryModal = true
            )
            _uiState.value = updateGuessAttempts(newState)
            timerJob?.cancel()
            decoderTimerJob?.cancel()
            playFeedbackTone(1500, 600) // Correct guess triumph sound!
            return Pair(true, "🎉 CHÍNH XÁC HOÀN HẢO! Đúng tên hiện vật & ghép chuẩn 4 mảnh ảnh theo thứ tự của ${candidate.name}!")
        } else {
            // Thất bại: Xử lý trừ điểm / trừ lượt bẻ khóa & phạt khóa 12s
            val newTotalScore = if (numCleared < 15) {
                (state.totalScore - 150).coerceAtLeast(0)
            } else {
                state.totalScore
            }
            
            val newPost15GuessesUsed = if (numCleared >= 15) {
                state.post15GuessesUsed + 1
            } else {
                state.post15GuessesUsed
            }
            
            val newIncorrectCount = state.incorrectGuessesCount + 1
            
            val newState = state.copy(
                totalScore = newTotalScore,
                currentScore = newTotalScore,
                post15GuessesUsed = newPost15GuessesUsed,
                incorrectGuessesCount = newIncorrectCount,
                decoderLockoutSecondsLeft = 12
            )
            _uiState.value = updateGuessAttempts(newState)
            startDecoderTimer()
            playFeedbackTone(200, 350)
            
            val errorDetail = when {
                !isNameCorrect && !isImagesCorrect -> 
                    "❌ SAI CẢ 2 ĐIỀU KIỆN! Sai tên hiện vật VÀ chưa ghép đúng 4 mảnh ảnh của hiện vật bí mật theo thứ tự 1-4."
                isNameCorrect && !isImagesCorrect -> 
                    "❌ CHƯA ĐỦ ĐIỀU KIỆN: Tên hiện vật chính xác nhưng 4 mảnh ảnh trên Bảng Ghép chưa đủ hoặc chưa đúng thứ tự 1-4 của ${candidate.name}!"
                else -> 
                    "❌ CHƯA ĐỦ ĐIỀU KIỆN: 4 mảnh ảnh ghép đã đúng thứ tự nhưng tên hiện vật bí mật dự đoán bị sai!"
            }
            
            return Pair(false, errorDetail)
        }
    }

    private fun updateGuessAttempts(state: MuseumUiState): MuseumUiState {
        val numCleared = state.completedArtifactIds.size
        val remaining = if (numCleared < 15) {
            999
        } else {
            val allowedGuesses = 3 + (if (state.help1Used) 1 else 0) + (if (state.help2Used) 1 else 0)
            (allowedGuesses - state.post15GuessesUsed).coerceAtLeast(0)
        }
        return state.copy(guessRemainingAttempts = remaining)
    }
}

// Support definitions for the Grouping and Guessing Upgrades
data class SecretCandidate(
    val id: Int,
    val name: String,
    val synonyms: List<String>,
    val clues: List<String>
)

val secretCandidatesList = listOf(
    SecretCandidate(
        id = 1,
        name = "Bom Mỹ",
        synonyms = listOf("bom my", "bom", "bom mỹ", "bom chùm", "bom chum"),
        clues = listOf(
            "Bom Mỹ tại QK9",
            "Sản xuất 1960-1975 từ Hoa Kỳ",
            "Mẫu bom chùm Mỹ chứa hàng trăm bom con",
            "Sức sát thương diện rộng nguy cơ bom mìn sót",
            "Công binh rà phá vô hiệu hóa lưu giữ bảo tàng"
        )
    ),
    SecretCandidate(
        id = 2,
        name = "Bệ đạn tên lửa",
        synonyms = listOf("be dan ten lua", "be dan", "ten lua", "bệ đạn tên lửa", "bệ đạn", "tên lửa", "sam"),
        clues = listOf(
            "Bệ phóng tên lửa phòng không bảo vệ bầu trời",
            "Chế tạo thép chịu lực tầm bắn phòng không cao",
            "Thu giữ và sử dụng trong chiến dịch lớn",
            "Trang bị khí tài quân sự hiện đại thế kỷ 20",
            "Lưu giữ tại khu trưng bày khí tài bảo tàng"
        )
    ),
    SecretCandidate(
        id = 3,
        name = "Ghe xuồng thuyền",
        synonyms = listOf("ghe xuong thuyen", "ghe thuyen", "xuong", "ghe", "thuyen", "ghe xuồng thuyền", "ghe xuồng", "xuồng"),
        clues = listOf(
            "Ghe thuyền gỗ tre địa phương thô sơ dã chiến",
            "Thu tháng 3 năm 1963 phục vụ U Minh Thượng",
            "Trận Sóng Tình Thương và Thuyền M4-T6 thu 1970",
            "Đưa đón cán bộ mật vận chuyển vũ khí tiếp tế",
            "Ngụy trang bùn đất cơ động kênh rạch sông nước"
        )
    ),
    SecretCandidate(
        id = 4,
        name = "Lu hầm bí mật",
        synonyms = listOf("lu ham bi mat", "lu ham", "hầm bí mật", "lu hầm bí mật", "lu hầm", "hầm"),
        clues = listOf(
            "Lu hầm đất nung bền bỉ chế tạo năm 1970 tại U Minh",
            "Ngụy trang lu nước chôn sâu dưới đất bảo vệ cán bộ",
            "Căn cứ bí mật được Đại tướng Lê Đức Anh tin dùng",
            "Cất giấu tài liệu mật lưu trữ vũ khí an toàn",
            "Thiết kế hệ thống thông hơi mưu trí che mắt kẻ thù"
        )
    ),
    SecretCandidate(
        id = 5,
        name = "Máy cán tôn",
        synonyms = listOf("may can ton", "may can tol", "máy cán tôn", "máy cán tol", "máy cán tôn qk9"),
        clues = listOf(
            "Chế tạo tại Việt Nam thời kháng chiến chống Mỹ",
            "Xưởng quân giới QK9 vận hành bằng tay quay không dùng điện",
            "Khung thép gang chế tạo vỏ hộp đạn sửa chữa trang bị",
            "Gia công kim loại tấm xây dựng công sự hầm dã chiến",
            "Tinh thần tự lực vượt khó tận dụng sắt phế bền bỉ"
        )
    ),
    SecretCandidate(
        id = 6,
        name = "Máy in Pédal",
        synonyms = listOf("may in pedal", "may in", "máy in pedal", "máy in", "pedal"),
        clues = listOf(
            "Gốc Pháp, Trung Quốc giai đoạn 1965-1975 bền bỉ",
            "Hoạt động bằng bàn đạp chân không dùng điện năng dã chiến",
            "In báo chí cách mạng, truyền đơn tài liệu chỉ đạo mật",
            "Cơ sở in bí mật phục vụ tuyên truyền thông tin tư tưởng",
            "Khung kim loại chắc chắn gắn kết tình quân dân kháng chiến"
        )
    ),
    SecretCandidate(
        id = 7,
        name = "Mỏ neo tàu",
        synonyms = listOf("mo neo tau", "mo neo", "neo tau", "mỏ neo tàu", "mỏ neo", "neo tàu", "neo"),
        clues = listOf(
            "Mỏ neo chế tạo năm 1972 tại Việt Nam thép gang đúc",
            "Khối lượng cực nặng 125 kg bám chắc nền bùn sông",
            "Trang bị Lữ đoàn Công binh 25 làm nhiệm vụ Campuchia",
            "Chịu lực dòng chảy cố định tàu thuyền phương tiện",
            "Thiết kế hai cánh neo đảm bảo kỹ thuật hậu cần tốt"
        )
    ),
    SecretCandidate(
        id = 8,
        name = "Pháo",
        synonyms = listOf("phao", "khau phao", "pháo", "khẩu pháo", "sơn pháo", "cao xạ", "phao cao xa"),
        clues = listOf(
            "Sơn pháo 75mm Pháp tại Dinh Nguyễn Khoa Nam lịch sử",
            "Pháo 105mm Mỹ thu được trong trận Thầy Phó bắn Trà Nóc",
            "Pháo 76,2mm Liên Xô Sư đoàn 330 diệt quân Pôn Pốt",
            "Pháo 85mm Lữ đoàn 6 và pháo cao xạ 57mm Tiểu đoàn 226",
            "Sức mạnh hỏa lực QK9 bảo vệ biên giới và đất nước ta"
        )
    ),
    SecretCandidate(
        id = 9,
        name = "Súng thần công",
        synonyms = listOf("sung than cong", "than cong", "súng thần công", "thần công", "sung co"),
        clues = listOf(
            "Súng thần công đúc bằng đồng đúc thủ công xưa",
            "Trang bị phòng thủ đồn bốt ven biển sông rạch",
            "Bắn đạn gang tròn uy lực lớn thời triều Nguyễn",
            "Hiện vật quân sự cổ giá trị lịch sử đặc biệt",
            "Trưng bày tại sảnh chính lịch sử quân sự"
        )
    ),
    SecretCandidate(
        id = 10,
        name = "Trục máy B52",
        synonyms = listOf("truc may b52", "b52", "truc may", "trục máy b52", "trục máy b-52", "xac b52"),
        clues = listOf(
            "Bộ phận động cơ máy bay B-52 Hoa Kỳ bị bắn rơi",
            "Hợp kim siêu bền chịu nhiệt độ áp suất cực cao",
            "Chứng tích chiến thắng Điện Biên Phủ trên không",
            "Bộ đội ta thu gom từ xác máy bay địch",
            "Lưu giữ biểu tượng chiến công oanh liệt"
        )
    ),
    SecretCandidate(
        id = 11,
        name = "Tàu tuần tiễu PCF",
        synonyms = listOf("tau tuan tieu pcf", "tau pcf", "pcf", "tau tuan tieu", "tàu tuần tiễu pcf", "tàu pcf", "tàu tuần tiễu"),
        clues = listOf(
            "Tàu tuần tra cao tốc bằng nhôm sản xuất năm 1969 từ Hoa Kỳ",
            "Thu giữ tại Đồng Tâm vào đêm 30 tháng 4 lịch sử",
            "Biên chế Lữ đoàn 962 làm nhiệm vụ Campuchia 1979-1989",
            "Tuần tiễu ven biển kiểm soát tuyến sông kênh rạch",
            "Trang bị súng máy, chống ăn mòn nước mặn sông ngòi"
        )
    ),
    SecretCandidate(
        id = 12,
        name = "Xe bọc thép",
        synonyms = listOf("xe boc thep", "boc thep", "m113", "xe bọc thép", "bọc thép", "xe m113"),
        clues = listOf(
            "Xe bọc thép bánh xích lội nước cơ động cao",
            "Trang bị hỏa lực súng máy 12.7mm bảo vệ bộ binh",
            "Thu giữ của đối phương sau giải phóng 1975",
            "Phục vụ chiến đấu bảo vệ biên giới tây nam",
            "Trưng bày khối phương tiện cơ giới ngoài trời"
        )
    ),
    SecretCandidate(
        id = 13,
        name = "Xe Peugeot",
        synonyms = listOf("xe peugeot", "peugeot", "xe peugeot 1967", "xe cổ peugeot", "xe co peugeot"),
        clues = listOf(
            "Xe cổ sang trọng sản xuất năm 1967 nguồn gốc từ Pháp",
            "Khung thép chắc chắn động cơ xăng ổn định bền bỉ",
            "Vận chuyển cán bộ công tác hành chính nội đô miền Nam",
            "Thiết kế kiểu dáng châu Âu lịch lãm thời thượng xưa",
            "Chuyển đổi công năng phục vụ cách mạng liên lạc bí mật"
        )
    ),
    SecretCandidate(
        id = 14,
        name = "Xe tăng",
        synonyms = listOf("xe tang", "tang", "xe tăng", "tăng", "xe tăng 843", "xe tang 843"),
        clues = listOf(
            "Xe tăng chiến đấu chủ lực bọc thép bánh xích",
            "Trang bị pháo chính uy lực dẫn đầu đoàn quân giải phóng",
            "Đã tiến vào dinh lũy cuối cùng ngày 30/4/1975",
            "Biểu tượng sức mạnh đột kích lực lượng tăng thiết giáp",
            "Trưng bày tại vị trí trang trọng sân bảo tàng"
        )
    ),
    SecretCandidate(
        id = 15,
        name = "Máy bay trực thăng",
        synonyms = listOf("may bay truc thang", "truc thang", "máy bay trực thăng", "trực thăng", "uh1", "uh-1"),
        clues = listOf(
            "Trực thăng quân sự sản xuất năm 1971 tại Hoa Kỳ",
            "Động cơ tuabin khí thân hợp kim nhôm cơ động vùng lầy",
            "Vận chuyển binh lính, tiếp tế trinh sát chiến trường",
            "Cất hạ cánh thẳng, chỉ huy tác chiến cứu thương",
            "Cánh quạt chính lớn thu giữ sau 1975 trưng bày ngoài trời"
        )
    )
)

fun getGroupIdForArtifactId(artId: Int): Int {
    return when (artId) {
        1, 2, 10 -> 1
        11, 12, 14, 15 -> 2
        3, 7 -> 3
        4, 5, 6 -> 4
        8, 9, 13 -> 5
        else -> 2
    }
}

fun getGroupPuzzleName(groupId: Int): String {
    return when (groupId) {
        1 -> "VU KHI VA KHI TAI PHONG KHONG KHONG QUAN"
        2 -> "PHUONG TIEN CHIEN DAU HANG NANG VA CO GIOI"
        3 -> "VAN TAI THUY VA HAU CAN SONG NUOC DI SAN"
        4 -> "THONG TIN LIEN LAC VA NGUY TRANG BI MAT"
        5 -> "HOA LUC PHAO BINH VA XE CO DAC CHUNG"
        else -> "PHUONG TIEN CHIEN DAU HANG NANG VA CO GIOI"
    }
}

fun getGroupDisplayName(groupId: Int): String {
    return when (groupId) {
        1 -> "Vũ khí và Khí tài Phòng không - Không quân"
        2 -> "Phương tiện Chiến đấu Hạng nặng và Cơ giới"
        3 -> "Vận tải Thủy và Hậu cần Sông nước"
        4 -> "Thông tin Liên lạc và Ngụy trang Bí mật"
        5 -> "Hỏa lực Pháo binh và Xe cơ giới Đặc chủng"
        else -> "Nhóm Khác"
    }
}

fun getGroupAndLettersForArtifact(artifactId: Int): Pair<Int, String> {
    return when (artifactId) {
        1 -> Pair(2, "VUK")
        2 -> Pair(1, "PH")
        3 -> Pair(3, "HAUSONG")
        4 -> Pair(4, "THONG")
        5 -> Pair(4, "TIN")
        6 -> Pair(4, "NGUYTANG")
        7 -> Pair(3, "CANNUOC")
        8 -> Pair(5, "VANT")
        9 -> Pair(1, "ON")
        10 -> Pair(1, "GK")
        11 -> Pair(2, "HIPH")
        12 -> Pair(1, "TEN")
        13 -> Pair(5, "AIDONGCO")
        14 -> Pair(1, "LUA")
        15 -> Pair(2, "UONGTAY")
        else -> Pair(1, "")
    }
}

fun getGroupName(groupId: Int): String {
    return getGroupDisplayName(groupId)
}
