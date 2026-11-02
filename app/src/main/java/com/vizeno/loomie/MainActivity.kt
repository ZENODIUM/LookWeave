package com.vizeno.loomie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vizeno.loomie.ui.LookWeaveApp
import com.vizeno.loomie.ui.theme.LoomieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoomieTheme(darkTheme = false) {
                LookWeaveApp()
            }
        }
    }
}
