package com.timeguess.navigation

sealed class NavRoutes(val route: String) {
    object LanguageSelection : NavRoutes("language_selection")
    object Home : NavRoutes("home")
    object TimeSelection : NavRoutes("time_selection")
    object RoundSelection : NavRoutes("round_selection")
    object PlayerSetup : NavRoutes("player_setup")
    object Game : NavRoutes("game")
    object SameTimeA : NavRoutes("same_time_a")
    object RoundResult : NavRoutes("round_result")
    object Result : NavRoutes("result")
    object History : NavRoutes("history")
}
