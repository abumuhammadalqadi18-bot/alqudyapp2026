package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.AlQadiApplication
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AlQadiTheme
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.viewmodels.AppViewModelProvider
import com.example.ui.viewmodels.SettingsViewModel

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val appContainer = (application as AlQadiApplication).container
    val viewModelProvider = AppViewModelProvider(appContainer)
    val settingsViewModel = ViewModelProvider(this, viewModelProvider)[SettingsViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      val uiState by settingsViewModel.uiState.collectAsState()
      
      androidx.compose.runtime.CompositionLocalProvider(LocalCurrencySymbol provides uiState.currencySymbol) {
        AlQadiTheme(darkTheme = uiState.isDarkMode) {
          AppNavigation(viewModelProvider = viewModelProvider)
        }
      }
    }
  }
}
