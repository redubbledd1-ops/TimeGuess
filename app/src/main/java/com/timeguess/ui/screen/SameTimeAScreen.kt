package com.timeguess.ui.screen

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timeguess.ui.utils.fmtSignedDiffSeconds
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel
import java.util.Locale
import kotlin.math.abs

enum class SameTimeAState {
    SETUP,
    READY,
    RUNNING,
    ALL_STOPPED,
    SHOWING_RESULTS
}

@Composable
fun SameTimeAScreen(
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

    val currentRound by viewModel.currentRound.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    val currentRoundTargetTime by viewModel.currentRoundTargetTime.collectAsState()
    val players by viewModel.players.collectAsState()
    val simultaneousStopTimes by viewModel.simultaneousStopTimes.collectAsState()
    val isGameComplete by viewModel.isGameComplete.collectAsState()
    val isRoundComplete by viewModel.isRoundComplete.collectAsState()
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    
    var gameState by remember { mutableStateOf(SameTimeAState.SETUP) }

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

    LaunchedEffect(simultaneousStopTimes) {
        if (viewModel.allPlayersStoppedSimultaneous() && gameState == SameTimeAState.RUNNING) {
            gameState = SameTimeAState.ALL_STOPPED
            viewModel.processSimultaneousResults()
        }
    }

    LaunchedEffect(currentRound) {
        gameState = SameTimeAState.SETUP
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (gameState) {
            SameTimeAState.SETUP -> {
                val playerCount = players.size
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when (playerCount) {
                        2 -> {
                            players.forEachIndexed { index, player ->
                                PlayerNameInputArea(
                                    player = player,
                                    index = index,
                                    viewModel = viewModel,
                                    textColor = textColor,
                                    rotation = if (index == 0) 180f else 0f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                        3 -> {
                            players.forEachIndexed { index, player ->
                                PlayerNameInputArea(
                                    player = player,
                                    index = index,
                                    viewModel = viewModel,
                                    textColor = textColor,
                                    rotation = if (index == 0) 180f else 0f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                        4 -> {
                            for (row in 0 until 2) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (col in 0 until 2) {
                                        val index = row * 2 + col
                                        val player = players[index]
                                        val rotation = when {
                                            row == 0 && col == 0 -> 180f
                                            row == 0 && col == 1 -> 180f
                                            else -> 0f
                                        }
                                        PlayerNameInputArea(
                                            player = player,
                                            index = index,
                                            viewModel = viewModel,
                                            textColor = textColor,
                                            rotation = rotation,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        )
                                    }
                                }
                            }
                        }
                        5 -> {
                            players.forEachIndexed { index, player ->
                                val rotation = if (index < 2) 180f else 0f
                                PlayerNameInputArea(
                                    player = player,
                                    index = index,
                                    viewModel = viewModel,
                                    textColor = textColor,
                                    rotation = rotation,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                        6 -> {
                            for (row in 0 until 2) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (col in 0 until 3) {
                                        val index = row * 3 + col
                                        val player = players[index]
                                        val rotation = if (row == 0) 180f else 0f
                                        PlayerNameInputArea(
                                            player = player,
                                            index = index,
                                            viewModel = viewModel,
                                            textColor = textColor,
                                            rotation = rotation,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            players.forEachIndexed { index, player ->
                                val rotation = if (index < playerCount / 2) 180f else 0f
                                PlayerNameInputArea(
                                    player = player,
                                    index = index,
                                    viewModel = viewModel,
                                    textColor = textColor,
                                    rotation = rotation,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = t("target_seconds", currentRoundTargetTime),
                            color = textColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                viewModel.startSimultaneousRound()
                                gameState = SameTimeAState.RUNNING 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = textColor,
                                contentColor = backgroundColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            val startButtonText =
                                if (currentRound == 1) {
                                    t("start_timer").lowercase(Locale.getDefault())
                                        .replaceFirstChar { it.titlecase(Locale.getDefault()) }
                                } else {
                                    "${t("round")} $currentRound ${t("start_timer").lowercase(Locale.getDefault())
                                        .replaceFirstChar { it.titlecase(Locale.getDefault()) }}"
                                }
                            Text(
                                text = startButtonText,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
            
            SameTimeAState.READY -> {
                 Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.startSimultaneousRound()
                            gameState = SameTimeAState.RUNNING
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Empty as we skip this, but keeping structure valid
                }
            }

            SameTimeAState.RUNNING -> {
                val playerCount = players.size

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (playerCount) {
                            2 -> {
                                players.forEachIndexed { index, player ->
                                    val hasStopped = simultaneousStopTimes.containsKey(player.id)
                                    val displayName = viewModel.getDisplayName(player, index)
                                    PlayerStopButton(
                                        displayName = displayName,
                                        hasStopped = hasStopped,
                                        isDarkMode = isDarkMode,
                                        textColor = textColor,
                                        backgroundColor = backgroundColor,
                                        rotation = if (index == 0) 180f else 0f,
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        onClick = { viewModel.recordSimultaneousStop(player.id) }
                                    )
                                }
                            }
                            3 -> {
                                players.forEachIndexed { index, player ->
                                    val hasStopped = simultaneousStopTimes.containsKey(player.id)
                                    val displayName = viewModel.getDisplayName(player, index)
                                    PlayerStopButton(
                                        displayName = displayName,
                                        hasStopped = hasStopped,
                                        isDarkMode = isDarkMode,
                                        textColor = textColor,
                                        backgroundColor = backgroundColor,
                                        rotation = if (index == 0) 180f else 0f,
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        onClick = { viewModel.recordSimultaneousStop(player.id) }
                                    )
                                }
                            }
                            4 -> {
                                for (row in 0 until 2) {
                                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (col in 0 until 2) {
                                            val index = row * 2 + col
                                            val player = players[index]
                                            val hasStopped = simultaneousStopTimes.containsKey(player.id)
                                            val displayName = viewModel.getDisplayName(player, index)
                                            val rotation = if (row == 0) 180f else 0f
                                            PlayerStopButton(
                                                displayName = displayName,
                                                hasStopped = hasStopped,
                                                isDarkMode = isDarkMode,
                                                textColor = textColor,
                                                backgroundColor = backgroundColor,
                                                rotation = rotation,
                                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                                onClick = { viewModel.recordSimultaneousStop(player.id) }
                                            )
                                        }
                                    }
                                }
                            }
                            5 -> {
                                players.forEachIndexed { index, player ->
                                    val hasStopped = simultaneousStopTimes.containsKey(player.id)
                                    val displayName = viewModel.getDisplayName(player, index)
                                    val rotation = if (index < 2) 180f else 0f
                                    PlayerStopButton(
                                        displayName = displayName,
                                        hasStopped = hasStopped,
                                        isDarkMode = isDarkMode,
                                        textColor = textColor,
                                        backgroundColor = backgroundColor,
                                        rotation = rotation,
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        onClick = { viewModel.recordSimultaneousStop(player.id) }
                                    )
                                }
                            }
                            6 -> {
                                for (row in 0 until 2) {
                                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (col in 0 until 3) {
                                            val index = row * 3 + col
                                            val player = players[index]
                                            val hasStopped = simultaneousStopTimes.containsKey(player.id)
                                            val displayName = viewModel.getDisplayName(player, index)
                                            val rotation = if (row == 0) 180f else 0f
                                            PlayerStopButton(
                                                displayName = displayName,
                                                hasStopped = hasStopped,
                                                isDarkMode = isDarkMode,
                                                textColor = textColor,
                                                backgroundColor = backgroundColor,
                                                rotation = rotation,
                                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                                onClick = { viewModel.recordSimultaneousStop(player.id) }
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                players.forEachIndexed { index, player ->
                                    val hasStopped = simultaneousStopTimes.containsKey(player.id)
                                    val displayName = viewModel.getDisplayName(player, index)
                                    val rotation = if (index < playerCount / 2) 180f else 0f
                                    PlayerStopButton(
                                        displayName = displayName,
                                        hasStopped = hasStopped,
                                        isDarkMode = isDarkMode,
                                        textColor = textColor,
                                        backgroundColor = backgroundColor,
                                        rotation = rotation,
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        onClick = { viewModel.recordSimultaneousStop(player.id) }
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.align(Alignment.Center)
                            .background(Color(0xFF616161).copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "$currentRoundTargetTime s",
                            color = Color.White, // Changed text color to white for better contrast
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            SameTimeAState.ALL_STOPPED, SameTimeAState.SHOWING_RESULTS -> {
                val targetTimeMs = currentRoundTargetTime * 1000L
                val sortedResults = simultaneousStopTimes.entries
                    .sortedBy { abs(it.value - targetTimeMs) }
                    .mapIndexed { index, entry ->
                        val player = players.find { it.id == entry.key }
                        val displayName = player?.let { viewModel.getDisplayName(it, players.indexOf(it)) } ?: t("player")
                        val diff = entry.value - targetTimeMs
                        Triple(displayName, diff, index + 1)
                    }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "${t("round_of", currentRound, totalRounds)} - ${t("results")}",
                        color = textColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    sortedResults.forEach { (name, diff, rank) ->
                        val diffSeconds = diff / 1000.0
                        val diffText = fmtSignedDiffSeconds(diffSeconds)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(
                                    if (rank == 1) textColor.copy(alpha = 0.1f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#$rank",
                                    color = if (rank == 1) Color(0xFF4ECDC4) else textColor.copy(alpha = 0.5f),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "  $name",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = diffText,
                                color = if (diff >= 0) Color(0xFFFF6B6B) else Color(0xFF4ECDC4),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (!isGameComplete) {
                                viewModel.startSimultaneousRound()
                                gameState = SameTimeAState.RUNNING
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = textColor,
                            contentColor = backgroundColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (currentRound < totalRounds) t("next") else t("results"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun PlayerNameInputArea(
    player: com.timeguess.model.Player,
    index: Int,
    viewModel: GameViewModel,
    textColor: Color,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    val displayName = viewModel.getDisplayName(player, index)
    
    Box(
        modifier = modifier
            .background(textColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .rotate(rotation)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = t("player_hint", index + 1),
                color = textColor.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayName,
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PlayerStopButton(
    displayName: String,
    hasStopped: Boolean,
    isDarkMode: Boolean,
    textColor: Color,
    backgroundColor: Color,
    rotation: Float = 0f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val defaultContainerColor = if (isDarkMode) Color(0xFF3A3A3A) else Color(0xFFD9D9D9)
    val defaultLabelColor = if (isDarkMode) Color.White else Color.Black
    val containerColor = if (hasStopped) Color(0xFF4ECDC4) else defaultContainerColor
    val labelColor = if (hasStopped) Color.White else defaultLabelColor
    val onStop by rememberUpdatedState(onClick)

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .pointerInput(hasStopped) {
                if (hasStopped) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { pointer ->
                            if (pointer.changedToDownIgnoreConsumed()) {
                                onStop()
                                return@awaitPointerEventScope
                            }
                        }
                    }
                }
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.rotate(rotation),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayName,
                color = labelColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasStopped) "✓" else t("stop"),
                color = labelColor,
                fontSize = if (hasStopped) 36.sp else 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
