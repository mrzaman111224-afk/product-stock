package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.CloudStockTheme

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.ui.NavigationDestination
import com.example.ui.components.ExitConfirmationDialog

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudStockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentDest by viewModel.currentDestination.collectAsState()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button when attempting to exit the application
    BackHandler(enabled = currentUser == null || currentDest == NavigationDestination.Dashboard) {
        showExitDialog = true
    }

    Crossfade(targetState = currentUser != null, label = "AuthCrossfade") { isAuthenticated ->
        if (isAuthenticated) {
            MainScreen(viewModel = viewModel)
        } else {
            AuthScreen(viewModel = viewModel)
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirmExit = {
                showExitDialog = false
                (context as? Activity)?.finishAffinity()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
}

