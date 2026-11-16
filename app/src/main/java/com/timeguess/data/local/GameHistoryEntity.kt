package com.timeguess.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "game_history")
data class GameHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val numberOfPlayers: Int,
    val durationMode: String,
    val playMode: String,
    val baseTimeSeconds: Int,
    val timeIncrementSeconds: Int,
    val minTimeSeconds: Int,
    val maxTimeSeconds: Int,
    val roundCount: Int,
    val playerResultsJson: String
)

@Entity(
    tableName = "game_rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class GameRoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val roundIndex: Int,
    val playerName: String,
    val targetTimeMs: Long,
    val actualTimeMs: Long,
    val differenceMs: Long,
    val tapOrder: Int = 0,
    val positionIndex: Int = 0,
    val isWinner: Boolean = false
)
