package com.timeguess.ui.screen

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

enum class SameTimeBState {
    READY,
    RUNNING,
    ALL_TAPPED
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimultaneousGameScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onAllStopped: () -> Unit,
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

    val currentRound by viewModel.currentRound.collectAsState()
    val currentRoundTargetTime by viewModel.currentRoundTargetTime.collectAsState()
    val players by viewModel.players.collectAsState()
    val simultaneousStopTimes by viewModel.simultaneousStopTimes.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val isRoundComplete by viewModel.isRoundComplete.collectAsState()
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    
    var gameState by remember { mutableStateOf(SameTimeBState.READY) }
    val playerCount = players.size
    val tapCount = simultaneousStopTimes.size

    val readyInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(isGameComplete) {
        if (isGameComplete) {
            onGameComplete()
        }
    }
    
    LaunchedEffect(isRoundComplete) {
        if (isRoundComplete) {
            onRoundComplete()
        }
    }

    LaunchedEffect(tapCount, playerCount) {
        if (tapCount >= playerCount && gameState == SameTimeBState.RUNNING) {
            gameState = SameTimeBState.ALL_TAPPED
            onAllStopped()
        }
    }

    LaunchedEffect(currentRound) {
        if (currentRound == 1) {
            viewModel.startSimultaneousRound()
            gameState = SameTimeBState.RUNNING
        } else {
            gameState = SameTimeBState.READY
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when (gameState) {
                    SameTimeBState.READY -> backgroundColor
                    SameTimeBState.RUNNING -> Color(0xFF2C3E50)
                    SameTimeBState.ALL_TAPPED -> Color(0xFF4ECDC4)
                }
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            when (gameState) {
                SameTimeBState.READY -> {
                    Text(
                        text = t("round_simple", currentRound),
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = t("target_seconds", currentRoundTargetTime),
                        color = textColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = t("tap_anywhere_to_start"),
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                SameTimeBState.RUNNING -> {
                    Text(
                        text = "${playerCount - tapCount}",
                        color = Color.White,
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = t("taps_remaining"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }

                SameTimeBState.ALL_TAPPED -> {
                    Text(
                        text = t("all_tapped"),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = t("loading_order_selection"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        when (gameState) {
            SameTimeBState.READY -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = readyInteractionSource,
                            indication = null
                        ) {
                            viewModel.startSimultaneousRound()
                            gameState = SameTimeBState.RUNNING
                        }
                )
            }
            SameTimeBState.RUNNING -> {
                Row(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val widthPx = size.width
                                    if (widthPx <= 0 || playerCount <= 0) continue
                                    val zoneWidth = widthPx.toFloat() / playerCount.toFloat()

                                    event.changes.forEach { pointer ->
                                        if (pointer.changedToDownIgnoreConsumed()) {
                                            val zoneIndex =
                                                (pointer.position.x / zoneWidth).toInt()
                                                    .coerceIn(0, playerCount - 1)
                                            viewModel.recordTapForSameTimeBIfAbsent(zoneIndex)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    repeat(playerCount) {
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
            SameTimeBState.ALL_TAPPED -> { }
        }
    }
}
