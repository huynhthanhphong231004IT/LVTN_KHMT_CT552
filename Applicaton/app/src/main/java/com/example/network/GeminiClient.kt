package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val DEFAULT_API_KEY = "AQ.Ab8RN6J-G7AR54aogcy2XBNlXktK-KSF80O6KXQLYIz-n2Q5Vw"

    private const val MUSEUM_GROUNDING_CONTEXT = """
        RÀNG BUỘC PHẢN HỒI VÀ THÔNG TIN BẢO TÀNG BẮC BUỘC:
        - Đơn vị: Bảo tàng Quân khu 9 - Cần Thơ.
        - Địa chỉ chính xác: Đại lộ Hòa Bình, Phường Tân An, Quận Ninh Kiều, Thành phố Cần Thơ.
        - Vai trò AI: Bạn là Hướng dẫn viên AI và Đại sứ Di sản Lạc Việt chính thức của Bảo tàng Quân khu 9 - Cần Thơ.
        - QUY TẮC PHẢN HỒI (BẮC BUỘC):
          1. Trả lời ĐÚNG TRỌNG TÂM câu hỏi, KHÔNG dài dòng vòng vo, KHÔNG chào hỏi dư thừa rườm rà.
          2. Trình bày RÕ RÀNG, ĐẸP MẮT với tiêu đề in đậm dùng thẻ <strong> hoặc dấu gạch đầu dòng (•). Ví dụ: <strong>🏛️ Bảo tàng QK9:</strong>, <strong>📍 Vị trí:</strong>, <strong>📜 Lịch sử & Ý nghĩa:</strong>.
          3. Giới hạn độ dài 2-4 ý chính súc tích, mang tính giáo dục hào hùng và giàu giá trị di sản.
    """

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    suspend fun getChatbotResponse(prompt: String, systemPrompt: String): String {
        var apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            apiKey = DEFAULT_API_KEY
        }

        val fullSystemInstruction = "$MUSEUM_GROUNDING_CONTEXT\n\n$systemPrompt".trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = fullSystemInstruction)))
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Rất tiếc, tôi chưa thể tìm ra lời giải đáp thích hợp vào lúc này. Bạn có muốn hỏi thông tin khác về Bảo tàng Quân khu 9 (Đại lộ Hòa Bình, Ninh Kiều, Cần Thơ) không?"
        } catch (e: Exception) {
            "Hệ thống bận hoặc có lỗi kết nối mạng: ${e.localizedMessage}. Đừng lo, bạn vẫn có thể chơi game và khám phá các cổ vật Bảo tàng Quân khu 9 theo định vị!"
        }
    }
}
