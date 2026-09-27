package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScaffold
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShipTrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShipTrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val isSplashVisible by viewModel.isSplashVisible.collectAsState()
                    val currentUser by viewModel.currentUser.collectAsState()
                    val allUsers by viewModel.allUsers.collectAsState()

                    if (isSplashVisible) {
                        SplashScreen(
                            onFinishLoading = {
                                viewModel.dismissSplash()
                            }
                        )
                    } else if (currentUser == null) {
                        AuthScreen(
                            users = allUsers,
                            onLogin = { username, pin, onResult ->
                                viewModel.login(username, pin, onResult)
                            },
                            onQuickSignIn = { user, onResult ->
                                viewModel.quickSignIn(user, onResult)
                            },
                            onRegister = { fullName, username, pin, role, onResult ->
                                viewModel.registerAccount(fullName, username, pin, role, onResult)
                            }
                        )
                    } else {
                        MainAppScaffold(
                            viewModel = viewModel,
                            currentUser = currentUser!!,
                            onLogout = {
                                viewModel.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}
