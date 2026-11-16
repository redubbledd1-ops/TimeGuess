package com.timeguess.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeguess.model.DurationMode
import com.timeguess.model.GameResultAggregate
import com.timeguess.model.RoundDetailItem
import com.timeguess.ui.utils.currentFormattingLocale
import com.timeguess.ui.utils.fmtPositiveSecondsWithUnit
import com.timeguess.ui.utils.fmtSecondsWithUnit
import com.timeguess.ui.utils.fmtSignedDiffSeconds
import com.timeguess.ui.utils.isDifferenceMsPerfectForDisplay
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

@Composable
fun ResultScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    val timeConfig by viewModel.timeConfig.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val aggregates = viewModel.getGameResultAggregates()
    val sortedAggregates = aggregates.sortedBy { it.totalTimeDeviation }
    val isSoloGame = playerCount == 1
    val showRandomRoundTargets = timeConfig.mode == DurationMode.RANDOM

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp + 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = t("results"),
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${t("base_time", timeConfig.baseTimeSeconds, t("abbr_seconds"))} • ${getModeText(timeConfig.mode.name)} • ${t("rounds_count", totalRounds)}",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedAggregates, key = { it.playerName }) { aggregate ->
                    val playerRoundDetails = viewModel.getRoundDetailsForPlayerName(aggregate.playerName)
                    
                    ResultCardExtended(
                        rank = aggregate.totalTimeRank,
                        aggregate = aggregate,
                        textColor = textColor,
                        backgroundColor = backgroundColor,
                        isDarkMode = isDarkMode,
                        isSoloGame = isSoloGame,
                        roundDetails = playerRoundDetails,
                        showRandomRoundTargets = showRandomRoundTargets
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = textColor,
                    contentColor = backgroundColor
                )
            ) {
                Text(
                    text = t("back_to_home"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ResultCardExtended(
    rank: Int,
    aggregate: GameResultAggregate,
    textColor: Color,
    backgroundColor: Color,
    isDarkMode: Boolean,
    isSoloGame: Boolean = false,
    roundDetails: List<RoundDetailItem> = emptyList(),
    showRandomRoundTargets: Boolean = false
) {
    val isWinner = aggregate.isWinnerTotalTime || aggregate.isWinnerAvgPosition
    
    val hasPositive = aggregate.totalPositiveMs > 0
    val hasNegative = aggregate.totalNegativeMs < 0
    val perfectAccent = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF7E57C2)
    val positiveSeconds = aggregate.totalPositiveMs / 1000.0
    val negativeSeconds = aggregate.totalNegativeMs / 1000.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) textColor.copy(alpha = 0.15f) else textColor.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSoloGame) {
                    Text(
                        text = "#$rank",
                        color = if (isWinner) Color(0xFF4ECDC4) else textColor.copy(alpha = 0.5f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = aggregate.playerName.ifBlank { t("player") },
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (!isSoloGame && aggregate.isWinnerTotalTime) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🏆",
                                fontSize = 14.sp
                            )
                        }
                        if (!isSoloGame && aggregate.isWinnerAvgPosition) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "⭐",
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    if (!isSoloGame) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${t("wins_short")}: ${aggregate.totalWins}",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            if (aggregate.averagePosition > 0) {
                                Text(
                                    text = "${t("avg_short")}: ${String.format(currentFormattingLocale(), "%.1f", aggregate.averagePosition)}",
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val totalDeviationSeconds = aggregate.totalTimeDeviation / 1000.0
                    Text(
                        text = fmtSecondsWithUnit(totalDeviationSeconds),
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (hasPositive || hasNegative) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (hasPositive) {
                                Text(
                                    text = fmtPositiveSecondsWithUnit(positiveSeconds),
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 12.sp
                                )
                            }
                            if (hasNegative) {
                                Text(
                                    text = fmtSecondsWithUnit(negativeSeconds),
                                    color = Color(0xFF4ECDC4),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            
            if (roundDetails.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = textColor.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roundDetails.forEach { detail ->
                        val diffSeconds = detail.differenceMs / 1000.0
                        val isPerfectScore = isDifferenceMsPerfectForDisplay(detail.differenceMs)
                        val diffColor = when {
                            isPerfectScore -> perfectAccent
                            detail.differenceMs > 0 -> Color(0xFFFF6B6B)
                            detail.differenceMs < 0 -> Color(0xFF4ECDC4)
                            else -> perfectAccent
                        }
                        val diffText = fmtSignedDiffSeconds(diffSeconds)
                        val roundTextColor = if (isPerfectScore) perfectAccent else textColor.copy(alpha = 0.7f)
                        val positionText = if (detail.positionIndex > 0 && !isSoloGame) {
                            "#${detail.positionIndex}"
                        } else ""
                        val targetSec = (detail.targetTimeMs / 1000L).toInt().coerceAtLeast(0)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${t("results")} ${detail.roundIndex}",
                                        color = roundTextColor,
                                        fontSize = 13.sp
                                    )
                                    if (positionText.isNotEmpty()) {
                                        Text(
                                            text = positionText,
                                            color = if (detail.isWinner) Color(0xFF4ECDC4) else textColor.copy(alpha = 0.5f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = diffText,
                                    color = diffColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (showRandomRoundTargets) {
                                Text(
                                    text = t("target_seconds", targetSec),
                                    color = textColor.copy(alpha = 0.55f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getModeText(modeName: String): String {
    return when (modeName) {
        "NORMAL" -> t("mode_normal")
        "ASCENDING" -> t("mode_ascending")
        "DESCENDING" -> t("mode_descending")
        "RANDOM" -> t("mode_random")
        else -> modeName
    }
}
