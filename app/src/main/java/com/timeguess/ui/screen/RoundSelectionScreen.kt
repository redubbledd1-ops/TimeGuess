package com.timeguess.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.timeguess.R
import com.timeguess.model.PlayMode
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

@Composable
fun RoundSelectionScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val selectedRounds by viewModel.totalRounds.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val playMode by viewModel.playMode.collectAsState()
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                text = t("number_of_players"),
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CounterButton(
                    text = "−",
                    enabled = playerCount > 1,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    onClick = { viewModel.setPlayerCount(playerCount - 1) }
                )

                Text(
                    text = playerCount.toString(),
                    color = textColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.Center
                )

                CounterButton(
                    text = "+",
                    enabled = playerCount < 6, // Max players changed to 6
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    onClick = { viewModel.setPlayerCount(playerCount + 1) }
                )
            }

            if (playerCount > 1) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (playMode == PlayMode.ONE_BY_ONE) textColor.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { viewModel.setPlayMode(PlayMode.ONE_BY_ONE) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = playMode == PlayMode.ONE_BY_ONE,
                        onClick = { viewModel.setPlayMode(PlayMode.ONE_BY_ONE) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = textColor,
                            unselectedColor = textColor.copy(alpha = 0.5f)
                        )
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "1️⃣ ${t("mode_one_by_one")}",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = t("mode_one_by_one_desc"),
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (playMode == PlayMode.SAME_TIME_A) textColor.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { 
                            if (playerCount <= 6) viewModel.setPlayMode(PlayMode.SAME_TIME_A) 
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = playMode == PlayMode.SAME_TIME_A,
                        onClick = { if (playerCount <= 6) viewModel.setPlayMode(PlayMode.SAME_TIME_A) },
                        enabled = playerCount <= 6,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = textColor,
                            unselectedColor = textColor.copy(alpha = 0.5f),
                            disabledSelectedColor = textColor.copy(alpha = 0.3f),
                            disabledUnselectedColor = textColor.copy(alpha = 0.3f)
                        )
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = "2️⃣ ${t("mode_same_time")}",
                            color = if (playerCount <= 6) textColor else textColor.copy(alpha = 0.4f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = t("mode_same_time_desc"),
                            color = if (playerCount <= 6) textColor.copy(alpha = 0.6f) else textColor.copy(alpha = 0.3f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = t("number_of_rounds"),
                color = textColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CounterButton(
                    text = "−",
                    enabled = selectedRounds > 1,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    onClick = { viewModel.setTotalRounds(selectedRounds - 1) }
                )

                Text(
                    text = selectedRounds.toString(),
                    color = textColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.Center
                )

                CounterButton(
                    text = "+",
                    enabled = selectedRounds < 100,
                    textColor = textColor,
                    backgroundColor = backgroundColor,
                    onClick = { viewModel.setTotalRounds(selectedRounds + 1) }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = t("back"),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColor,
                        contentColor = backgroundColor
                    )
                ) {
                    Text(
                        text = t("next"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CounterButton(
    text: String,
    enabled: Boolean,
    textColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) backgroundColor else backgroundColor)
            .border(
                2.dp,
                if (enabled) textColor else textColor.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.3f),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
