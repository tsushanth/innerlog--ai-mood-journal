package com.factory.innerlogaimoodjournal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.factory.innerlogaimoodjournal.InnerLogApplication

@Composable
fun currentApp(): InnerLogApplication {
    val context = LocalContext.current
    return context.applicationContext as InnerLogApplication
}
