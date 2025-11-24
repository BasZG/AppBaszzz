package com.example.appbasz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.appbasz.data.local.PreferencesManager
import com.example.appbasz.navigation.AppNavigation
import com.example.appbasz.ui.theme.AppBaszTheme
import com.example.appbasz.ViewModel.SettingsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Solución temporal
            val preferencesManager = PreferencesManager(this)
            val settingsViewModel = SettingsViewModel(preferencesManager)

            val isDarkTheme by settingsViewModel.isDarkThemeEnabled.collectAsState(initial = false)

            AppBaszTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}