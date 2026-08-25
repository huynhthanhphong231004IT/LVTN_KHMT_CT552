package com.example.data

import com.example.network.LeaderboardItemDto
import com.example.network.LocalClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class RealLeaderboardEntry(
    val rank: Int = 0,
    val name: String,
    val title: String,
    val score: Int,
    val cleared: Int,
    val email: String = "",
    val ticket: String = "",
    val isUser: Boolean = false
)

object LeaderboardRepository {
    private val _realScores = MutableStateFlow<List<RealLeaderboardEntry>>(emptyList())
    val realScores: StateFlow<List<RealLeaderboardEntry>> = _realScores.asStateFlow()

    suspend fun submitUserScore(
        userName: String,
        heroTitle: String,
        score: Int,
        clearedStages: Int,
        userEmail: String,
        ticketCode: String
    ): List<RealLeaderboardEntry> = withContext(Dispatchers.IO) {
        val entry = RealLeaderboardEntry(
            name = if (userName.isBlank()) "Khách Tham Quan" else userName,
            title = heroTitle,
            score = score,
            cleared = clearedStages,
            email = userEmail,
            ticket = ticketCode,
            isUser = true
        )

        // Try API Port 8006 submission
        try {
            val dto = LeaderboardItemDto(
                name = entry.name,
                title = entry.title,
                score = entry.score,
                cleared = entry.cleared,
                email = entry.email,
                ticket = entry.ticket
            )
            try {
                LocalClient.leaderboardApiService.submitScore(dto)
            } catch (_: Exception) {
                LocalClient.leaderboardApiService.submitApiScore(dto)
            }
        } catch (_: Exception) {
            // Graceful fallback to local persistence if API port 8006 is unreachable
        }

        val currentList = _realScores.value.toMutableList()
        // Remove prior entry for same user name/email if exists
        currentList.removeAll { it.name.equals(entry.name, ignoreCase = true) || (entry.email.isNotEmpty() && it.email.equals(entry.email, ignoreCase = true)) }
        currentList.add(entry)

        val sorted = currentList.sortedByDescending { it.score }.mapIndexed { idx, item ->
            item.copy(rank = idx + 1)
        }

        _realScores.value = sorted
        return@withContext sorted
    }

    suspend fun fetchLeaderboard(currentUserName: String): List<RealLeaderboardEntry> = withContext(Dispatchers.IO) {
        try {
            val response = try {
                LocalClient.leaderboardApiService.getLeaderboard()
            } catch (_: Exception) {
                LocalClient.leaderboardApiService.getApiLeaderboard()
            }
            val remoteData = response.data
            if (!remoteData.isNullOrEmpty()) {
                val remoteEntries = remoteData.map { dto ->
                    RealLeaderboardEntry(
                        name = dto.name,
                        title = dto.title.ifBlank { "Thám Tử Cổ Vật" },
                        score = dto.score,
                        cleared = dto.cleared,
                        email = dto.email,
                        ticket = dto.ticket,
                        isUser = dto.name.equals(currentUserName, ignoreCase = true)
                    )
                }
                // Keep local user score if submitted and not yet in remoteData
                val localUserEntries = _realScores.value.filter { it.isUser && remoteEntries.none { r -> r.name.equals(it.name, ignoreCase = true) } }
                val merged = (localUserEntries + remoteEntries)
                    .distinctBy { it.name }
                    .sortedByDescending { it.score }
                    .mapIndexed { idx, item -> item.copy(rank = idx + 1) }
                _realScores.value = merged
                return@withContext merged
            } else {
                // If remote API returns empty data, display only real local user entries (if any)
                val localUserEntries = _realScores.value.filter { it.isUser }
                _realScores.value = localUserEntries
                return@withContext localUserEntries
            }
        } catch (_: Exception) {
            // Fallback to local user entries
            val localUserEntries = _realScores.value.filter { it.isUser }
            _realScores.value = localUserEntries
            return@withContext localUserEntries
        }
    }
}
