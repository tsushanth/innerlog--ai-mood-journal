package com.factory.innerlogaimoodjournal

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.factory.innerlogaimoodjournal.ui.navigation.InnerLogNavGraph
import com.factory.innerlogaimoodjournal.ui.theme.InnerLogTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InnerLogTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    InnerLogNavGraph(activity = this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as InnerLogApplication).billingManager.refresh()
    }
}
