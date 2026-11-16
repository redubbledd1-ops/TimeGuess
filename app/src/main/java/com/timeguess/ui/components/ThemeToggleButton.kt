package com.timeguess.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.timeguess.R

@Composable
fun ThemeToggleButton(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    
    IconButton(
        onClick = onToggleTheme,
        modifier = modifier.padding(top = 40.dp, end = 16.dp, start = 16.dp)
    ) {
        Icon(
            painter = painterResource(
                id = if (isDarkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
            ),
            contentDescription = "Toggle theme",
            tint = textColor,
            modifier = Modifier.size(28.dp)
        )
    }
}
