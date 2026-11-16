package com.timeguess.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.timeguess.R
import com.timeguess.ui.utils.t
import com.timeguess.viewmodel.GameViewModel

@Composable
fun PlayerSetupScreen(
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val players by viewModel.players.collectAsState()
    val recentNames by viewModel.recentNames.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    var selectedFieldIndex by remember { mutableStateOf(-1) }

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
                text = t("player_names"),
                color = textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            if (recentNames.isNotEmpty()) {
                Text(
                    text = t("player_names"),
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentNames.take(10).forEach { name ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = textColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    if (selectedFieldIndex >= 0 && selectedFieldIndex < players.size) {
                                        viewModel.setPlayerName(selectedFieldIndex, name)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = name,
                                color = textColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(players) { index, player ->
                    var isFocused by remember { mutableStateOf(false) }
                    val displayValue = if (isFocused || player.name.isNotEmpty()) {
                        player.name
                    } else {
                        ""
                    }
                    val placeholder = t("player_hint", index + 1)
                    
                    OutlinedTextField(
                        value = displayValue,
                        onValueChange = { viewModel.setPlayerName(index, it) },
                        placeholder = { 
                            if (!isFocused && player.name.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = textColor.copy(alpha = 0.5f)
                                )
                            }
                        },
                        label = { Text(t("player_hint", index + 1)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .onFocusChanged { focusState ->
                                isFocused = focusState.isFocused
                                if (focusState.isFocused) {
                                    selectedFieldIndex = index
                                }
                            },
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = textColor,
                    contentColor = backgroundColor
                )
            ) {
                Text(
                    text = t("start_game"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TextButton(onClick = onBack) {
                Text(
                    text = t("back"),
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
