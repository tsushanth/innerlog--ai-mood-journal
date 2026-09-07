package com.factory.innerlogaimoodjournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Text(
        text = "PRO",
        color = MaterialTheme.colorScheme.onTertiary,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        modifier = modifier
            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
