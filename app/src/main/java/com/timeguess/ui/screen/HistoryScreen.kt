package com.timeguess.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeguess.R
import com.timeguess.model.DurationMode
import com.timeguess.model.GameHistoryItem
import com.timeguess.model.PlayMode
import com.timeguess.model.RoundDetailItem
import com.timeguess.ui.utils.fmtPositiveSecondsWithUnit
import com.timeguess.ui.utils.fmtSecondsWithUnit
import com.timeguess.ui.utils.fmtSignedDiffSeconds
import com.timeguess.ui.utils.isDifferenceMsPerfectForDisplay
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun HistoryScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val history by viewModel.gameHistory.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var expandedItemId by remember { mutableStateOf<Long?>(null) }
    var loadedRoundDetails by remember { mutableStateOf<Map<Long, List<RoundDetailItem>>>(emptyMap()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = t("game_history"),
                color = textColor,
                fontSize = 29.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (history.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = t("no_history"),
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history, key = { it.id }) { item ->
                        HistoryItemCard(
                            item = item,
                            isExpanded = expandedItemId == item.id,
                            roundDetails = loadedRoundDetails[item.id] ?: emptyList(),
                            isDarkMode = isDarkMode,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            onToggleExpand = {
                                if (expandedItemId == item.id) {
                                    expandedItemId = null
                                } else {
                                    expandedItemId = item.id
                                    if (!loadedRoundDetails.containsKey(item.id)) {
                                        viewModel.loadRoundDetails(item.id) { details ->
                                            loadedRoundDetails = loadedRoundDetails + (item.id to details)
                                        }
                                    }
                                }
                            },
                            onDelete = { viewModel.deleteHistoryItem(item.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDeleteAllDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = t("delete_all_history"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text(
                    text = t("back"),
                    color = textColor,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(t("delete_all_history")) },
            text = { Text(t("delete_all_confirm")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllHistory()
                        showDeleteAllDialog = false
                    }
                ) {
                    Text(t("delete"), color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(t("cancel"))
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    item: GameHistoryItem,
    isExpanded: Boolean,
    roundDetails: List<RoundDetailItem>,
    isDarkMode: Boolean,
    textColor: Color,
    backgroundColor: Color,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
    val dateStr = dateFormat.format(Date(item.timestamp))
    
    val modeText = when (item.durationMode) {
        DurationMode.NORMAL -> t("mode_normal_short")
        DurationMode.ASCENDING -> t("mode_ascending_short")
        DurationMode.DESCENDING -> t("mode_descending_short")
        DurationMode.RANDOM -> t("mode_random_short")
    }
    
    val playModeText = when (item.playMode) {
        PlayMode.ONE_BY_ONE -> t("mode_one_by_one")
        PlayMode.SAME_TIME_A -> t("mode_same_time")
        PlayMode.SAME_TIME_B -> t("mode_same_time")
    }
    val modeTimeText = when (item.durationMode) {
        DurationMode.NORMAL -> t("time_fixed_label", item.baseTimeSeconds, t("abbr_seconds"))
        DurationMode.ASCENDING, DurationMode.DESCENDING -> t("start_time_label", item.baseTimeSeconds, t("abbr_seconds"))
        DurationMode.RANDOM -> t("range_time_label", item.minTimeSeconds, t("abbr_seconds"), item.maxTimeSeconds)
    }
    val perfectAccent = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF7E57C2)
    val perfectToleranceMs = 5L

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = textColor.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dateStr,
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${t("players_count", item.numberOfPlayers)} • ${t("rounds_count", item.roundCount)}",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = modeText,
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = t("delete"),
                        tint = Color(0xFFFF6B6B)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "$modeTimeText • $playModeText",
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "${t("final_results")}:",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    val sortedResults = item.playerResults.sortedBy { abs(it.totalDifferenceMs) }
                    val isSoloGame = item.numberOfPlayers == 1
                    
                    sortedResults.forEachIndexed { index, result ->
                        val hasPositive = result.totalPositiveMs > 0
                        val hasNegative = result.totalNegativeMs < 0
                        val isPerfectTotal = !hasPositive && !hasNegative
                        val positiveSeconds = result.totalPositiveMs / 1000.0
                        val negativeSeconds = result.totalNegativeMs / 1000.0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSoloGame) {
                                    result.name.ifBlank { t("player") }
                                } else {
                                    "#${index + 1} ${result.name.ifBlank { t("player") }}"
                                },
                                color = if (isPerfectTotal) perfectAccent else if (index == 0 && !isSoloGame) Color(0xFF4ECDC4) else textColor,
                                fontSize = 15.sp,
                                fontWeight = if (isPerfectTotal || (index == 0 && !isSoloGame)) FontWeight.Medium else FontWeight.Normal,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                textAlign = TextAlign.End
                            )
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (hasPositive) {
                                    Text(
                                        text = fmtPositiveSecondsWithUnit(positiveSeconds),
                                        color = Color(0xFFFF6B6B),
                                        fontSize = if (hasNegative) 13.sp else 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Start
                                    )
                                }
                                if (hasNegative) {
                                    Text(
                                        text = fmtSecondsWithUnit(negativeSeconds),
                                        color = Color(0xFF4ECDC4),
                                        fontSize = if (hasPositive) 13.sp else 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Start
                                    )
                                }
                                if (!hasPositive && !hasNegative) {
                                    Text(
                                        text = fmtSecondsWithUnit(0.0),
                                        color = perfectAccent,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                        }
                    }
                    
                    if (roundDetails.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        Text(
                            text = "${t("round")} ${t("results")}:",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        val groupedByRound = roundDetails.groupBy { it.roundIndex }
                        
                        groupedByRound.forEach { (roundIndex, details) ->
                            Spacer(modifier = Modifier.height(22.dp))
                            
                            Text(
                                text = "${t("round_simple", roundIndex)} • ${t("target_seconds", (details.firstOrNull()?.targetTimeMs ?: 0L) / 1000)}",
                                color = textColor.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Sort by absolute difference (closest to target first) to determine correct rank
                            val sortedDetails = details.sortedBy { abs(it.differenceMs) }
                            val ranksByIndex = mutableListOf<Int>()
                            var currentRank = 1
                            var lastGroupScore: Long? = null
                            sortedDetails.forEach { d ->
                                val score = abs(d.differenceMs)
                                val isNewGroup = lastGroupScore == null || abs(score - lastGroupScore!!) >= perfectToleranceMs
                                if (isNewGroup && lastGroupScore != null) {
                                    currentRank += 1
                                }
                                ranksByIndex += currentRank
                                if (isNewGroup) {
                                    lastGroupScore = score
                                }
                            }
                            
                            sortedDetails.forEachIndexed { index, detail ->
                                val diffSeconds = detail.differenceMs / 1000.0
                                val diffText = fmtSignedDiffSeconds(diffSeconds)
                                val isPerfectScore = isDifferenceMsPerfectForDisplay(detail.differenceMs)
                                
                                // Tie-safe dense ranking: same rank for near-equal (within tolerance) scores.
                                val rank = ranksByIndex[index]
                                val orderText = if (!isSoloGame) "#$rank " else ""
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$orderText${detail.playerName.ifBlank { t("player") }}",
                                        color = if (isPerfectScore) perfectAccent else textColor.copy(alpha = 0.7f),
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 10.dp),
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = diffText,
                                        color = when {
                                            isPerfectScore -> perfectAccent
                                            detail.differenceMs > 0 -> Color(0xFFFF6B6B)
                                            else -> Color(0xFF4ECDC4)
                                        },
                                        fontSize = 13.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 10.dp),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}
