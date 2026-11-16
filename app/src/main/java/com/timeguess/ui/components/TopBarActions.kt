package com.timeguess.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.timeguess.R
import com.timeguess.ui.utils.t

data class LanguageOption(
    val code: String,
    val nativeName: String,
    val englishName: String
)

val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("en", "English", "English"),
    LanguageOption("nl", "Nederlands", "Dutch"),
    LanguageOption("es", "Español", "Spanish"),
    LanguageOption("pt", "Português", "Portuguese"),
    LanguageOption("fr", "Français", "French"),
    LanguageOption("de", "Deutsch", "German"),
    LanguageOption("it", "Italiano", "Italian"),
    LanguageOption("ru", "Русский", "Russian"),
    LanguageOption("ar", "العربية", "Arabic"),
    LanguageOption("zh", "中文", "Chinese"),
    LanguageOption("hi", "हिन्दी", "Hindi"),
    LanguageOption("ja", "日本語", "Japanese"),
    LanguageOption("ko", "한국어", "Korean"),
    LanguageOption("in", "Bahasa Indonesia", "Indonesian"),
    LanguageOption("tr", "Türkçe", "Turkish"),
    LanguageOption("pl", "Polski", "Polish"),
    LanguageOption("vi", "Tiếng Việt", "Vietnamese"),
    LanguageOption("th", "ไทย", "Thai"),
    LanguageOption("ms", "Bahasa Melayu", "Malay"),
    LanguageOption("fa", "فارسی", "Persian")
)

@Composable
fun TopBarActions(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier.padding(top = 40.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { showLanguageDialog = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_language),
                contentDescription = "Change language",
                tint = textColor,
                modifier = Modifier.size(26.dp)
            )
        }
        
        IconButton(onClick = onToggleTheme) {
            Icon(
                painter = painterResource(
                    id = if (isDarkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                ),
                contentDescription = "Toggle theme",
                tint = textColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLanguageCode,
            onLanguageSelected = { code ->
                onLanguageSelected(code)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 760.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = t("select_language"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 700.dp)
                ) {
                    items(SUPPORTED_LANGUAGES) { language ->
                        val isSelected = language.code == currentLanguageCode
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageSelected(language.code) }
                                .background(
                                    if (isSelected) textColor.copy(alpha = 0.1f) 
                                    else backgroundColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = language.nativeName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = language.englishName,
                                    fontSize = 11.sp,
                                    color = textColor.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✓",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}
