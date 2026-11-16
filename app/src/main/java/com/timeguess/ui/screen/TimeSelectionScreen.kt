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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.timeguess.R
import com.timeguess.model.DurationMode
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

private val TIME_OPTIONS = listOf(5, 10, 30, 60, 600, 1800)

@Composable
fun TimeSelectionScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val timeConfig by viewModel.timeConfig.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    
    var customTimeText by remember { mutableStateOf(timeConfig.baseTimeSeconds.toString()) }
    var incrementText by remember { mutableStateOf(timeConfig.incrementSeconds.toString()) }
    var minTimeText by remember { mutableStateOf(timeConfig.minTimeSeconds.toString()) }
    var maxTimeText by remember { mutableStateOf(timeConfig.maxTimeSeconds.toString()) }
    var useCustomTime by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(28.dp))

            if (timeConfig.mode != DurationMode.RANDOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TIME_OPTIONS.take(3).forEach { time ->
                        TimeOptionButton(
                            time = time,
                            isSelected = !useCustomTime && timeConfig.baseTimeSeconds == time,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            onClick = {
                                viewModel.setBaseTime(time)
                                useCustomTime = false
                                customTimeText = time.toString()
                            }
                        )
                        if (time != 20) Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TIME_OPTIONS.drop(3).forEach { time ->
                        TimeOptionButton(
                            time = time,
                            isSelected = !useCustomTime && timeConfig.baseTimeSeconds == time,
                            textColor = textColor,
                            backgroundColor = backgroundColor,
                            onClick = {
                                viewModel.setBaseTime(time)
                                useCustomTime = false
                                customTimeText = time.toString()
                            }
                        )
                        if (time != 60) Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (timeConfig.mode == DurationMode.RANDOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minTimeText,
                        onValueChange = { 
                            minTimeText = it
                            it.toIntOrNull()?.let { sec -> viewModel.setMinTime(sec) }
                        },
                        label = { Text(t("min_sec")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            focusedLabelColor = textColor,
                            unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                            cursorColor = textColor
                        )
                    )
                    OutlinedTextField(
                        value = maxTimeText,
                        onValueChange = { 
                            maxTimeText = it
                            it.toIntOrNull()?.let { sec -> viewModel.setMaxTime(sec) }
                        },
                        label = { Text(t("max_sec")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = textColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            focusedLabelColor = textColor,
                            unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                            cursorColor = textColor
                        )
                    )
                }
            } else {
                OutlinedTextField(
                    value = customTimeText,
                    onValueChange = { 
                        customTimeText = it
                        it.toIntOrNull()?.let { seconds ->
                            if (seconds in 1..1800) {
                                viewModel.setBaseTime(seconds)
                                useCustomTime = true
                            }
                        }
                    },
                    label = { Text(t("custom_time_label")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                        cursorColor = textColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            DurationMode.entries.forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clickable { viewModel.setDurationMode(mode) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = timeConfig.mode == mode,
                        onClick = { viewModel.setDurationMode(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = textColor,
                            unselectedColor = textColor.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = when (mode) {
                            DurationMode.NORMAL -> t("mode_normal")
                            DurationMode.ASCENDING -> t("mode_ascending")
                            DurationMode.DESCENDING -> t("mode_descending")
                            DurationMode.RANDOM -> t("mode_random")
                        },
                        color = textColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (timeConfig.mode == DurationMode.ASCENDING || timeConfig.mode == DurationMode.DESCENDING) {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = incrementText,
                    onValueChange = { 
                        incrementText = it
                        it.toIntOrNull()?.let { sec -> viewModel.setIncrement(sec) }
                    },
                    label = { Text(t("increment_label")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(0.6f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = textColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor.copy(alpha = 0.7f),
                        cursorColor = textColor
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(
                    if (timeConfig.mode == DurationMode.ASCENDING || timeConfig.mode == DurationMode.DESCENDING) {
                        40.dp
                    } else {
                        24.dp
                    }
                )
            )

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
private fun TimeOptionButton(
    time: Int,
    isSelected: Boolean,
    textColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val displayText = if (time >= 60) "${time / 60}${t("abbr_minutes")}" else "${time}${t("abbr_seconds")}"
    
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) textColor else backgroundColor)
            .border(2.dp, textColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = if (isSelected) backgroundColor else textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
