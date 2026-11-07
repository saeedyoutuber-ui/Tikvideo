package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.attemptAutoSignIn
import com.example.data.ScreenTab
import com.example.data.TikVideoRepository
import com.example.ui.components.TikVideoBottomNav
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.theme.TikBackground
import com.example.ui.theme.TikVideoTheme
import com.example.viewmodel.TikVideoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TikVideoViewModel by viewModels {
        viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                TikVideoViewModel(TikVideoRepository(app))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Attempt silent auto-sign-in on app startup if already authorized
        val credentialManager = CredentialManager.create(this)
        attemptAutoSignIn(
            context = this,
            credentialManager = credentialManager,
            onAuthSuccess = { user ->
                viewModel.setUiMessage("Welcome back, ${user.displayName ?: "Creator"}!")
            },
            onUnauthenticated = {
                // User can interactively sign in
            },
            scope = lifecycleScope
        )

        setContent {
            TikVideoTheme {
                TikVideoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TikVideoApp(viewModel: TikVideoViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val homeFeedTab by viewModel.homeFeedTab.collectAsStateWithLifecycle()
    val messageFilterTab by viewModel.messageFilterTab.collectAsStateWithLifecycle()
    val messageSearchQuery by viewModel.messageSearchQuery.collectAsStateWithLifecycle()
    val notificationFilterTab by viewModel.notificationFilterTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val videos by viewModel.videos.collectAsStateWithLifecycle()
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    // Handle Android system back press to return to Home feed tab
    if (currentTab != ScreenTab.HOME) {
        BackHandler {
            viewModel.switchTab(ScreenTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TikBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            TikVideoBottomNav(
                currentTab = currentTab,
                onTabSelected = { tab -> viewModel.switchTab(tab) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TikBackground)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    videos = videos,
                    currentFeedTab = homeFeedTab
                )
                ScreenTab.CHAT -> MessagesScreen(
                    viewModel = viewModel,
                    filterTab = messageFilterTab,
                    searchQuery = messageSearchQuery,
                    chats = chats
                )
                ScreenTab.UPLOAD -> UploadScreen(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
                ScreenTab.ALERTS -> NotificationsScreen(
                    viewModel = viewModel,
                    filterTab = notificationFilterTab,
                    notifications = notifications
                )
                ScreenTab.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    userProfile = userProfile
                )
            }
        }
    }
}
