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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeguess.ui.utils.fmtSignedDiffSeconds
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

data class ConfirmedEntry(val playerIndex: Int, val playerName: String, val tapTime: Long)

@Composable
fun ConfirmOrderScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    orderedEntries: List<ConfirmedEntry>,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val currentRound by viewModel.currentRound.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    val currentRoundTargetTime by viewModel.currentRoundTargetTime.collectAsState()
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "${t("confirm")} ${t("results")}",
                color = textColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = t("round_of", currentRound, totalRounds),
                color = textColor.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = t("target_seconds", currentRoundTargetTime),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                orderedEntries.forEachIndexed { index, entry ->
                    val targetTimeMs = currentRoundTargetTime * 1000L
                    val differenceMs = entry.tapTime - targetTimeMs
                    
                    ConfirmedCard(
                        displayName = entry.playerName,
                        orderNumber = index + 1,
                        stopTime = entry.tapTime,
                        differenceMs = differenceMs,
                        textColor = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textColor
                    )
                ) {
                    Text(
                        text = t("back"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColor,
                        contentColor = backgroundColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = t("confirm"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConfirmedCard(
    displayName: String,
    orderNumber: Int,
    stopTime: Long,
    differenceMs: Long,
    textColor: Color
) {
    val diffSeconds = differenceMs / 1000.0
    val diffColor = when {
        differenceMs > 0 -> Color(0xFFFF6B6B)
        differenceMs < 0 -> Color(0xFF4ECDC4)
        else -> textColor.copy(alpha = 0.6f)
    }
    val diffText = fmtSignedDiffSeconds(diffSeconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = textColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$orderNumber",
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = t("time_label", stopTime / 1000.0, t("abbr_seconds")),
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
            
            Text(
                text = diffText,
                color = diffColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
