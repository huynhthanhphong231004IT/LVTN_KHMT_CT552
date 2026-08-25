package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class PredictResponse(
    @Json(name = "results") val results: List<PredictResult>?
)

@JsonClass(generateAdapter = true)
data class PredictResult(
    @Json(name = "cnn_id") val cnnId: String
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "responses") val responses: List<ChatResponseItem>?
)

@JsonClass(generateAdapter = true)
data class ChatResponseItem(
    @Json(name = "answer") val answer: String?,
    @Json(name = "suggestions") val suggestions: List<String>?
)

@JsonClass(generateAdapter = true)
data class LocalChatRequest(
    @Json(name = "message") val message: String,
    @Json(name = "label") val label: Int?
)

interface LocalPredictApiService {
    @Multipart
    @POST("predict")
    suspend fun predict(
        @Part file: MultipartBody.Part
    ): okhttp3.ResponseBody
}

interface LocalChatApiService {
    @POST("chat")
    suspend fun chat(
        @Body request: LocalChatRequest
    ): ChatResponse

    @POST("chat_2")
    suspend fun chat_2(
        @Body request: LocalChatRequest
    ): ChatResponse
}

@JsonClass(generateAdapter = true)
data class ApiQuestionResponse(
    @Json(name = "question") val question: ApiQuestion?
)

@JsonClass(generateAdapter = true)
data class ApiQuestion(
    @Json(name = "id") val id: Int?,
    @Json(name = "question") val question: String,
    @Json(name = "options") val options: List<String>?,
    @Json(name = "answer") val answer: String
)

interface LocalQuestionApiService {
    @retrofit2.http.GET
    suspend fun getRandomQuestion(
        @retrofit2.http.Url url: String
    ): ApiQuestionResponse
}

@JsonClass(generateAdapter = true)
data class DecryptRequest(
    @Json(name = "run_id") val runId: String,
    @Json(name = "wm_dir") val wmDir: String
)

@JsonClass(generateAdapter = true)
data class DecryptResponse(
    @Json(name = "answers") val answers: List<DecryptAnswer>?
)

@JsonClass(generateAdapter = true)
data class DecryptAnswer(
    @Json(name = "image") val image: String,
    @Json(name = "id") val id: String?
)

@JsonClass(generateAdapter = true)
data class LeaderboardItemDto(
    @Json(name = "name") val name: String,
    @Json(name = "title") val title: String = "",
    @Json(name = "score") val score: Int,
    @Json(name = "cleared") val cleared: Int = 0,
    @Json(name = "email") val email: String = "",
    @Json(name = "ticket") val ticket: String = ""
)

@JsonClass(generateAdapter = true)
data class LeaderboardResponse(
    @Json(name = "status") val status: String? = "success",
    @Json(name = "data") val data: List<LeaderboardItemDto>? = null
)

interface LocalLeaderboardApiService {
    @retrofit2.http.GET("leaderboard")
    suspend fun getLeaderboard(): LeaderboardResponse

    @POST("leaderboard")
    suspend fun submitScore(@Body entry: LeaderboardItemDto): LeaderboardResponse

    @retrofit2.http.GET("api/leaderboard")
    suspend fun getApiLeaderboard(): LeaderboardResponse

    @POST("api/leaderboard")
    suspend fun submitApiScore(@Body entry: LeaderboardItemDto): LeaderboardResponse
}

interface LocalDecryptApiService {
    @POST("game/{slug}")
    suspend fun decrypt(
        @retrofit2.http.Path("slug") slug: String,
        @Body request: DecryptRequest
    ): DecryptResponse
}

object LocalClient {
    // SINGLE SOURCE OF TRUTH FOR THE API HOST & PORTS (Change these values to update all endpoints)
    private var baseIp: String = "http://10.158.209.106"
    private var portChat: String = "8000"
    private var portPredict: String = "8001"
    private var portDecrypt: String = "8003"
    private var portQuestion: String = "8004"
    private var portImage: String = "8005"
    private var portLeaderboard: String = "8006"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    var predictApiService: LocalPredictApiService = createPredictService()
        private set

    var chatApiService: LocalChatApiService = createChatService()
        private set

    var questionApiService: LocalQuestionApiService = createQuestionService()
        private set

    var decryptApiService: LocalDecryptApiService = createDecryptService()
        private set

    var leaderboardApiService: LocalLeaderboardApiService = createLeaderboardService()
        private set

    fun getBaseIp(): String = baseIp

    fun getChatPort(): String = portChat
    fun getPredictPort(): String = portPredict
    fun getDecryptPort(): String = portDecrypt
    fun getQuestionPort(): String = portQuestion
    fun getImagePort(): String = portImage
    fun getLeaderboardPort(): String = portLeaderboard

    fun getChatUrl(): String = "${baseIp.removeSuffix("/")}:$portChat/"
    fun getPredictUrl(): String = "${baseIp.removeSuffix("/")}:$portPredict/"
    fun getDecryptUrl(): String = "${baseIp.removeSuffix("/")}:$portDecrypt/"
    fun getLeaderboardUrl(): String = "${baseIp.removeSuffix("/")}:$portLeaderboard/"
    fun getQuestionUrl(slug: String): String = "${baseIp.removeSuffix("/")}:$portQuestion/random-question-$slug"

    /**
     * Updates the base IP and all ports in a single, centralized source of truth.
     */
    fun updateApiSettings(
        newIp: String,
        newPortChat: String = "8000",
        newPortPredict: String = "8001",
        newPortDecrypt: String = "8003",
        newPortQuestion: String = "8004",
        newPortImage: String = "8005",
        newPortLeaderboard: String = "8006"
    ) {
        val trimmed = newIp.trim()
        if (trimmed.isNotEmpty()) {
            baseIp = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "http://$trimmed"
            } else {
                trimmed
            }
        }

        portChat = newPortChat.trim().ifEmpty { "8000" }
        portPredict = newPortPredict.trim().ifEmpty { "8001" }
        portDecrypt = newPortDecrypt.trim().ifEmpty { "8003" }
        portQuestion = newPortQuestion.trim().ifEmpty { "8004" }
        portImage = newPortImage.trim().ifEmpty { "8005" }
        portLeaderboard = newPortLeaderboard.trim().ifEmpty { "8006" }

        // Rebuild services
        predictApiService = createPredictService()
        chatApiService = createChatService()
        questionApiService = createQuestionService()
        decryptApiService = createDecryptService()
        leaderboardApiService = createLeaderboardService()
    }

    // Keep original updateBaseIp for backward compatibility
    fun updateBaseIp(newIp: String) {
        updateApiSettings(newIp, portChat, portPredict, portDecrypt, portQuestion, portImage)
    }

    private fun createPredictService(): LocalPredictApiService {
        return Retrofit.Builder()
            .baseUrl(getPredictUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LocalPredictApiService::class.java)
    }

    private fun createChatService(): LocalChatApiService {
        return Retrofit.Builder()
            .baseUrl(getChatUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LocalChatApiService::class.java)
    }

    private fun createQuestionService(): LocalQuestionApiService {
        return Retrofit.Builder()
            .baseUrl("${baseIp.removeSuffix("/")}:$portQuestion/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LocalQuestionApiService::class.java)
    }

    private fun createDecryptService(): LocalDecryptApiService {
        return Retrofit.Builder()
            .baseUrl(getDecryptUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LocalDecryptApiService::class.java)
    }

    private fun createLeaderboardService(): LocalLeaderboardApiService {
        return Retrofit.Builder()
            .baseUrl(getLeaderboardUrl())
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(LocalLeaderboardApiService::class.java)
    }

    fun getStageFolderName(artifactId: Int): String {
        return when (artifactId) {
            14 -> "XeTang"
            13 -> "xepeugeot"
            12 -> "xebocthep"
            11 -> "tautuantieupcf"
            10 -> "trucmaybayb52"
            9  -> "sungthancong"
            8  -> "phao"
            7  -> "moneotau"
            6  -> "mayinpedal"
            5  -> "maycantol"
            4  -> "luhambimat"
            3  -> "ghexuongthuyen"
            2  -> "bevadantenlua"
            1  -> "Bom"
            15 -> "maybaytructhang"
            else -> "XeTang"
        }
    }

    fun resolveImageUrl(imageStr: String, artifactId: Int): String? {
        if (imageStr.isEmpty()) return null
        if (imageStr.startsWith("http://") || imageStr.startsWith("https://")) {
            return imageStr
        }
        val lower = imageStr.lowercase().trim()
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp") || lower.endsWith(".gif")) {
            val stageFolder = getStageFolderName(artifactId)
            val ip = baseIp.removeSuffix("/")
            return "$ip:$portImage/images/$stageFolder/$imageStr"
        }
        return null
    }
}
