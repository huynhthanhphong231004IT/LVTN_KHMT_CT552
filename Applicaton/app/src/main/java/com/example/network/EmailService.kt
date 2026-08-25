package com.example.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object EmailService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .writeTimeout(1500, TimeUnit.MILLISECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun buildEmailBody(
        userName: String,
        ticketCode: String,
        targetEmail: String,
        totalScore: Int,
        heroTitle: String,
        playTimeFormatted: String,
        totalVisited: Int,
        totalSkipped: Int
    ): String {
        val nameToUse = if (userName.isBlank()) "bạn" else userName
        val senderEmail = "phongb2207555@student.ctu.edu.vn"
        return """
Ban Quản lý Bảo tàng Quân khu 9 xin trân trọng cảm ơn $nameToUse đã dành thời gian tham quan Bảo tàng.

Thông tin vé: $ticketCode
Email nhận báo cáo: $targetEmail

Kết quả tham quan của bạn đạt được:
• Tổng điểm: $totalScore điểm
• Danh hiệu vinh danh: $heroTitle
• Thời gian tham quan: $playTimeFormatted
• Số di sản đã hoàn thành: $totalVisited / 15
• Số di sản bỏ qua: $totalSkipped

Trân trọng cảm ơn và hẹn gặp lại bạn trong thời gian gần nhất!

---
BAN QUẢN LÝ BẢO TÀNG QUÂN KHU 9
Email hỗ trợ: $senderEmail
        """.trimIndent()
    }

    suspend fun sendRealEmail(
        targetEmail: String,
        userName: String,
        ticketCode: String,
        totalScore: Int,
        playTimeFormatted: String,
        totalVisited: Int,
        totalSkipped: Int,
        heroTitle: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (targetEmail.isBlank() || !targetEmail.contains("@")) {
            Log.e("EmailService", "Invalid target email: $targetEmail")
            return@withContext false
        }

        val nameToUse = if (userName.isBlank()) "bạn" else userName
        val subject = "KẾT QUẢ THAM QUAN BẢO TÀNG - $nameToUse"
        val senderEmail = "phongb2207555@student.ctu.edu.vn"
        val emailBody = buildEmailBody(userName, ticketCode, targetEmail, totalScore, heroTitle, playTimeFormatted, totalVisited, totalSkipped)

        var success = false

        // 1. Dispatch to Local API ports first, prioritizing 8004 (/send-email)
        try {
            val baseIp = LocalClient.getBaseIp().removeSuffix("/")
            val ports = listOf(
                "8004",
                LocalClient.getQuestionPort(),
                LocalClient.getChatPort(),
                LocalClient.getLeaderboardPort(),
                LocalClient.getPredictPort(),
                LocalClient.getDecryptPort(),
                LocalClient.getImagePort(),
                "8000", "8001", "8002", "8003", "8005", "8006", "8080"
            ).distinct()

            val serverPayload = JSONObject().apply {
                put("from_email", senderEmail)
                put("from", senderEmail)
                put("to", targetEmail)
                put("subject", subject)
                put("body", emailBody)
                put("html_body", emailBody)
                put("plain_text", emailBody)
            }.toString()

            for (port in ports) {
                val url = "$baseIp:$port/send-email".replace(":/", "://").replace("([^:])//+".toRegex(), "$1/")
                try {
                    val req = Request.Builder()
                        .url(url)
                        .post(serverPayload.toRequestBody(JSON_MEDIA_TYPE))
                        .build()
                    client.newCall(req).execute().use { res ->
                        if (res.isSuccessful) {
                            Log.d("EmailService", "Successfully sent email via backend server at $url")
                            success = true
                        }
                    }
                } catch (e: Exception) {
                    Log.d("EmailService", "Port $port /send-email failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("EmailService", "Error sending via Local Server API: ${e.message}")
        }

        // 2. Dispatch to FormSubmit as fallback
        try {
            val jsonObject = JSONObject().apply {
                put("_subject", subject)
                put("_replyto", senderEmail)
                put("_from_name", "Bảo Tàng Quân Khu 9 ($senderEmail)")
                put("Tên_khách_hàng", nameToUse)
                put("Email_nhận", targetEmail)
                put("Mã_vé", ticketCode)
                put("Tổng_điểm", "$totalScore điểm")
                put("Danh_hiệu", heroTitle)
                put("Thời_gian", playTimeFormatted)
                put("Di_sản_hoàn_thành", "$totalVisited / 15")
                put("Nội_dung_báo_cáo", emailBody)
                put("_captcha", "false")
            }

            val jsonReq = Request.Builder()
                .url("https://formsubmit.co/ajax/$targetEmail")
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .post(jsonObject.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(jsonReq).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d("EmailService", "Successfully dispatched JSON email via FormSubmit to $targetEmail")
                    success = true
                }
            }
        } catch (e: Exception) {
            Log.e("EmailService", "Error sending via FormSubmit: ${e.message}")
        }

        return@withContext success
    }

    fun launchEmailAppIntent(
        context: Context,
        targetEmail: String,
        userName: String,
        ticketCode: String,
        totalScore: Int,
        playTimeFormatted: String,
        totalVisited: Int,
        totalSkipped: Int,
        heroTitle: String
    ) {
        try {
            val nameToUse = if (userName.isBlank()) "Du Khách" else userName
            val subject = "KẾT QUẢ THAM QUAN BẢO TÀNG - $nameToUse"
            val emailBody = buildEmailBody(userName, ticketCode, targetEmail, totalScore, heroTitle, playTimeFormatted, totalVisited, totalSkipped)

            val uri = Uri.parse("mailto:$targetEmail?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(emailBody))
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(targetEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Gửi Email Báo Cáo Kết Quả..."))
        } catch (e: Exception) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(targetEmail))
                    putExtra(Intent.EXTRA_SUBJECT, "KẾT QUẢ THAM QUAN BẢO TÀNG - $userName")
                    putExtra(Intent.EXTRA_TEXT, "BÁO CÁO KẾT QUẢ THAM QUAN BẢO TÀNG QUÂN KHU 9\nKính gửi Quý khách: $userName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Mở ứng dụng Gmail/Email..."))
            } catch (_: Exception) {
                Log.e("EmailService", "Cannot open mail client intent")
            }
        }
    }
}
