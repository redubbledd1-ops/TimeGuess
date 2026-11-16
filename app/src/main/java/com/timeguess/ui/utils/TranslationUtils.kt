package com.timeguess.ui.utils

import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.timeguess.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

@Composable
fun t(key: String, vararg formatArgs: Any): String {
    // Recompose when locale/configuration changes.
    val configuration = LocalConfiguration.current
    val currentLanguage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        configuration.locales[0]?.language ?: Locale.getDefault().language
    } else {
        @Suppress("DEPRECATION")
        configuration.locale.language
    }

    val resourceId = when (key) {
        "home_title" -> R.string.home_title
        "home_subtitle" -> R.string.home_subtitle
        "start_game" -> R.string.start_game
        "quick_play" -> R.string.quick_play
        "history" -> R.string.history
        "game_history" -> R.string.game_history
        "no_history" -> R.string.no_history
        "delete_all_history" -> R.string.delete_all_history
        "delete_all_confirm" -> R.string.delete_all_confirm
        "delete" -> R.string.delete
        "cancel" -> R.string.cancel
        "back" -> R.string.back
        "next" -> R.string.next
        "round_of" -> R.string.round_of
        "round_simple" -> R.string.round_simple
        "tap_to_start" -> R.string.tap_to_start
        "tap_to_stop" -> R.string.tap_to_stop
        "target_seconds" -> R.string.target_seconds
        "your_time" -> R.string.your_time
        "new_round" -> R.string.new_round
        "too_late" -> R.string.too_late
        "too_early" -> R.string.too_early
        "select_target_time" -> R.string.select_target_time
        "duration_mode" -> R.string.duration_mode
        "mode_normal" -> R.string.mode_normal
        "mode_ascending" -> R.string.mode_ascending
        "mode_descending" -> R.string.mode_descending
        "mode_random" -> R.string.mode_random
        "mode_normal_short" -> R.string.mode_normal_short
        "mode_ascending_short" -> R.string.mode_ascending_short
        "mode_descending_short" -> R.string.mode_descending_short
        "mode_random_short" -> R.string.mode_random_short
        "increment_label" -> R.string.increment_label
        "min_sec" -> R.string.min_sec
        "max_sec" -> R.string.max_sec
        "custom_time_label" -> R.string.custom_time_label
        "abbr_seconds" -> R.string.abbr_seconds
        "abbr_minutes" -> R.string.abbr_minutes
        "number_of_players" -> R.string.number_of_players
        "number_of_rounds" -> R.string.number_of_rounds
        "play_mode" -> R.string.play_mode
        "mode_one_by_one" -> R.string.mode_one_by_one
        "mode_same_time" -> R.string.mode_same_time
        "mode_one_by_one_desc" -> R.string.mode_one_by_one_desc
        "mode_same_time_desc" -> R.string.mode_same_time_desc
        "player_names" -> R.string.player_names
        "player_hint" -> R.string.player_hint
        "results" -> R.string.results
        "wins_short" -> R.string.wins_short
        "back_to_home" -> R.string.back_to_home
        "enter_tap_order" -> R.string.enter_tap_order
        "tap_order_instructions" -> R.string.tap_order_instructions
        "position" -> R.string.position
        "avg_short" -> R.string.avg_short
        "confirm" -> R.string.confirm
        "players_count" -> R.string.players_count
        "rounds_count" -> R.string.rounds_count
        "base_time" -> R.string.base_time
        "select_language" -> R.string.select_language
        "continue_btn" -> R.string.continue_btn
        "all_players_ready" -> R.string.all_players_ready
        "waiting_for_players" -> R.string.waiting_for_players
        "start_timer" -> R.string.start_timer
        "stop" -> R.string.stop
        "round_results" -> R.string.round_results
        "next_round" -> R.string.next_round
        "view_final_results" -> R.string.view_final_results
        "time_label" -> R.string.time_label
        "time_fixed_label" -> R.string.time_fixed_label
        "start_time_label" -> R.string.start_time_label
        "range_time_label" -> R.string.range_time_label
        "perfect" -> R.string.perfect
        "select_order_of_clicks" -> R.string.select_order_of_clicks
        "hold_and_drag_to_reorder" -> R.string.hold_and_drag_to_reorder
        "start_game_btn" -> R.string.start_game_btn
        "taps_remaining" -> R.string.taps_remaining
        "all_tapped" -> R.string.all_tapped
        "loading_order_selection" -> R.string.loading_order_selection
        "tap_anywhere_to_start" -> R.string.tap_anywhere_to_start
        "round" -> R.string.round
        "final_results" -> R.string.final_results
        "score" -> R.string.score
        "correct" -> R.string.correct
        "wrong" -> R.string.wrong
        "play_again" -> R.string.play_again
        "player" -> R.string.player
        "total" -> R.string.total
        else -> {
            Log.w("TranslationUtils", "Missing translation key: $key, falling back to Dutch")
            return getFallbackTranslation(key, *formatArgs)
        }
    }

    val translated = if (formatArgs.isNotEmpty()) {
        stringResource(resourceId, *formatArgs)
    } else {
        stringResource(resourceId)
    }

    if (
        key == "start_game_btn" ||
        key == "round_results" ||
        key == "next_round" ||
        key == "view_final_results" ||
        key == "stop" ||
        key == "final_results"
    ) {
        Log.d("TranslationUtils", "lang=$currentLanguage key=$key value=$translated")
    }

    return translated
}

private fun getFallbackTranslation(key: String, vararg formatArgs: Any): String {
    val fallbackText = when (key) {
        "home_title" -> "TimeGuess"
        "home_subtitle" -> "Hoe goed is jouw tijdsgevoel?"
        "start_game" -> "Aangepast Spel"
        "quick_play" -> "Snel Spelen"
        "history" -> "Geschiedenis"
        "game_history" -> "Spelgeschiedenis"
        "no_history" -> "Nog geen spelgeschiedenis"
        "delete_all_history" -> "Alle Geschiedenis Verwijderen"
        "delete_all_confirm" -> "Weet je zeker dat je alle spelgeschiedenis wilt verwijderen? Dit kan niet ongedaan worden gemaakt."
        "delete" -> "Verwijderen"
        "cancel" -> "Annuleren"
        "back" -> "Terug"
        "next" -> "Volgende"
        "round_of" -> "Ronde %d van %d"
        "round_simple" -> "Ronde %d"
        "tap_to_start" -> "Tik om te starten"
        "tap_to_stop" -> "Tik om te stoppen"
        "target_seconds" -> "Doel: %d seconden"
        "your_time" -> "Jouw tijd"
        "new_round" -> "Nieuwe ronde"
        "too_late" -> "+%1${'$'}.2f %2${'$'}s (te laat)"
        "too_early" -> "%1${'$'}.2f %2${'$'}s (te vroeg)"
        "select_target_time" -> "Kies Doeltijd"
        "duration_mode" -> "Duur Modus"
        "mode_normal" -> "Normaal – elke ronde dezelfde tijd"
        "mode_ascending" -> "Oplopend – tijd neemt toe per ronde"
        "mode_descending" -> "Aflopend – tijd neemt af per ronde"
        "mode_random" -> "Willekeurig – willekeurige tijd per ronde"
        "mode_normal_short" -> "Normaal"
        "mode_ascending_short" -> "Oplopend"
        "mode_descending_short" -> "Aflopend"
        "mode_random_short" -> "Willekeurig"
        "increment_label" -> "Toename (sec/ronde)"
        "min_sec" -> "Min (sec)"
        "max_sec" -> "Max (sec)"
        "custom_time_label" -> "Aangepast (1–1800 sec)"
        "abbr_seconds" -> "s"
        "abbr_minutes" -> "m"
        "number_of_players" -> "Aantal Spelers"
        "number_of_rounds" -> "Aantal Rondes"
        "play_mode" -> "Spelmodus"
        "mode_one_by_one" -> "Om de beurt"
        "mode_same_time" -> "Tegelijkertijd"
        "mode_one_by_one_desc" -> "Spelers om de beurt"
        "mode_same_time_desc" -> "Tegelijk (max 6 spelers)"
        "player_names" -> "Spelersnamen"
        "player_hint" -> "Speler %d"
        "results" -> "Resultaten"
        "wins_short" -> "Win's"
        "back_to_home" -> "Terug naar Home"
        "enter_tap_order" -> "Voer Tikvolgorde In"
        "tap_order_instructions" -> "Voer de positie in waarop elke speler tikte (1 = eerste)"
        "position" -> "Positie"
        "avg_short" -> "Gem."
        "confirm" -> "Bevestigen"
        "players_count" -> "%d speler(s)"
        "rounds_count" -> "%d ronde(s)"
        "base_time" -> "Basistijd: %d %s"
        "select_language" -> "Kies Taal"
        "continue_btn" -> "Doorgaan"
        "all_players_ready" -> "Alle spelers klaar!"
        "waiting_for_players" -> "Wachten op %d speler(s)"
        "start_timer" -> "START"
        "stop" -> "STOP"
        "round_results" -> "Ronde %d Resultaten"
        "next_round" -> "Volgende Ronde"
        "view_final_results" -> "Bekijk Eindresultaten"
        "time_label" -> "Tijd: %1${'$'}.2f %2${'$'}s"
        "time_fixed_label" -> "Tijd: %d %s"
        "start_time_label" -> "Start tijd: %d %s"
        "range_time_label" -> "Min: %1${'$'}d %2${'$'}s, Max: %3${'$'}d %2${'$'}s"
        "perfect" -> "Perfect!"
        "select_order_of_clicks" -> "Selecteer Tikvolgorde"
        "hold_and_drag_to_reorder" -> "Houd vast en sleep om opnieuw in te delen (1e bovenaan)"
        "start_game_btn" -> "Spel Starten"
        "taps_remaining" -> "tikken resterend"
        "all_tapped" -> "Alles getikt!"
        "loading_order_selection" -> "Tikvolgorde laden..."
        "tap_anywhere_to_start" -> "Tik ergens om te starten"
        "round" -> "Ronde"
        "final_results" -> "Eindresultaten"
        "score" -> "Score"
        "correct" -> "Goed"
        "wrong" -> "Fout"
        "play_again" -> "Opnieuw spelen"
        "player" -> "Speler"
        "total" -> "totaal"
        else -> key
    }
    
    return if (formatArgs.isNotEmpty()) {
        try {
            fallbackText.format(*formatArgs)
        } catch (e: Exception) {
            fallbackText
        }
    } else {
        fallbackText
    }
}

@Composable
fun currentFormattingLocale(): Locale {
    val configuration = LocalConfiguration.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.locales[0] ?: Locale.getDefault()
    } else {
        @Suppress("DEPRECATION")
        configuration.locale
    }
}

@Composable
fun fmtSecondsWithUnit(value: Double): String {
    val loc = currentFormattingLocale()
    return String.format(loc, "%.2f %s", value, t("abbr_seconds"))
}

@Composable
fun fmtSignedDiffSeconds(value: Double): String {
    val loc = currentFormattingLocale()
    val unit = t("abbr_seconds")
    return when {
        value > 0 -> String.format(loc, "+%.2f %s", value, unit)
        else -> String.format(loc, "%.2f %s", value, unit)
    }
}

/**
 * True when the error rounds to exactly 0.00 s (half-up, 0.01 s), matching [fmtSignedDiffSeconds] /
 * [GameViewModel.differenceMsAlignedToDisplayCentiseconds] so UI colours match what users read.
 */
fun isDifferenceMsPerfectForDisplay(differenceMs: Long): Boolean {
    return BigDecimal.valueOf(differenceMs)
        .divide(BigDecimal.valueOf(1000L), 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(1000L))
        .toLong() == 0L
}

@Composable
fun fmtSignedDiffSecondsWithPerfect(value: Double): String {
    val loc = currentFormattingLocale()
    val unit = t("abbr_seconds")
    val centis = kotlin.math.round(value * 100.0) / 100.0
    return when {
        kotlin.math.abs(centis) < 1e-9 ->
            "${String.format(loc, "%.2f %s", 0.0, unit)} (${t("perfect")})"
        centis > 0 -> String.format(loc, "+%.2f %s", centis, unit)
        else -> String.format(loc, "%.2f %s", centis, unit)
    }
}

@Composable
fun fmtPositiveSecondsWithUnit(value: Double): String {
    val loc = currentFormattingLocale()
    return String.format(loc, "+%.2f %s", value, t("abbr_seconds"))
}
