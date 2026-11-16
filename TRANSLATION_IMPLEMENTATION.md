# Translation System Implementation

## Overview
The app now has a fully functional translation system that:
1. Uses a centralized `t()` function for all UI text
2. Supports multiple languages with fallback to Dutch
3. Logs missing translation keys for debugging
4. Updates UI immediately when language is changed

## Key Components

### 1. Translation Utility (`TranslationUtils.kt`)
- **Location**: `app/src/main/java/com/timeguess/ui/utils/TranslationUtils.kt`
- **Function**: `t(key: String, vararg formatArgs: Any): String`
- **Features**:
  - Maps translation keys to Android string resources
  - Supports formatted strings with arguments (e.g., `t("round_of", 1, 5)`)
  - Falls back to Dutch translations if key is missing
  - Logs warnings for missing keys to help identify gaps
  - Handles exceptions gracefully

### 2. Updated Screens
All screens now use the `t()` function instead of hardcoded text:

- **HomeScreen**: Title, subtitle, button labels
- **HistoryScreen**: Title, empty state message, delete dialog, buttons
- **TimeGuessScreen**: Round info, tap prompts, results
- **TimeSelectionScreen**: Labels, duration modes, increment field
- **RoundSelectionScreen**: Player/round counts, play mode options
- **PlayerSetupScreen**: Title, button labels
- **SimultaneousGameScreen**: Round info, tap counter, status messages
- **TapOrderScreen**: Instructions, round info, drag hints
- **RoundResultScreen**: Results title, next/final buttons
- **ResultScreen**: Results title, home button
- **TopBarActions**: Language selection dialog title

### 3. Translation Keys Added
New keys for UI text that wasn't previously in strings.xml:

```
round_results
next_round
view_final_results
time_label
perfect
select_order_of_clicks
hold_and_drag_to_reorder
start_game_btn
taps_remaining
all_tapped
loading_order_selection
tap_anywhere_to_start
```

### 4. Language Support
- **English** (en) - values/strings.xml
- **Dutch** (nl) - values-nl/strings.xml (fallback language)
- **Other languages**: Spanish, Portuguese, French, German, Italian, Russian, Arabic, Chinese, Hindi, Japanese, Korean, Indonesian, Turkish, Polish, Vietnamese, Thai, Malay, Persian

## How It Works

### Language Selection Flow
1. User selects language in LanguageSelectionScreen or TopBarActions
2. `onLanguageSelected` callback is triggered
3. MainActivity updates:
   - Saves language preference
   - Calls `applyLanguage()` to set system locale
   - Triggers `recreate()` to reload the UI
4. All screens automatically use the new language

### Translation Resolution
When `t("key")` is called:
1. Look up key in the mapping (TranslationUtils.kt)
2. Get the corresponding R.string resource ID
3. Call `stringResource(resourceId)` to get translated text
4. If key not found, log warning and use Dutch fallback
5. Apply any format arguments if provided

### Fallback Mechanism
- **Primary**: Current system language (via stringResource)
- **Secondary**: Dutch translations (hardcoded in getFallbackTranslation)
- **Tertiary**: Key name itself (if all else fails)

## Testing Checklist

✅ All hardcoded UI text replaced with `t()` calls
✅ Translation keys added to strings.xml and strings-nl.xml
✅ Fallback translations provided in TranslationUtils
✅ Language switching triggers UI update
✅ Missing keys are logged to console
✅ Format arguments work correctly (e.g., round numbers, player counts)

## Future Enhancements

1. **Add translations for other languages**: Currently only English and Dutch have complete translations
2. **Pluralization**: Handle singular/plural forms (e.g., "1 round" vs "5 rounds")
3. **Date/Time formatting**: Localize date and time displays
4. **RTL support**: For Arabic, Persian, and other RTL languages
5. **Translation management**: Consider using a translation management tool

## Files Modified

- `app/src/main/java/com/timeguess/ui/utils/TranslationUtils.kt` (NEW)
- `app/src/main/res/values/strings.xml` (added keys)
- `app/src/main/res/values-nl/strings.xml` (added keys)
- All screen files in `app/src/main/java/com/timeguess/ui/screen/`
- `app/src/main/java/com/timeguess/ui/components/TopBarActions.kt`

## Debugging

To find missing translations:
1. Check Android Logcat for "Missing translation key" warnings
2. The key name will be logged
3. Add the key to TranslationUtils.kt mapping
4. Add translations to strings.xml files
5. Rebuild and test

Example log output:
```
W/TranslationUtils: Missing translation key: my_new_key, falling back to Dutch
```
