package com.timeguess.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timeguess.data.HistoryRepository
import com.timeguess.data.local.GameSettings
import com.timeguess.data.local.QuickPlayPreferences
import com.timeguess.data.local.RecentNamesStore
import com.timeguess.data.local.SettingsDataStore
import com.timeguess.data.local.TimeGuessDatabase
import com.timeguess.model.DurationMode
import com.timeguess.model.GameHistoryItem
import com.timeguess.model.GameResultAggregate
import com.timeguess.model.PlayMode
import com.timeguess.model.Player
import com.timeguess.model.PlayerResult
import com.timeguess.model.RoundDetailItem
import com.timeguess.model.RoundResult
import com.timeguess.model.TimeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        private const val TAG = "GameViewModel"
    }
    
    private val database: TimeGuessDatabase? = try {
        TimeGuessDatabase.getDatabase(application)
    } catch (_: Exception) { null }
    
    private val historyRepository: HistoryRepository? = try {
        database?.let { HistoryRepository(it.gameHistoryDao(), it.gameRoundDao()) }
    } catch (_: Exception) { null }
    
    private val quickPlayPreferences: QuickPlayPreferences? = try {
        QuickPlayPreferences(application)
    } catch (_: Exception) { null }
    
    private val settingsDataStore: SettingsDataStore? = try {
        SettingsDataStore(application)
    } catch (_: Exception) { null }
    
    private val recentNamesStore: RecentNamesStore? = try {
        RecentNamesStore(application)
    } catch (_: Exception) { null }
    
    // StateFlow declarations moved UP
    private val _recentNames = MutableStateFlow<List<String>>(emptyList())
    val recentNames: StateFlow<List<String>> = _recentNames.asStateFlow()
    
    private val _timeConfig = MutableStateFlow(TimeConfig())
    val timeConfig: StateFlow<TimeConfig> = _timeConfig.asStateFlow()
    
    private val _playMode = MutableStateFlow(PlayMode.ONE_BY_ONE)
    val playMode: StateFlow<PlayMode> = _playMode.asStateFlow()
    
    private val _currentRoundTargetTime = MutableStateFlow(10)
    val currentRoundTargetTime: StateFlow<Int> = _currentRoundTargetTime.asStateFlow()
    
    private val _totalRounds = MutableStateFlow(1)
    val totalRounds: StateFlow<Int> = _totalRounds.asStateFlow()
    
    private val _isQuickPlaySession = MutableStateFlow(false)
    val isQuickPlaySession: StateFlow<Boolean> = _isQuickPlaySession.asStateFlow()
    
    private val _playerCount = MutableStateFlow(1)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()
    
    private val _players = MutableStateFlow<List<Player>>(listOf(Player(0, "Player 1")))
    val players: StateFlow<List<Player>> = _players.asStateFlow()
    
    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound.asStateFlow()
    
    private val _currentPlayerIndex = MutableStateFlow(0)
    val currentPlayerIndex: StateFlow<Int> = _currentPlayerIndex.asStateFlow()
    
    private val _roundResults = MutableStateFlow<List<RoundResult>>(emptyList())
    val roundResults: StateFlow<List<RoundResult>> = _roundResults.asStateFlow()
    
    private val _roundDetails = MutableStateFlow<List<RoundDetailItem>>(emptyList())
    val roundDetails: StateFlow<List<RoundDetailItem>> = _roundDetails.asStateFlow()
    
    private val _isGameComplete = MutableStateFlow(false)
    val isGameComplete: StateFlow<Boolean> = _isGameComplete.asStateFlow()
    
    private val _isRoundComplete = MutableStateFlow(false)
    val isRoundComplete: StateFlow<Boolean> = _isRoundComplete.asStateFlow()
    
    private val _lastCompletedRound = MutableStateFlow(0)
    val lastCompletedRound: StateFlow<Int> = _lastCompletedRound.asStateFlow()
    
    private val _simultaneousStopTimes = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val simultaneousStopTimes: StateFlow<Map<Int, Long>> = _simultaneousStopTimes.asStateFlow()
    
    private val _simultaneousStartTime = MutableStateFlow(0L)
    val simultaneousStartTime: StateFlow<Long> = _simultaneousStartTime.asStateFlow()
    
    private val _tapOrder = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val tapOrder: StateFlow<Map<Int, Int>> = _tapOrder.asStateFlow()
    
    private val _confirmedOrderEntries = MutableStateFlow<List<Triple<Int, String, Long>>>(emptyList())
    val confirmedOrderEntries: StateFlow<List<Triple<Int, String, Long>>> = _confirmedOrderEntries.asStateFlow()
    
    data class RoundBackup(
        val roundNumber: Int,
        val playerIndex: Int,
        val roundResults: List<RoundResult>,
        val roundDetails: List<RoundDetailItem>,
        val simultaneousStopTimes: Map<Int, Long>,
        val tapOrder: Map<Int, Int>
    )
    
    private val _lastRoundBackup = MutableStateFlow<RoundBackup?>(null)

    private fun normalizedName(value: String): String = value.trim().lowercase()

    /**
     * Rounds a tap error in ms the same way as [com.timeguess.ui.utils.fmtSignedDiffSeconds] (half-up, 0.01 s),
     * so summed totals match the per-round lines on the results screen.
     */
    private fun differenceMsAlignedToDisplayCentiseconds(diffMs: Long): Long {
        return BigDecimal.valueOf(diffMs)
            .divide(BigDecimal.valueOf(1000L), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(1000L))
            .toLong()
    }

    /**
     * Buckets absolute error (ms) by what users see with two fractional digits in seconds (0.01 s steps).
     * Examples: 380 ms and 384 ms → same bucket; 380 ms and 390 ms → different.
     */
    private fun absErrorDisplayBucket(ms: Long): Long = kotlin.math.round(abs(ms) / 10.0).toLong()

    private fun isWinnerByDisplayBucket(differenceMs: Long, bestAbsDiffMs: Long): Boolean {
        return absErrorDisplayBucket(differenceMs) == kotlin.math.round(bestAbsDiffMs / 10.0).toLong()
    }

    /** 1-based rank: same visible absolute error (two decimals in seconds) ⇒ same rank. */
    private fun rankByDisplayAbsError(differenceMs: Long, allDifferenceMs: List<Long>): Int {
        val myBucket = absErrorDisplayBucket(differenceMs)
        return 1 + allDifferenceMs.count { absErrorDisplayBucket(it) < myBucket }
    }
    
    val gameHistory: StateFlow<List<GameHistoryItem>> = try {
        historyRepository?.getAllHistory()?.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
            ?: MutableStateFlow(emptyList())
    } catch (_: Exception) {
        MutableStateFlow(emptyList())
    }
    
    init {
        // Initialize with safe defaults immediately
        // (StateFlows are already initialized with defaults in their declaration)
        
        // Load persistent data asynchronously to prevent startup crashes
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loadRecentNames()
                loadSavedSettings()
            } catch (e: Exception) {
                // If loading fails, ensure we fallback to defaults
                resetAll()
            }
        }
    }
    
    fun setConfirmedOrderEntries(entries: List<Triple<Int, String, Long>>) {
        _confirmedOrderEntries.value = entries
    }
    
    fun setTimeConfig(config: TimeConfig) {
        _timeConfig.value = config
    }
    
    fun setPlayMode(mode: PlayMode) {
        _playMode.value = mode
    }
    
    fun setDurationMode(mode: DurationMode) {
        _timeConfig.value = _timeConfig.value.copy(mode = mode)
        settingsDataStore?.saveDurationMode(mode)
    }
    
    fun setBaseTime(seconds: Int) {
        val coerced = seconds.coerceIn(1, 1800)
        _timeConfig.value = _timeConfig.value.copy(baseTimeSeconds = coerced)
        settingsDataStore?.saveBaseTime(coerced)
    }
    
    fun setIncrement(seconds: Int) {
        val coerced = seconds.coerceIn(1, 60)
        _timeConfig.value = _timeConfig.value.copy(incrementSeconds = coerced)
        settingsDataStore?.saveIncrement(coerced)
    }
    
    fun setMinTime(seconds: Int) {
        val coerced = seconds.coerceIn(1, 1800)
        _timeConfig.value = _timeConfig.value.copy(minTimeSeconds = coerced)
        settingsDataStore?.saveMinTime(coerced)
    }
    
    fun setMaxTime(seconds: Int) {
        val coerced = seconds.coerceIn(1, 1800)
        _timeConfig.value = _timeConfig.value.copy(maxTimeSeconds = coerced)
        settingsDataStore?.saveMaxTime(coerced)
    }
    
    fun setTotalRounds(rounds: Int) {
        val coerced = rounds.coerceIn(1, 100)
        _totalRounds.value = coerced
        settingsDataStore?.saveTotalRounds(coerced)
    }
    
    fun setPlayerCount(count: Int) {
        val newCount = count.coerceIn(1, 6) // Max players changed to 6
        if (newCount > 1) {
            _playMode.value = PlayMode.SAME_TIME_A // Default to Same Time - A for multiplayer
        }
        _playerCount.value = newCount
        val currentPlayers = _players.value
        _players.value = (0 until newCount).map { index ->
            if (index < currentPlayers.size) {
                currentPlayers[index]
            } else {
                Player(index, "")
            }
        }
    }
    
    fun setPlayerName(index: Int, name: String) {
        val currentPlayers = _players.value.toMutableList()
        if (index in currentPlayers.indices) {
            currentPlayers[index] = currentPlayers[index].copy(name = name)
            _players.value = currentPlayers
        }
    }
    
    fun getDisplayName(player: Player, index: Int): String {
        return player.name.ifBlank { "Player ${index + 1}" }
    }
    
    fun calculateTargetTimeForRound(round: Int): Int {
        val config = _timeConfig.value
        return when (config.mode) {
            DurationMode.NORMAL -> config.baseTimeSeconds
            DurationMode.ASCENDING -> (config.baseTimeSeconds + (round - 1) * config.incrementSeconds).coerceIn(1, 1800)
            DurationMode.DESCENDING -> (config.baseTimeSeconds - (round - 1) * config.incrementSeconds).coerceAtLeast(1)
            DurationMode.RANDOM -> pickRandomNiceTargetSeconds(
                minSec = config.minTimeSeconds,
                maxSec = config.maxTimeSeconds,
                round = round
            )
        }
    }
    
    fun updateCurrentRoundTargetTime() {
        _currentRoundTargetTime.value = calculateTargetTimeForRound(_currentRound.value)
    }
    
    /**
     * Records this player's result and advances turn / round state.
     * @return `true` if the full round is complete and the host should navigate (e.g. RoundResult), `false` if play continues on the same screen.
     */
    fun recordRoundResult(
        differenceMs: Long,
        targetTimeMs: Long? = null,
        actualTimeMs: Long? = null,
        quickPlaySoloSkipRoundResultScreen: Boolean = false
    ): Boolean {
        val player = getCurrentPlayer()
        val playerIndex = _currentPlayerIndex.value
        val displayName = getDisplayName(player, playerIndex)
        val result = RoundResult(
            playerId = playerIndex,
            roundNumber = _currentRound.value,
            differenceMs = differenceMs
        )
        _roundResults.value = _roundResults.value + result
        
        val detail = RoundDetailItem(
            roundIndex = _currentRound.value,
            playerName = displayName,
            targetTimeMs = targetTimeMs ?: (_currentRoundTargetTime.value * 1000L),
            actualTimeMs = actualTimeMs ?: ((_currentRoundTargetTime.value * 1000L) + differenceMs),
            differenceMs = differenceMs
        )
        _roundDetails.value = _roundDetails.value + detail
        
        advanceToNext(quickPlaySoloSkipRoundResultScreen)
        return _isRoundComplete.value
    }
    
    private fun advanceToNext(quickPlaySoloSkipRoundResultScreen: Boolean = false) {
        val nextPlayerIndex = _currentPlayerIndex.value + 1
        
        if (nextPlayerIndex < _playerCount.value) {
            _currentPlayerIndex.value = nextPlayerIndex
        } else {
            val completedRound = _currentRound.value
            _lastCompletedRound.value = completedRound
            calculateRoundRankings(completedRound)
            if (quickPlaySoloSkipRoundResultScreen && _isQuickPlaySession.value && _playerCount.value == 1) {
                advanceCurrentRoundAfterRecordedRound()
            } else {
                _isRoundComplete.value = true
            }
        }
    }
    
    fun continueToNextRound() {
        _isRoundComplete.value = false
        advanceCurrentRoundAfterRecordedRound()
    }
    
    private fun advanceCurrentRoundAfterRecordedRound() {
        val nextRound = _currentRound.value + 1
        if (nextRound <= _totalRounds.value) {
            _currentRound.value = nextRound
            _currentPlayerIndex.value = 0
            updateCurrentRoundTargetTime()
        } else {
            _isGameComplete.value = true
            saveResults()
        }
    }
    
    private fun calculateRoundRankings(roundNumber: Int) {
        val roundDetailsForRound = _roundDetails.value.filter { it.roundIndex == roundNumber }
        if (roundDetailsForRound.isEmpty()) return

        val absDiffs = roundDetailsForRound.map { abs(it.differenceMs) }
        val bestAbsDiff = absDiffs.minOrNull() ?: return

        val updatedDetails = _roundDetails.value.map { detail ->
            if (detail.roundIndex == roundNumber) {
                val position = rankByDisplayAbsError(detail.differenceMs, absDiffs)
                val isWinner = isWinnerByDisplayBucket(detail.differenceMs, bestAbsDiff)
                Log.d(
                    "DEBUG_WINNER",
                    "round=$roundNumber player=${detail.playerName} diff=${detail.differenceMs} abs=${abs(detail.differenceMs)} rank=$position bestAbs=$bestAbsDiff isWinner=$isWinner"
                )
                detail.copy(positionIndex = position, isWinner = isWinner)
            } else {
                detail
            }
        }
        _roundDetails.value = updatedDetails
    }
    
    fun getRoundResultsForRound(roundNumber: Int): List<RoundDetailItem> {
        val resultsForRound = _roundResults.value
            .filter { it.roundNumber == roundNumber }

        val detailsForRound = _roundDetails.value.filter { it.roundIndex == roundNumber }
        val fallbackTargetMs = detailsForRound.firstOrNull()?.targetTimeMs
            ?: (calculateTargetTimeForRound(roundNumber) * 1000L)

        val merged = resultsForRound.map { result ->
            val playerIndex = _players.value.indexOfFirst { it.id == result.playerId }
            val player = _players.value.getOrNull(playerIndex)
            val displayName = if (player != null && playerIndex >= 0) {
                getDisplayName(player, playerIndex)
            } else {
                "Player ${result.playerId + 1}"
            }

            val matchedDetail = detailsForRound.firstOrNull { detail ->
                normalizedName(detail.playerName) == normalizedName(displayName) &&
                    detail.differenceMs == result.differenceMs
            } ?: detailsForRound.firstOrNull { detail ->
                normalizedName(detail.playerName) == normalizedName(displayName) &&
                    absErrorDisplayBucket(detail.differenceMs) == absErrorDisplayBucket(result.differenceMs)
            }

            val targetTimeMs = matchedDetail?.targetTimeMs ?: fallbackTargetMs
            val actualTimeMs = matchedDetail?.actualTimeMs ?: (targetTimeMs + result.differenceMs)
            val coherentDifferenceMs = actualTimeMs - targetTimeMs

            RoundDetailItem(
                roundIndex = roundNumber,
                playerName = displayName,
                targetTimeMs = targetTimeMs,
                actualTimeMs = actualTimeMs,
                differenceMs = coherentDifferenceMs,
                tapOrder = matchedDetail?.tapOrder ?: 0,
                positionIndex = matchedDetail?.positionIndex ?: 0,
                isWinner = matchedDetail?.isWinner ?: false
            )
        }.toMutableList()

        if (merged.isEmpty()) return emptyList()

        val absDiffs = merged.map { abs(it.differenceMs) }
        val bestAbsDiff = absDiffs.minOrNull() ?: return merged
        return merged.map { detail ->
            val rank = rankByDisplayAbsError(detail.differenceMs, absDiffs)
            val winnerByTolerance = isWinnerByDisplayBucket(detail.differenceMs, bestAbsDiff)
            Log.d(
                "DEBUG_WINNER",
                "round=$roundNumber merged player=${detail.playerName} diff=${detail.differenceMs} abs=${abs(detail.differenceMs)} rank=$rank bestAbs=$bestAbsDiff isWinner=$winnerByTolerance"
            )
            detail.copy(
                positionIndex = rank,
                isWinner = winnerByTolerance
            )
        }.sortedBy { it.positionIndex }
    }

    fun getRoundDetailsForPlayerName(playerName: String): List<RoundDetailItem> {
        val normalized = normalizedName(playerName)
        return (1.._totalRounds.value)
            .asSequence()
            .flatMap { round -> getRoundResultsForRound(round).asSequence() }
            .filter { normalizedName(it.playerName) == normalized }
            .sortedBy { it.roundIndex }
            .toList()
    }
    
    fun startSimultaneousRound() {
        _simultaneousStartTime.value = System.currentTimeMillis()
        _simultaneousStopTimes.value = emptyMap()
    }
    
    fun recordSimultaneousStop(playerId: Int) {
        val elapsed = System.currentTimeMillis() - _simultaneousStartTime.value
        var inserted = false
        _simultaneousStopTimes.update { current ->
            if (current.containsKey(playerId)) {
                current
            } else {
                inserted = true
                current + (playerId to elapsed)
            }
        }
        if (inserted) {
            Log.d(TAG, "recordSimultaneousStop inserted playerId=$playerId elapsedMs=$elapsed")
        } else {
            Log.d(TAG, "recordSimultaneousStop ignored duplicate playerId=$playerId elapsedMs=$elapsed")
        }
    }

    /** Next tap index is derived from how many taps were already recorded (safe for simultaneous multi-touch). */
    fun recordNextSameTimeBTap() {
        val elapsed = System.currentTimeMillis() - _simultaneousStartTime.value
        var insertedIndex = -1
        _simultaneousStopTimes.update { current ->
            if (current.size >= _playerCount.value) return@update current
            val tapIndex = current.size
            insertedIndex = tapIndex
            current + (tapIndex to elapsed)
        }
        if (insertedIndex >= 0) {
            Log.d(TAG, "recordNextSameTimeBTap inserted tapIndex=$insertedIndex elapsedMs=$elapsed")
        }
    }
    
    fun recordTapForSameTimeB(tapIndex: Int) {
        if (tapIndex < 0 || tapIndex >= _playerCount.value) return
        val elapsed = System.currentTimeMillis() - _simultaneousStartTime.value
        _simultaneousStopTimes.update { current ->
            if (current.containsKey(tapIndex)) current else current + (tapIndex to elapsed)
        }
    }

    /** One registration per tap index (for parallel strips / multi-touch). */
    fun recordTapForSameTimeBIfAbsent(tapIndex: Int) {
        if (tapIndex < 0 || tapIndex >= _playerCount.value) return
        val elapsed = System.currentTimeMillis() - _simultaneousStartTime.value
        var inserted = false
        _simultaneousStopTimes.update { current ->
            if (current.containsKey(tapIndex)) {
                current
            } else {
                inserted = true
                current + (tapIndex to elapsed)
            }
        }
        if (inserted) {
            Log.d(TAG, "recordTapForSameTimeBIfAbsent inserted tapIndex=$tapIndex elapsedMs=$elapsed")
        } else {
            Log.d(TAG, "recordTapForSameTimeBIfAbsent ignored duplicate tapIndex=$tapIndex elapsedMs=$elapsed")
        }
    }
    
    fun allPlayersStoppedSimultaneous(): Boolean {
        return _simultaneousStopTimes.value.size == _playerCount.value
    }
    
    fun setTapOrder(order: Map<Int, Int>) {
        _tapOrder.value = order
    }
    
    /** Finalizes Same Time A from [_simultaneousStopTimes] (Compose state can lag one frame). */
    fun processSimultaneousResults() {
        val stopTimes = _simultaneousStopTimes.value
        val orderedPlayerIds = stopTimes.entries.sortedBy { it.value }.map { it.key }
        val targetTimeMs = _currentRoundTargetTime.value * 1000L
        Log.d(
            TAG,
            "processSimultaneousResults round=${_currentRound.value} expectedPlayers=${_playerCount.value} stops=${stopTimes.size} ordered=${orderedPlayerIds.size}"
        )
        
        // Calculate all deviations first to determine ranks
        val playerCalculations = orderedPlayerIds.map { playerId ->
            val actualTimeMs = stopTimes[playerId] ?: 0L
            val differenceMs = actualTimeMs - targetTimeMs
            Triple(playerId, actualTimeMs, differenceMs)
        }
        
        // Sort by absolute difference to get ranks
        val absDiffs = playerCalculations.map { abs(it.third) }
        val bestAbsDiff = absDiffs.minOrNull() ?: 0L
        
        orderedPlayerIds.forEachIndexed { orderIndex, playerId ->
            val calculation = playerCalculations.find { it.first == playerId }!!
            val actualTimeMs = calculation.second
            val differenceMs = calculation.third
            
            val player = _players.value.find { it.id == playerId } ?: return@forEachIndexed
            val playerIndex = _players.value.indexOfFirst { it.id == playerId }
            val displayName = getDisplayName(player, playerIndex)
            
            val rank = rankByDisplayAbsError(differenceMs, absDiffs)
            val isWinner = isWinnerByDisplayBucket(differenceMs, bestAbsDiff)
            Log.d(
                "DEBUG_WINNER",
                "same_time_a round=${_currentRound.value} player=$displayName diff=$differenceMs abs=${abs(differenceMs)} rank=$rank bestAbs=$bestAbsDiff isWinner=$isWinner"
            )
            
            val result = RoundResult(
                playerId = playerId,
                roundNumber = _currentRound.value,
                differenceMs = differenceMs
            )
            _roundResults.value = _roundResults.value + result
            
            val detail = RoundDetailItem(
                roundIndex = _currentRound.value,
                playerName = displayName,
                targetTimeMs = targetTimeMs,
                actualTimeMs = actualTimeMs,
                differenceMs = differenceMs,
                tapOrder = orderIndex + 1,
                positionIndex = rank, // Set rank correctly
                isWinner = isWinner
            )
            _roundDetails.value = _roundDetails.value + detail
        }
        
        _lastCompletedRound.value = _currentRound.value
        if (stopTimes.size != _playerCount.value) {
            Log.e(
                TAG,
                "Round ${_currentRound.value} incomplete: expected ${_playerCount.value} results, got ${stopTimes.size}"
            )
        }
        _simultaneousStopTimes.value = emptyMap()
        _tapOrder.value = emptyMap()
        _isRoundComplete.value = true
    }
    
    fun processSimultaneousResultsForSameTimeB(orderedTapTimes: List<Long>) {
        val targetTimeMs = _currentRoundTargetTime.value * 1000L
        Log.d(
            TAG,
            "processSimultaneousResultsForSameTimeB round=${_currentRound.value} expectedPlayers=${_playerCount.value} received=${orderedTapTimes.size}"
        )
        
        // Calculate all deviations first
        val resultCalculations = orderedTapTimes.mapIndexed { index, actualTimeMs ->
            val differenceMs = actualTimeMs - targetTimeMs
            Triple(index, actualTimeMs, differenceMs)
        }
        
        val absDiffs = resultCalculations.map { abs(it.third) }
        val bestAbsDiff = absDiffs.minOrNull() ?: 0L
        
        orderedTapTimes.forEachIndexed { orderIndex, actualTimeMs ->
            val differenceMs = actualTimeMs - targetTimeMs
            
            val player = _players.value.getOrNull(orderIndex) ?: return@forEachIndexed
            val playerIndex = _players.value.indexOfFirst { it.id == player.id }
            val displayName = getDisplayName(player, playerIndex)
            
            val rank = rankByDisplayAbsError(differenceMs, absDiffs)
            val isWinner = isWinnerByDisplayBucket(differenceMs, bestAbsDiff)
            Log.d(
                "DEBUG_WINNER",
                "same_time_b round=${_currentRound.value} player=$displayName diff=$differenceMs abs=${abs(differenceMs)} rank=$rank bestAbs=$bestAbsDiff isWinner=$isWinner"
            )
            
            val result = RoundResult(
                playerId = player.id,
                roundNumber = _currentRound.value,
                differenceMs = differenceMs
            )
            _roundResults.value = _roundResults.value + result
            
            val detail = RoundDetailItem(
                roundIndex = _currentRound.value,
                playerName = displayName,
                targetTimeMs = targetTimeMs,
                actualTimeMs = actualTimeMs,
                differenceMs = differenceMs,
                tapOrder = orderIndex + 1,
                positionIndex = rank, // Set rank correctly
                isWinner = isWinner
            )
            _roundDetails.value = _roundDetails.value + detail
        }
        
        _lastCompletedRound.value = _currentRound.value
        if (orderedTapTimes.size != _playerCount.value) {
            Log.e(
                TAG,
                "Round ${_currentRound.value} incomplete: expected ${_playerCount.value} results, got ${orderedTapTimes.size}"
            )
        }
        _simultaneousStopTimes.value = emptyMap()
        _tapOrder.value = emptyMap()
        _isRoundComplete.value = true
    }
    
    fun getCurrentPlayer(): Player {
        return _players.value.getOrElse(_currentPlayerIndex.value) { Player(0, "Player 1") }
    }
    
    fun getPlayerResults(): List<PlayerResult> {
        return _players.value.mapIndexed { index, player ->
            val displayName = getDisplayName(player, index)
            val normDisplay = normalizedName(displayName)
            val normPlayer = normalizedName(player.name)
            val mergedRounds = (1.._totalRounds.value)
                .asSequence()
                .flatMap { round -> getRoundResultsForRound(round).asSequence() }
                .filter { detail ->
                    val dn = normalizedName(detail.playerName)
                    dn == normDisplay || dn == normPlayer
                }
                .sortedBy { it.roundIndex }
                .toList()
            val playerRounds = _roundResults.value.filter { it.playerId == player.id }
            val diffsMs = if (mergedRounds.isNotEmpty()) {
                mergedRounds.map { it.differenceMs }
            } else {
                playerRounds.sortedBy { it.roundNumber }.map { it.differenceMs }
            }
            val displayAlignedDiffs = diffsMs.map { differenceMsAlignedToDisplayCentiseconds(it) }
            val totalAbsoluteDiff = displayAlignedDiffs.sumOf { kotlin.math.abs(it) }
            val playerDetails = _roundDetails.value.filter {
                val detailName = normalizedName(it.playerName)
                detailName == normPlayer || detailName == normDisplay
            }
            val positions = playerDetails.map { it.positionIndex }.filter { it > 0 }
            val wins = playerDetails.count { it.isWinner }
            val avgPos = if (positions.isNotEmpty()) positions.average().toFloat() else 0f

            val totalPositive = displayAlignedDiffs.filter { it > 0 }.sum()
            val totalNegative = displayAlignedDiffs.filter { it < 0 }.sum()

            PlayerResult(
                name = displayName,
                totalDifferenceMs = totalAbsoluteDiff,
                roundDifferences = displayAlignedDiffs,
                winsCount = wins,
                avgPosition = avgPos,
                positions = positions,
                totalPositiveMs = totalPositive,
                totalNegativeMs = totalNegative
            )
        }
    }
    
    fun getGameResultAggregates(): List<GameResultAggregate> {
        val results = getPlayerResults()
        if (results.isEmpty()) return emptyList()
        
        val totals = results.map { it.totalDifferenceMs }
        val minTotalDeviation = totals.minOrNull() ?: 0L
        val minAvgPosition = results.filter { it.avgPosition > 0 }.minOfOrNull { it.avgPosition } ?: 0f
        
        return results.map { result ->
            val rank = rankByDisplayAbsError(result.totalDifferenceMs, totals)
            val isWinnerTime = isWinnerByDisplayBucket(result.totalDifferenceMs, minTotalDeviation)
            val isWinnerPos = result.avgPosition > 0 && result.avgPosition == minAvgPosition
            
            GameResultAggregate(
                playerName = result.name,
                totalWins = result.winsCount,
                averagePosition = result.avgPosition,
                totalTimeDeviation = result.totalDifferenceMs,
                totalTimeRank = rank,
                isWinnerTotalTime = isWinnerTime,
                isWinnerAvgPosition = isWinnerPos,
                totalPositiveMs = result.totalPositiveMs,
                totalNegativeMs = result.totalNegativeMs
            )
        }
    }
    
    private fun saveResults() {
        viewModelScope.launch {
            try {
                val config = _timeConfig.value
                historyRepository?.saveGameResult(
                    timestamp = System.currentTimeMillis(),
                    numberOfPlayers = _playerCount.value,
                    durationMode = config.mode,
                    playMode = _playMode.value, // Corrected to use _playMode.value
                    baseTimeSeconds = config.baseTimeSeconds,
                    timeIncrementSeconds = config.incrementSeconds,
                    minTimeSeconds = config.minTimeSeconds,
                    maxTimeSeconds = config.maxTimeSeconds,
                    roundCount = _totalRounds.value,
                    playerResults = getPlayerResults(),
                    roundDetails = _roundDetails.value
                )
            } catch (_: Exception) {
            }
        }
    }
    
    fun loadRoundDetails(gameId: Long, onLoaded: (List<RoundDetailItem>) -> Unit) {
        viewModelScope.launch {
            try {
                val details = historyRepository?.getRoundDetails(gameId) ?: emptyList()
                onLoaded(details)
            } catch (_: Exception) {
                onLoaded(emptyList())
            }
        }
    }
    
    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            try {
                historyRepository?.deleteById(id)
            } catch (_: Exception) {
            }
        }
    }
    
    fun deleteAllHistory() {
        viewModelScope.launch {
            try {
                historyRepository?.deleteAll()
            } catch (_: Exception) {
            }
        }
    }
    
    fun resetGame() {
        _currentRound.value = 1
        _currentPlayerIndex.value = 0
        _roundResults.value = emptyList()
        _roundDetails.value = emptyList()
        _isGameComplete.value = false
        _simultaneousStopTimes.value = emptyMap()
        _simultaneousStartTime.value = 0L
        _tapOrder.value = emptyMap()
        _lastRoundBackup.value = null
        updateCurrentRoundTargetTime()
    }
    
    fun safeInitNewGame() {
        _currentRound.value = 1
        _currentPlayerIndex.value = 0
        _roundResults.value = emptyList()
        _roundDetails.value = emptyList()
        _isGameComplete.value = false
        _simultaneousStopTimes.value = emptyMap()
        _simultaneousStartTime.value = 0L
        _tapOrder.value = emptyMap()
        _lastRoundBackup.value = null
        _currentRoundTargetTime.value = calculateTargetTimeForRound(1)
    }
    
    fun resetAll() {
        _timeConfig.value = TimeConfig()
        _playMode.value = PlayMode.ONE_BY_ONE
        _totalRounds.value = 1
        _playerCount.value = 1
        _players.value = listOf(Player(0, "Player 1"))
        _isQuickPlaySession.value = false
        resetGame()
    }
    
    fun loadQuickPlayConfig() {
        // Ensure a default TimeConfig is set before attempting to load, as a safeguard
        _timeConfig.value = TimeConfig()
        _totalRounds.value = 1

        try {
            val config = quickPlayPreferences?.loadConfig() ?: run {
                _isQuickPlaySession.value = false
                safeInitNewGame()
                return
            }
            _playerCount.value = config.playerCount
            // Quick play always starts with one round; extra rounds are per session only (not persisted).
            _totalRounds.value = 1
            _playMode.value = config.playMode
            _timeConfig.value = TimeConfig(
                mode = config.durationMode,
                baseTimeSeconds = config.baseTimeSeconds,
                incrementSeconds = config.incrementSeconds,
                minTimeSeconds = config.minTimeSeconds,
                maxTimeSeconds = config.maxTimeSeconds
            )
            val playerNames = config.playerNames
            _players.value = (0 until config.playerCount).map { index ->
                val name = playerNames.getOrNull(index) ?: ""
                Player(index, name)
            }
        } catch (_: Exception) {
            // If loading fails, clear any potentially corrupt quick play settings
            // Assuming quickPlayPreferences.clearConfig() does not exist or is causing issues.
            // Consider adding a clearConfig method to QuickPlayPreferences if needed.
            // quickPlayPreferences?.clearConfig()
            resetAll()
            safeInitNewGame()
            return
        }
        safeInitNewGame()
        _isQuickPlaySession.value = true
    }
    
    /** Adds one round for the current quick-play session (solo). Total rounds are not saved to quick-play prefs. */
    fun addExtraQuickPlayRound() {
        if (!_isQuickPlaySession.value) return
        _totalRounds.value = (_totalRounds.value + 1).coerceAtMost(100)
    }
    
    fun hasQuickPlayConfig(): Boolean {
        return try {
            quickPlayPreferences?.hasConfig() ?: false
        } catch (_: Exception) {
            false
        }
    }
    
    private fun loadRecentNames() {
        _recentNames.value = try {
            recentNamesStore?.getRecentNames() ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    fun saveRecentNames() {
        try {
            val uniqueNames = _players.value.map { it.name }.filter { it.isNotBlank() && !it.startsWith("Player ") }
            if (uniqueNames.isNotEmpty()) {
                recentNamesStore?.addNames(uniqueNames)
                loadRecentNames()
            }
        } catch (_: Exception) {
        }
    }
    
    private fun loadSavedSettings() {
        // Ensure a default TimeConfig is set before attempting to load, as a safeguard
        _timeConfig.value = TimeConfig()
        _totalRounds.value = 1

        try {
            if (settingsDataStore?.hasSettings() == true) {
                val settings = settingsDataStore.loadSettings()
                _totalRounds.value = settings.totalRounds.coerceIn(1, 100)
                _timeConfig.value = TimeConfig(
                    mode = settings.durationMode,
                    baseTimeSeconds = settings.baseTimeSeconds.coerceIn(1, 1800),
                    incrementSeconds = settings.incrementSeconds.coerceIn(1, 60),
                    minTimeSeconds = settings.minTimeSeconds.coerceIn(1, 1800),
                    maxTimeSeconds = settings.maxTimeSeconds.coerceIn(1, 1800)
                )
            }
        } catch (_: Exception) {
            // If loading fails, clear any potentially corrupt settings
            try {
                settingsDataStore?.clearAllSettings()
            } catch (_: Exception) {}
            resetAll() // Reset all game state to defaults
        }
    }
    
    fun loadSettingsForCustomGame() {
        // Ensure a default TimeConfig is set before attempting to load, as a safeguard
        _timeConfig.value = TimeConfig()
        _totalRounds.value = 1
        _isQuickPlaySession.value = false

        try {
            if (settingsDataStore?.hasSettings() == true) {
                val settings = settingsDataStore.loadSettings()
                _totalRounds.value = settings.totalRounds.coerceIn(1, 100)
                _timeConfig.value = TimeConfig(
                    mode = settings.durationMode,
                    baseTimeSeconds = settings.baseTimeSeconds.coerceIn(1, 1800),
                    incrementSeconds = settings.incrementSeconds.coerceIn(1, 60),
                    minTimeSeconds = settings.minTimeSeconds.coerceIn(1, 1800),
                    maxTimeSeconds = settings.maxTimeSeconds.coerceIn(1, 1800)
                )
            }
        } catch (_: Exception) {
            // If loading fails, clear any potentially corrupt settings
            try {
                settingsDataStore?.clearAllSettings()
            } catch (_: Exception) {}
            resetAll() // Reset all game state to defaults
        }
        safeInitNewGame()
    }
    
    fun saveCurrentSettings() {
        try {
            val settings = GameSettings(
                baseTimeSeconds = _timeConfig.value.baseTimeSeconds,
                totalRounds = _totalRounds.value,
                durationMode = _timeConfig.value.mode,
                incrementSeconds = _timeConfig.value.incrementSeconds,
                minTimeSeconds = _timeConfig.value.minTimeSeconds,
                maxTimeSeconds = _timeConfig.value.maxTimeSeconds,
                customTimeInput = _timeConfig.value.baseTimeSeconds
            )
            settingsDataStore?.saveSettings(settings)
        } catch (_: Exception) {
        }
    }
    
    fun backupCurrentRound() {
        _lastRoundBackup.value = RoundBackup(
            roundNumber = _currentRound.value,
            playerIndex = _currentPlayerIndex.value,
            roundResults = _roundResults.value.toList(),
            roundDetails = _roundDetails.value.toList(),
            simultaneousStopTimes = _simultaneousStopTimes.value.toMap(),
            tapOrder = _tapOrder.value.toMap()
        )
    }
    
    fun undoLastRound(): Boolean {
        val lastRoundNumber = _totalRounds.value
        val roundToUndo = if (_isGameComplete.value) {
            lastRoundNumber
        } else if (_playMode.value == PlayMode.SAME_TIME_A || _playMode.value == PlayMode.SAME_TIME_B) {
            _currentRound.value - 1
        } else {
            if (_currentPlayerIndex.value == 0 && _currentRound.value > 1) {
                _currentRound.value - 1
            } else {
                _currentRound.value
            }
        }
        
        if (roundToUndo < 1) return false
        
        _roundResults.value = _roundResults.value.filter { it.roundNumber != roundToUndo }
        _roundDetails.value = _roundDetails.value.filter { it.roundIndex != roundToUndo }
        
        if (_playMode.value == PlayMode.SAME_TIME_A || _playMode.value == PlayMode.SAME_TIME_B) {
            _currentRound.value = roundToUndo
            _simultaneousStopTimes.value = emptyMap()
            _tapOrder.value = emptyMap()
        } else {
            _currentRound.value = roundToUndo
            _currentPlayerIndex.value = 0
        }
        
        _isGameComplete.value = false
        updateCurrentRoundTargetTime()
        
        return true
    }
    
    fun canUndoRound(): Boolean {
        return _totalRounds.value >= 1
    }

    /**
     * Grid step for random target seconds: finer for short targets, coarser for long (e.g. 600–1800 → steps of 60).
     */
    private fun niceStepSeconds(rangeMax: Int): Int {
        val m = rangeMax.coerceAtLeast(1)
        return when {
            m <= 20 -> 1
            m <= 90 -> 5
            m <= 300 -> 10
            m <= 700 -> 15
            m <= 1400 -> 30
            else -> 60
        }
    }

    private fun secondsMultiplesInRange(lo: Int, hi: Int, step: Int): List<Int> {
        if (step <= 1) return (lo..hi).toList()
        val start = ((lo + step - 1) / step) * step
        val end = (hi / step) * step
        if (start > end) return emptyList()
        val out = ArrayList<Int>((end - start) / step + 1)
        var v = start
        while (v <= hi) {
            out.add(v)
            v += step
        }
        return out
    }

    private fun priorTargetSecondsForRoundsBefore(round: Int): List<Int> {
        return _roundDetails.value
            .asSequence()
            .filter { it.roundIndex < round }
            .distinctBy { it.roundIndex }
            .map { (it.targetTimeMs / 1000L).toInt() }
            .toList()
    }

    /** Whole-second target chosen for the immediately previous round (same for all players). */
    private fun lastRoundTargetSeconds(round: Int): Int? {
        val prev = round - 1
        if (prev < 1) return null
        return _roundDetails.value
            .firstOrNull { it.roundIndex == prev }
            ?.let { (it.targetTimeMs / 1000L).toInt() }
    }

    private fun withoutSameAsLastRound(candidates: List<Int>, lastTarget: Int?): List<Int> {
        if (lastTarget == null) return candidates
        val filtered = candidates.filter { it != lastTarget }
        return filtered.ifEmpty { candidates }
    }

    private fun weightedRandomNiceTarget(candidates: List<Int>, weights: List<Double>): Int {
        val total = weights.sum()
        if (total <= 0.0 || candidates.size != weights.size) {
            return candidates.random()
        }
        var roll = Random.nextDouble() * total
        for (i in candidates.indices) {
            roll -= weights[i]
            if (roll <= 0) return candidates[i]
        }
        return candidates.last()
    }

    /**
     * When min/max are far apart, a single coarse step (e.g. 60) would skip all short targets (1, 3, 20…).
     * This merges a fine ladder on the low end with the global nice grid on the rest.
     */
    private fun candidatesForRandomRange(lo: Int, hi: Int): List<Int> {
        val span = hi - lo
        if (span < 320) {
            val step = niceStepSeconds(hi)
            return secondsMultiplesInRange(lo, hi, step).ifEmpty { (lo..hi).toList() }
        }
        val lowCap = minOf(hi, lo + minOf(165, maxOf(95, span / 10)))
        val microEnd = minOf(hi, lo + 42)
        val micro = (lo..microEnd).toList()
        val restLowEnd = minOf(hi, lowCap)
        val lowMid = if (microEnd + 1 <= restLowEnd) {
            secondsMultiplesInRange(microEnd + 1, restLowEnd, niceStepSeconds(restLowEnd))
        } else {
            emptyList()
        }
        val highStep = niceStepSeconds(hi)
        val highStart = restLowEnd + 1
        val high = if (highStart <= hi) secondsMultiplesInRange(highStart, hi, highStep) else emptyList()
        return (micro + lowMid + high).distinct().sorted()
    }

    private fun priorHadShortRound(prior: List<Int>, shortMax: Int): Boolean =
        prior.any { it <= shortMax }

    /**
     * How strongly to favor low absolute seconds when drawing a random target.
     * Fades out when the configured minimum is already in "long round" territory (~25+ min),
     * so a range like 30–60 min behaves almost uniformly over the allowed grid.
     */
    private fun lowSecondsBiasStrength(lo: Int, hi: Int): Double {
        val span = (hi - lo).coerceAtLeast(1)
        // No extra low-second skew when the whole game is long-duration only.
        if (lo >= 25 * 60) return 0.0
        val fromLo = (1.0 - lo / (25.0 * 60.0)).coerceIn(0.0, 1.0).pow(1.12)
        // Very tight ranges still get skew along [lo, hi], but a bit gentler.
        val fromSpan = if (span >= 300) 1.0 else (0.38 + 0.62 * span / 300.0).coerceIn(0.0, 1.0)
        return fromLo * fromSpan
    }

    /**
     * Multiplier favoring smaller absolute seconds (roughly exponential in log-space).
     * Inactive when [lowSecondsBiasStrength] is ~0.
     */
    private fun absoluteLowSecondsBiasMultiplier(c: Int, lo: Int, hi: Int): Double {
        val s = lowSecondsBiasStrength(lo, hi)
        if (s <= 1e-9) return 1.0
        val ref = maxOf(10.0, lo.toDouble()).coerceAtMost(hi.toDouble())
        val r = ref / c.toDouble().coerceAtLeast(1.0)
        // Between the original (0.92) and stronger (2.55) skew — midpoint ~1.74.
        return r.pow(1.74 * s)
    }

    private fun distanceSpreadWeights(candidates: List<Int>, prior: List<Int>, round: Int): List<Double> {
        val gamma = (1.12 + 0.26 * (round - 1).coerceAtMost(10)).coerceIn(1.05, 3.5)
        return candidates.map { c ->
            val minDist = prior.minOf { p -> abs(c - p) }.toDouble().coerceAtLeast(1.0)
            minDist.pow(gamma)
        }
    }

    /**
     * Picks a whole-second target on a "nice" grid (with a fine low band when the range is wide),
     * biases toward values farther from earlier rounds, and for very wide ranges actively mixes in
     * genuinely short targets (1, 3, 20…) so not every round sits in the hundreds/thousands.
     * Never repeats the exact same target as the previous round.
     */
    private fun pickRandomNiceTargetSeconds(minSec: Int, maxSec: Int, round: Int): Int {
        val lo = minOf(minSec, maxSec).coerceAtLeast(1)
        val hi = maxOf(minSec, maxSec).coerceAtLeast(lo)
        val span = hi - lo
        val lastTarget = lastRoundTargetSeconds(round)
        val candidates = withoutSameAsLastRound(candidatesForRandomRange(lo, hi), lastTarget)
        if (candidates.isEmpty()) {
            return lo
        }
        if (candidates.size == 1) return candidates.first()

        val prior = priorTargetSecondsForRoundsBefore(round)
        if (prior.isEmpty()) {
            val w0 = candidates.map { absoluteLowSecondsBiasMultiplier(it, lo, hi) }
            return weightedRandomNiceTarget(candidates, w0)
        }

        val shortCeil = minOf(hi, lo + 98)
        val shortPool = candidates.filter { it <= shortCeil }
        val wide = span >= 380
        val hadShort = priorHadShortRound(prior, shortMax = 55)
        val shortRoundsSoFar = prior.count { it <= shortCeil }

        if (wide && shortPool.size >= 2) {
            val forceShortChance = when {
                !hadShort && round >= 2 -> 0.48
                shortRoundsSoFar == 0 && round >= 3 -> 0.38
                shortRoundsSoFar * 3 < (round - 1) -> 0.28
                else -> 0.14
            }
            if (Random.nextDouble() < forceShortChance) {
                val wShort = distanceSpreadWeights(shortPool, prior, round).mapIndexed { i, w ->
                    w * absoluteLowSecondsBiasMultiplier(shortPool[i], lo, hi)
                }
                return weightedRandomNiceTarget(shortPool, wShort)
            }
        }

        val base = distanceSpreadWeights(candidates, prior, round)
        val lowBoost = when {
            !wide -> 1.0
            !hadShort -> 5.2
            shortRoundsSoFar * 2 < (round - 1) -> 2.8
            else -> 1.45
        }
        val weights = candidates.mapIndexed { i, c ->
            val b = base[i]
            val skew = absoluteLowSecondsBiasMultiplier(c, lo, hi)
            val boosted = if (c <= shortCeil) b * lowBoost else b
            boosted * skew
        }
        return weightedRandomNiceTarget(candidates, weights)
    }
}
