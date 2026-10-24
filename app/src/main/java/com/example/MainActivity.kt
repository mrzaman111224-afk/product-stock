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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BanglalinkOrange
import com.example.ui.theme.Slate950

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
    val isSessionRestored by viewModel.isSessionRestored.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentDest by viewModel.currentDestination.collectAsState()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back button when attempting to exit the application
    BackHandler(enabled = currentUser == null || currentDest == NavigationDestination.Dashboard) {
        showExitDialog = true
    }

    var forceProceed by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        forceProceed = true
    }

    val showSplash = !isSessionRestored && !forceProceed

    if (showSplash) {
        // Pristine Brand Splash Screen while verifying local token session
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.moner_upohar_logo),
                    contentDescription = "মনের উপহার Official Logo",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                CircularProgressIndicator(
                    color = BanglalinkOrange,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    } else {
        Crossfade(targetState = currentUser != null, label = "AuthCrossfade") { isAuthenticated ->
            if (isAuthenticated) {
                MainScreen(viewModel = viewModel)
            } else {
                AuthScreen(viewModel = viewModel)
            }
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

