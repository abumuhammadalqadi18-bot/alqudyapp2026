package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
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
    enableEdgeToEdge()
    
    setContent {
      val app = application as AlQadiApplication
      val containerVersion by app.containerVersion.collectAsState()
      
      key(containerVersion) {
        val viewModelProvider = remember(containerVersion) { AppViewModelProvider(application) }
        val settingsViewModel = remember(containerVersion) { 
            ViewModelProvider(this@MainActivity, viewModelProvider)[SettingsViewModel::class.java] 
        }
        val uiState by settingsViewModel.uiState.collectAsState()
        
        androidx.compose.runtime.CompositionLocalProvider(LocalCurrencySymbol provides uiState.currencySymbol) {
          AlQadiTheme(darkTheme = uiState.isDarkMode) {
            AppNavigation(viewModelProvider = viewModelProvider)
          }
        }
      }
    }
  }
}
