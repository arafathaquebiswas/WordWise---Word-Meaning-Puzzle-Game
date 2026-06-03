package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen
import com.example.ui.LevelSelectionScreen
import com.example.ui.GameScreen
import com.example.ui.ResultScreen
import com.example.ui.SettingsScreen
import com.example.ui.SplashScreen
import com.example.ui.FlashcardLevelSelectionScreen
import com.example.ui.FlashcardStudyScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val gameViewModel: GameViewModel = viewModel()
                val state by gameViewModel.uiState.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController = navController)
                    }
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            state = state,
                            onSetGoal = { goal ->
                                gameViewModel.setDailyGoal(goal)
                            }
                        )
                    }
                    composable("level_selection") {
                        LevelSelectionScreen(
                            navController = navController,
                            state = state,
                            onLevelSelected = { difficulty ->
                                gameViewModel.startNewSession(difficulty)
                            }
                        )
                    }
                    composable("game") {
                        GameScreen(
                            navController = navController,
                            state = state,
                            onAnswerSelected = { option ->
                                gameViewModel.handleAnswerSelection(option)
                            },
                            onQuit = {
                                gameViewModel.endSessionAndGoHome()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            effectFlow = gameViewModel.uiEffect
                        )
                    }
                    composable("results") {
                        ResultScreen(
                            navController = navController,
                            state = state,
                            onRestart = {
                                gameViewModel.restartSession()
                                navController.navigate("game") {
                                    popUpTo("results") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            navController = navController,
                            state = state,
                            onAdminAddCoins = {
                                gameViewModel.addBonusCoins(100)
                            },
                            onResetProgress = {
                                gameViewModel.resetGameProgress()
                            },
                            onIncrementStreak = {
                                gameViewModel.incrementCheatStreak()
                            },
                            onIncrementDailyGoal = {
                                gameViewModel.incrementCheatDailyGoal()
                            },
                            onSetGoal = { goal ->
                                gameViewModel.setDailyGoal(goal)
                            }
                        )
                    }
                    composable("flashcard_difficulty") {
                        FlashcardLevelSelectionScreen(
                            navController = navController,
                            state = state,
                            onLevelSelected = { difficulty ->
                                gameViewModel.startNewFlashcardSession(difficulty)
                                navController.navigate("flashcard") {
                                    popUpTo("flashcard_difficulty") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("flashcard") {
                        FlashcardStudyScreen(
                            navController = navController,
                            state = state,
                            onFlip = { gameViewModel.flipFlashcard() },
                            onKnewIt = { gameViewModel.handleFlashcardKnewIt() },
                            onForgotIt = { gameViewModel.handleFlashcardForgotIt() },
                            onQuit = {
                                gameViewModel.endFlashcardSessionAndGoHome()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onRestart = { gameViewModel.restartFlashcardSession() },
                            effectFlow = gameViewModel.uiEffect
                        )
                    }
                }
            }
        }
    }
}
