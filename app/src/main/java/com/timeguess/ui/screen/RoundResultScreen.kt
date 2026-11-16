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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.timeguess.model.RoundDetailItem
import com.timeguess.ui.utils.fmtSignedDiffSecondsWithPerfect
import com.timeguess.ui.utils.isDifferenceMsPerfectForDisplay
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

@Composable
fun RoundResultScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    val lastCompletedRound by viewModel.lastCompletedRound.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val currentRoundTargetTime by viewModel.currentRoundTargetTime.collectAsState()
    
    val roundResults = viewModel.getRoundResultsForRound(lastCompletedRound)
    val isSoloGame = playerCount == 1
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    val paddedModifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp + 12.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (isSoloGame) {
            // Match GameScreen-style vertical centering so the transition from solo play does not jump.
            Column(
                modifier = paddedModifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${t("round_of", lastCompletedRound, totalRounds)} - ${t("results")}",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${t("target_seconds", currentRoundTargetTime)} • ${t("round_of", lastCompletedRound, totalRounds)}",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                roundResults.firstOrNull()?.let { detail ->
                    RoundResultCard(
                        detail = detail,
                        textColor = textColor,
                        isDarkMode = isDarkMode,
                        isSoloGame = true
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColor,
                        contentColor = backgroundColor
                    )
                ) {
                    Text(
                        text = if (lastCompletedRound < totalRounds) t("next") else t("results"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            Column(
                modifier = paddedModifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    text = "${t("round_of", lastCompletedRound, totalRounds)} - ${t("results")}",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${t("target_seconds", currentRoundTargetTime)} • ${t("round_of", lastCompletedRound, totalRounds)}",
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
                    itemsIndexed(roundResults) { index, detail ->
                        RoundResultCard(
                            detail = detail,
                            textColor = textColor,
                            isDarkMode = isDarkMode,
                            isSoloGame = isSoloGame
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColor,
                        contentColor = backgroundColor
                    )
                ) {
                    Text(
                        text = if (lastCompletedRound < totalRounds) t("next") else t("results"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RoundResultCard(
    detail: RoundDetailItem,
    textColor: Color,
    isDarkMode: Boolean,
    isSoloGame: Boolean
) {
    val isWinner = detail.isWinner
    val isPerfectScore = isDifferenceMsPerfectForDisplay(detail.differenceMs)
    val perfectAccent = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF7E57C2)
    val diffSeconds = detail.differenceMs / 1000.0
    val actualSeconds = detail.actualTimeMs / 1000.0
    
    val diffColor = when {
        isPerfectScore -> perfectAccent
        detail.differenceMs > 0 -> Color(0xFFFF6B6B)
        detail.differenceMs < 0 -> Color(0xFF4ECDC4)
        else -> perfectAccent
    }
    
    val diffText = fmtSignedDiffSecondsWithPerfect(diffSeconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) textColor.copy(alpha = 0.15f) else textColor.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSoloGame) {
                Text(
                    text = "#${detail.positionIndex}",
                    color = if (isWinner) Color(0xFF4ECDC4) else textColor.copy(alpha = 0.5f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = detail.playerName.ifBlank { t("player") },
                        color = if (isPerfectScore) perfectAccent else textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (!isSoloGame && isWinner) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🏆",
                            fontSize = 16.sp
                        )
                    }
                }
                
                Text(
                    text = t("time_label", actualSeconds, t("abbr_seconds")),
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = diffText,
                    color = diffColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
