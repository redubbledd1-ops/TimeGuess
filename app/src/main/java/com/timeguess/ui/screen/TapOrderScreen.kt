package com.timeguess.ui.screen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import com.timeguess.R
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel
import kotlin.math.roundToInt

data class TapEntry(val playerIndex: Int, val tapTime: Long)

@Composable
fun TapOrderScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val players by viewModel.players.collectAsState()
    val simultaneousStopTimes by viewModel.simultaneousStopTimes.collectAsState()
    val currentRound by viewModel.currentRound.collectAsState()
    val totalRounds by viewModel.totalRounds.collectAsState()
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    
    val orderedPlayers = remember(simultaneousStopTimes) {
        mutableStateListOf<TapEntry>().apply {
            addAll(
                simultaneousStopTimes.entries
                    .sortedBy { it.value }
                    .mapIndexed { index, entry -> TapEntry(playerIndex = index, tapTime = entry.value) }
            )
        }
    }
    
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var totalDragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeightPx = with(LocalDensity.current) { 80.dp.toPx() }
    
    fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(25)
                }
            }
        } catch (_: Exception) { }
    }
    
    fun moveItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex != toIndex && fromIndex in orderedPlayers.indices && toIndex in orderedPlayers.indices) {
            val item = orderedPlayers.removeAt(fromIndex)
            orderedPlayers.add(toIndex, item)
            vibrate()
        }
    }

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
                text = t("select_order_of_clicks"),
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = t("hold_and_drag_to_reorder"),
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .pointerInput(orderedPlayers.size) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val index = (offset.y / itemHeightPx).toInt()
                                    .coerceIn(0, orderedPlayers.size - 1)
                                draggedIndex = index
                                dragStartIndex = index
                                dragOffsetY = 0f
                                totalDragOffset = 0f
                                vibrate()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (draggedIndex >= 0 && dragStartIndex >= 0) {
                                    totalDragOffset += dragAmount.y
                                    
                                    val targetIndex = (dragStartIndex + (totalDragOffset / itemHeightPx).toInt())
                                        .coerceIn(0, orderedPlayers.size - 1)
                                    
                                    if (targetIndex != draggedIndex) {
                                        moveItem(draggedIndex, targetIndex)
                                        draggedIndex = targetIndex
                                    }
                                    
                                    val offsetFromCurrentSlot = totalDragOffset - ((draggedIndex - dragStartIndex) * itemHeightPx)
                                    dragOffsetY = offsetFromCurrentSlot
                                }
                            },
                            onDragEnd = {
                                draggedIndex = -1
                                dragStartIndex = -1
                                dragOffsetY = 0f
                                totalDragOffset = 0f
                            },
                            onDragCancel = {
                                draggedIndex = -1
                                dragStartIndex = -1
                                dragOffsetY = 0f
                                totalDragOffset = 0f
                            }
                        )
                    }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orderedPlayers.forEachIndexed { index, entry ->
                        val isDragging = draggedIndex == index
                        val player = if (entry.playerIndex < players.size) players[entry.playerIndex] else null
                        val displayName = player?.let { viewModel.getDisplayName(it, entry.playerIndex) } ?: "Player ${entry.playerIndex + 1}"
                        
                        ReorderableCard(
                            displayName = displayName,
                            orderNumber = index + 1,
                            textColor = textColor,
                            isDragging = isDragging,
                            dragOffsetY = if (isDragging) dragOffsetY else 0f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val entries = orderedPlayers.mapIndexed { index, entry ->
                        val player = if (entry.playerIndex < players.size) players[entry.playerIndex] else null
                        val displayName = player?.let { viewModel.getDisplayName(it, entry.playerIndex) } ?: "Player ${entry.playerIndex + 1}"
                        val tapTime = simultaneousStopTimes.entries.toList().getOrNull(entry.playerIndex)?.value ?: entry.tapTime
                        Triple(entry.playerIndex, displayName, tapTime)
                    }
                    viewModel.setConfirmedOrderEntries(entries)
                    onConfirm()
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
                    text = "Confirm Order",
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
private fun ReorderableCard(
    displayName: String,
    orderNumber: Int,
    textColor: Color,
    isDragging: Boolean,
    dragOffsetY: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .zIndex(if (isDragging) 10f else 0f)
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .graphicsLayer {
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
                shadowElevation = if (isDragging) 16f else 4f
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) 
                textColor.copy(alpha = 0.25f) 
            else 
                textColor.copy(alpha = 0.1f)
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
                    .background(
                        if (isDragging) textColor.copy(alpha = 0.3f) 
                        else textColor.copy(alpha = 0.15f)
                    ),
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
            
            Text(
                text = displayName,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                painter = painterResource(id = R.drawable.ic_drag_handle),
                contentDescription = "Drag",
                tint = textColor.copy(alpha = if (isDragging) 1f else 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
