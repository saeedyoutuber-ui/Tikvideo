package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TikBlueCyan
import com.example.ui.theme.TikTealGreen

@Composable
fun TikVideoLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 38.sp
) {
    Text(
        text = "TikVideo",
        style = TextStyle(
            brush = Brush.horizontalGradient(
                colors = listOf(TikTealGreen, TikBlueCyan)
            ),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        ),
        modifier = modifier.padding(bottom = 8.dp)
    )
}
