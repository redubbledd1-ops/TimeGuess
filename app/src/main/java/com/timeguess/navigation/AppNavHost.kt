package com.timeguess.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.timeguess.model.PlayMode
import com.timeguess.ui.screen.GameScreen
import com.timeguess.ui.screen.HistoryScreen
import com.timeguess.ui.screen.HomeScreen
import com.timeguess.ui.screen.LanguageSelectionScreen
import com.timeguess.ui.screen.PlayerSetupScreen
import com.timeguess.ui.screen.ResultScreen
import com.timeguess.ui.screen.RoundResultScreen
import com.timeguess.ui.screen.RoundSelectionScreen
import com.timeguess.ui.screen.SameTimeAScreen
import com.timeguess.ui.screen.TimeSelectionScreen
import com.timeguess.viewmodel.GameViewModel

/**
 * Single-round games skip the per-round screen.
 * After the final round, skip [NavRoutes.RoundResult] (no extra "round X of Y – Resultaten" + Resultaten button);
 * [GameViewModel.continueToNextRound] marks the game complete and game screens' [isGameComplete] effect runs [onGameComplete].
 *
 * Custom game (not quick play) with one player: skip the intermediate round summary so play continues on [GameScreen].
 */
private fun navigateAfterRoundComplete(navController: NavHostController, viewModel: GameViewModel) {
    if (viewModel.totalRounds.value == 1) {
        viewModel.continueToNextRound()
        return
    }
    if (viewModel.lastCompletedRound.value >= viewModel.totalRounds.value) {
        viewModel.continueToNextRound()
        return
    }
    if (!viewModel.isQuickPlaySession.value && viewModel.playerCount.value == 1) {
        viewModel.continueToNextRound()
        return
    }
    navController.navigate(NavRoutes.RoundResult.route)
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: GameViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit
) {
    val playerCount by viewModel.playerCount.collectAsState()
    val playMode by viewModel.playMode.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        composable(NavRoutes.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = { languageCode ->
                    onLanguageSelected(languageCode)
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.LanguageSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavRoutes.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onStartGame = {
                    viewModel.loadSettingsForCustomGame()
                    navController.navigate(NavRoutes.TimeSelection.route)
                },
                onQuickPlay = {
                    viewModel.loadQuickPlayConfig()
                    viewModel.resetGame()
                    val loadedPlayerCount = viewModel.playerCount.value
                    val loadedPlayMode = viewModel.playMode.value
                    when {
                        loadedPlayerCount > 1 && loadedPlayMode == PlayMode.SAME_TIME_A -> {
                            navController.navigate(NavRoutes.SameTimeA.route)
                        }
                        else -> {
                            navController.navigate(NavRoutes.Game.route)
                        }
                    }
                },
                onViewHistory = {
                    navController.navigate(NavRoutes.History.route)
                }
            )
        }
        
        composable(NavRoutes.TimeSelection.route) {
            TimeSelectionScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onNext = {
                    navController.navigate(NavRoutes.RoundSelection.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoutes.RoundSelection.route) {
            RoundSelectionScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onNext = {
                    if (playerCount > 1) {
                        navController.navigate(NavRoutes.PlayerSetup.route)
                    } else {
                        viewModel.resetGame()
                        navController.navigate(NavRoutes.Game.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoutes.PlayerSetup.route) {
            PlayerSetupScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onNext = {
                    viewModel.resetGame()
                    when (playMode) {
                        PlayMode.SAME_TIME_A -> navController.navigate(NavRoutes.SameTimeA.route)
                        else -> navController.navigate(NavRoutes.Game.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoutes.Game.route) {
            GameScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onRoundComplete = {
                    navigateAfterRoundComplete(navController, viewModel)
                },
                onGameComplete = {
                    navController.navigate(NavRoutes.Result.route) {
                        popUpTo(NavRoutes.Home.route)
                    }
                }
            )
        }
        
        composable(NavRoutes.SameTimeA.route) {
            SameTimeAScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onRoundComplete = {
                    navigateAfterRoundComplete(navController, viewModel)
                },
                onGameComplete = {
                    navController.navigate(NavRoutes.Result.route) {
                        popUpTo(NavRoutes.Home.route)
                    }
                }
            )
        }
        
        
        composable(NavRoutes.RoundResult.route) {
            RoundResultScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onContinue = {
                    viewModel.continueToNextRound()
                    val isComplete = viewModel.isGameComplete.value
                    if (isComplete) {
                        navController.navigate(NavRoutes.Result.route) {
                            popUpTo(NavRoutes.Home.route)
                        }
                    } else {
                        val currentPlayMode = viewModel.playMode.value
                        when (currentPlayMode) {
                            PlayMode.SAME_TIME_A -> {
                                navController.navigate(NavRoutes.SameTimeA.route) {
                                    popUpTo(NavRoutes.RoundResult.route) { inclusive = true }
                                }
                            }
                            else -> {
                                navController.navigate(NavRoutes.Game.route) {
                                    popUpTo(NavRoutes.RoundResult.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            )
        }
        
        composable(NavRoutes.Result.route) {
            ResultScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onBackToHome = {
                    viewModel.saveRecentNames()
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavRoutes.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                currentLanguageCode = currentLanguageCode,
                onLanguageSelected = onLanguageSelected,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
