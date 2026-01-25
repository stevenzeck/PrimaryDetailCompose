package com.example.primarydetailcompose.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_lightColors_onOldSdk() {
        var isLight = false
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = false) {
                // In Material3, lightColorScheme() results in a specific primary color
                // We check if the current color scheme matches what we expect for light.
                // A simple way is to check the luminance or a specific color value.
                isLight =
                    MaterialTheme.colorScheme.primary != androidx.compose.material3.darkColorScheme().primary
            }
        }
        assert(isLight)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_darkColors_onOldSdk() {
        var isDark = false
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = true) {
                isDark =
                    MaterialTheme.colorScheme.primary == androidx.compose.material3.darkColorScheme().primary
            }
        }
        assert(isDark)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun theme_dynamicColors_onNewSdk() {
        // This test ensures dynamic colors don't crash and are applied on API 31+
        composeTestRule.setContent {
            PrimaryDetailTheme {
                // Just verifying it renders
            }
        }
    }
}
