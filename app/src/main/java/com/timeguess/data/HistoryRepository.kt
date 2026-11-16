package com.timeguess.data

import com.timeguess.data.local.GameHistoryDao
import com.timeguess.data.local.GameHistoryEntity
import com.timeguess.data.local.GameRoundDao
import com.timeguess.data.local.GameRoundEntity
import com.timeguess.model.DurationMode
import com.timeguess.model.GameHistoryItem
import com.timeguess.model.PlayMode
import com.timeguess.model.PlayerResult
import com.timeguess.model.RoundDetailItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(
    private val historyDao: GameHistoryDao,
    private val roundDao: GameRoundDao
) {

    private val gson = Gson()

    fun getAllHistory(): Flow<List<GameHistoryItem>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toGameHistoryItem() }
        }
    }

    suspend fun getRoundDetails(gameId: Long): List<RoundDetailItem> {
        return roundDao.getRoundsForGame(gameId).map { entity ->
            RoundDetailItem(
                roundIndex = entity.roundIndex,
                playerName = entity.playerName,
                targetTimeMs = entity.targetTimeMs,
                actualTimeMs = entity.actualTimeMs,
                differenceMs = entity.differenceMs,
                tapOrder = entity.tapOrder,
                positionIndex = entity.positionIndex,
                isWinner = entity.isWinner
            )
        }
    }

    suspend fun saveGameResult(
        timestamp: Long,
        numberOfPlayers: Int,
        durationMode: DurationMode,
        playMode: PlayMode,
        baseTimeSeconds: Int,
        timeIncrementSeconds: Int,
        minTimeSeconds: Int,
        maxTimeSeconds: Int,
        roundCount: Int,
        playerResults: List<PlayerResult>,
        roundDetails: List<RoundDetailItem>
    ): Long {
        val entity = GameHistoryEntity(
            timestamp = timestamp,
            numberOfPlayers = numberOfPlayers,
            durationMode = durationMode.name,
            playMode = playMode.name,
            baseTimeSeconds = baseTimeSeconds,
            timeIncrementSeconds = timeIncrementSeconds,
            minTimeSeconds = minTimeSeconds,
            maxTimeSeconds = maxTimeSeconds,
            roundCount = roundCount,
            playerResultsJson = gson.toJson(playerResults)
        )
        val gameId = historyDao.insert(entity)

        val roundEntities = roundDetails.map { detail ->
            GameRoundEntity(
                gameId = gameId,
                roundIndex = detail.roundIndex,
                playerName = detail.playerName,
                targetTimeMs = detail.targetTimeMs,
                actualTimeMs = detail.actualTimeMs,
                differenceMs = detail.differenceMs,
                tapOrder = detail.tapOrder,
                positionIndex = detail.positionIndex,
                isWinner = detail.isWinner
            )
        }
        roundDao.insertAll(roundEntities)

        return gameId
    }

    suspend fun deleteById(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun deleteAll() {
        historyDao.deleteAll()
    }

    suspend fun hasHistory(): Boolean {
        return historyDao.getCount() > 0
    }

    private fun GameHistoryEntity.toGameHistoryItem(): GameHistoryItem {
        val type = object : TypeToken<List<PlayerResult>>() {}.type
        val results: List<PlayerResult> = try {
            gson.fromJson(playerResultsJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return GameHistoryItem(
            id = id,
            timestamp = timestamp,
            numberOfPlayers = numberOfPlayers,
            durationMode = DurationMode.fromString(durationMode),
            playMode = PlayMode.fromString(playMode), // Use the safe fromString method
            baseTimeSeconds = baseTimeSeconds,
            timeIncrementSeconds = timeIncrementSeconds,
            minTimeSeconds = minTimeSeconds,
            maxTimeSeconds = maxTimeSeconds,
            roundCount = roundCount,
            playerResults = results
        )
    }
}
