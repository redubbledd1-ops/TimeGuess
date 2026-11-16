package com.timeguess.model

enum class DurationMode {
    NORMAL,
    ASCENDING,
    DESCENDING,
    RANDOM;

    companion object {
        fun fromString(value: String?): DurationMode {
            return try {
                if (value.isNullOrBlank()) NORMAL
                else valueOf(value)
            } catch (e: Exception) {
                NORMAL
            }
        }
    }
}

enum class PlayMode {
    ONE_BY_ONE,
    SAME_TIME_A,
    SAME_TIME_B;

    companion object {
        fun fromString(value: String?): PlayMode {
            return try {
                if (value.isNullOrBlank()) ONE_BY_ONE
                else when (value) {
                    "ONE_BY_ONE" -> ONE_BY_ONE
                    "SAME_TIME_A" -> SAME_TIME_A
                    // Backwards compatibility for old values
                    "SAME_TIME_B", "SAME_TIME" -> SAME_TIME_A
                    else -> valueOf(value)
                }
            } catch (e: Exception) {
                ONE_BY_ONE
            }
        }
    }
}

data class Player(
    val id: Int,
    val name: String
)

data class PlayerResult(
    val name: String,
    val totalDifferenceMs: Long,
    val roundDifferences: List<Long>,
    val winsCount: Int = 0,
    val avgPosition: Float = 0f,
    val positions: List<Int> = emptyList(),
    val totalPositiveMs: Long = 0L,
    val totalNegativeMs: Long = 0L
)

data class GameResult(
    val targetTimeSeconds: Int,
    val totalRounds: Int,
    val playerResults: List<PlayerResult>,
    val timestamp: Long
)

data class RoundResult(
    val playerId: Int,
    val roundNumber: Int,
    val differenceMs: Long
)

data class GameHistoryItem(
    val id: Long,
    val timestamp: Long,
    val numberOfPlayers: Int,
    val durationMode: DurationMode,
    val playMode: PlayMode,
    val baseTimeSeconds: Int,
    val timeIncrementSeconds: Int,
    val minTimeSeconds: Int,
    val maxTimeSeconds: Int,
    val roundCount: Int,
    val playerResults: List<PlayerResult>,
    val roundDetails: List<RoundDetailItem> = emptyList()
)

data class RoundDetailItem(
    val roundIndex: Int,
    val playerName: String,
    val targetTimeMs: Long,
    val actualTimeMs: Long,
    val differenceMs: Long,
    val tapOrder: Int = 0,
    val positionIndex: Int = 0,
    val isWinner: Boolean = false
)

data class GameResultAggregate(
    val playerName: String,
    val totalWins: Int,
    val averagePosition: Float,
    val totalTimeDeviation: Long,
    /** Dense rank by total time (1 = best); ties match two-decimal-second display (0.01 s steps). */
    val totalTimeRank: Int = 0,
    val isWinnerTotalTime: Boolean = false,
    val isWinnerAvgPosition: Boolean = false,
    val totalPositiveMs: Long = 0L,
    val totalNegativeMs: Long = 0L
)

data class TimeConfig(
    val mode: DurationMode = DurationMode.NORMAL,
    val baseTimeSeconds: Int = 10,
    val incrementSeconds: Int = 5,
    val minTimeSeconds: Int = 1,
    val maxTimeSeconds: Int = 60
)
