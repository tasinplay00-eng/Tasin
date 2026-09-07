package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.core.SessionManager
import com.example.core.SupabaseConfig
import com.example.data.repository.PulseRepository
import com.example.ui.navigation.PulseRootApp
import com.example.ui.theme.PulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SupabaseConfig.init(applicationContext)
        SessionManager.init(applicationContext)

        setContent {
            val isDarkMode by SessionManager.isDarkMode.collectAsState()
            val repository = remember { PulseRepository() }

            PulseTheme(darkTheme = isDarkMode) {
                PulseRootApp(repository = repository)
            }
        }
    }
}

