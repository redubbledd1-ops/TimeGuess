package com.timeguess.ui.screen

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeguess.ui.utils.fmtSecondsWithUnit
import com.timeguess.ui.utils.fmtSignedDiffSecondsWithPerfect
import com.timeguess.ui.utils.isDifferenceMsPerfectForDisplay
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

enum class GameState {
    READY,
    RUNNING,
    FINISHED
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onRoundComplete: () -> Unit = {},
    onGameComplete: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(activity) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val currentRoundTargetTime by viewModel.currentRoundTargetTime.collectAsState()
    val currentRound by viewModel.currentRound.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val isRoundComplete by viewModel.isRoundComplete.collectAsState()
    val currentPlayerIndex by viewModel.currentPlayerIndex.collectAsState()
    val players by viewModel.players.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val isQuickPlaySession by viewModel.isQuickPlaySession.collectAsState()
    
    val currentPlayer = players.getOrElse(currentPlayerIndex) { players.firstOrNull() ?: com.timeguess.model.Player(0, "Player 1") }
    
    val targetTimeMs = currentRoundTargetTime * 1000L

    var gameState by remember { mutableStateOf(GameState.READY) }
    var startTime by remember { mutableLongStateOf(0L) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(isGameComplete) {
        if (isGameComplete) {
            onGameComplete()
        }
    }

    // When the round index advances (e.g. custom solo skips RoundResult), reset local tap state on the same screen.
    LaunchedEffect(currentRound) {
        gameState = GameState.READY
        startTime = 0L
        elapsedTime = 0L
    }
    
    LaunchedEffect(isRoundComplete) {
        if (isRoundComplete) {
            onRoundComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                when (gameState) {
                    GameState.READY -> {
                        startTime = System.currentTimeMillis()
                        gameState = GameState.RUNNING
                    }
                    GameState.RUNNING -> {
                        elapsedTime = System.currentTimeMillis() - startTime
                        gameState = GameState.FINISHED
                    }
                    GameState.FINISHED -> {
                        // Do nothing - must use next button
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (gameState) {
                GameState.READY -> {
                    if (playerCount > 1) {
                        Text(
                            text = currentPlayer.name,
                            color = textColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Text(
                        text = t("round_simple", currentRound),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = if (playerCount > 1) 8.dp else 0.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = t("tap_to_start"),
                        color = textColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 24.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = t("target_seconds", currentRoundTargetTime),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                GameState.RUNNING -> {
                    if (playerCount > 1) {
                        Text(
                            text = currentPlayer.name,
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Text(
                        text = t("tap_to_stop"),
                        color = textColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = if (playerCount > 1) 8.dp else 0.dp),
                        textAlign = TextAlign.Center
                    )
                }
                GameState.FINISHED -> {
                    val elapsedSeconds = elapsedTime / 1000.0
                    val difference = elapsedTime - targetTimeMs
                    val differenceSeconds = difference / 1000.0
                    val isPerfect = isDifferenceMsPerfectForDisplay(difference)
                    val differenceText = if (isPerfect) {
                        fmtSignedDiffSecondsWithPerfect(differenceSeconds)
                    } else if (difference >= 0) {
                        t("too_late", differenceSeconds, t("abbr_seconds"))
                    } else {
                        t("too_early", differenceSeconds, t("abbr_seconds"))
                    }
                    val perfectAccent = if (isDarkMode) Color(0xFFBB86FC) else Color(0xFF7E57C2)
                    val diffColor = when {
                        isPerfect -> perfectAccent
                        difference >= 0 -> Color(0xFFFF6B6B)
                        else -> Color(0xFF4ECDC4)
                    }

                    if (playerCount > 1) {
                        Text(
                            text = currentPlayer.name,
                            color = textColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = t("your_time"),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = if (playerCount > 1) 8.dp else 0.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = fmtSecondsWithUnit(elapsedSeconds),
                        color = textColor,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = differenceText,
                        color = diffColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    val resetTimers: () -> Unit = {
                        startTime = 0L
                        elapsedTime = 0L
                    }

                    val submitRound: () -> Unit = {
                        val roundComplete = viewModel.recordRoundResult(
                            differenceMs = difference,
                            targetTimeMs = targetTimeMs,
                            actualTimeMs = elapsedTime
                        )
                        // If we navigate to RoundResult, avoid READY / "Tik om te starten" flashing before navigation.
                        if (!roundComplete) {
                            gameState = GameState.READY
                            resetTimers()
                        }
                    }

                    if (isQuickPlaySession && playerCount == 1 && currentRound == totalRounds) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addExtraQuickPlayRound()
                                viewModel.recordRoundResult(
                                    differenceMs = difference,
                                    targetTimeMs = targetTimeMs,
                                    actualTimeMs = elapsedTime,
                                    quickPlaySoloSkipRoundResultScreen = true
                                )
                                gameState = GameState.READY
                                resetTimers()
                            },
                            modifier = Modifier.padding(top = 32.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            )
                        ) {
                            Text(
                                text = t("new_round"),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = submitRound,
                        modifier = Modifier.padding(
                            top = if (isQuickPlaySession && playerCount == 1 && currentRound == totalRounds) 16.dp else 48.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = textColor,
                            contentColor = backgroundColor
                        )
                    ) {
                        Text(
                            text = if (isQuickPlaySession && playerCount == 1 && currentRound == totalRounds) {
                                t("results")
                            } else {
                                t("next")
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
