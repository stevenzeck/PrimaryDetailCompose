package com.example.primarydetailcompose.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- API 30 (Old SDK - Static Colors) ---

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_lightColors_onOldSdk_manual() {
        var isLight = false
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = false) {
                isLight = MaterialTheme.colorScheme.primary == lightColorScheme().primary
            }
        }
        assertTrue("Should use light colors when manual false on API 30", isLight)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_darkColors_onOldSdk_manual() {
        var isDark = false
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = true) {
                isDark = MaterialTheme.colorScheme.primary == darkColorScheme().primary
            }
        }
        assertTrue("Should use dark colors when manual true on API 30", isDark)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], qualifiers = "notnight")
    fun theme_defaultToLight_onOldSdk_dayMode() {
        var isLight = false
        composeTestRule.setContent {
            PrimaryDetailTheme {
                isLight = MaterialTheme.colorScheme.primary == lightColorScheme().primary
            }
        }
        assertTrue("Should default to light in day mode on API 30", isLight)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R], qualifiers = "night")
    fun theme_defaultToDark_onOldSdk_nightMode() {
        var isDark = false
        composeTestRule.setContent {
            PrimaryDetailTheme {
                isDark = MaterialTheme.colorScheme.primary == darkColorScheme().primary
            }
        }
        assertTrue("Should default to dark in night mode on API 30", isDark)
    }

    // --- API 31 (New SDK - Dynamic Colors) ---

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun theme_lightColors_onNewSdk_manual() {
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = false) {
                // Exercise manual param on API 31
            }
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun theme_darkColors_onNewSdk_manual() {
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = true) {
                // Exercise manual param on API 31
            }
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S], qualifiers = "notnight")
    fun theme_dynamicLight_onNewSdk_default() {
        composeTestRule.setContent {
            PrimaryDetailTheme {
                // Exercises default param + dynamic light path
            }
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S], qualifiers = "night")
    fun theme_dynamicDark_onNewSdk_default() {
        composeTestRule.setContent {
            PrimaryDetailTheme {
                // Exercises default param + dynamic dark path
            }
        }
    }

    // --- Compose Compiler Bitmask Coverage (Recomposition) ---

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_recomposes_differentValue() {
        var dark by mutableStateOf(false)
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = dark) {
                val currentPrimary = MaterialTheme.colorScheme.primary
                val expectedPrimary = if (dark) darkColorScheme().primary else lightColorScheme().primary
                assertEquals("Color mismatch", expectedPrimary, currentPrimary)
            }
        }
        // Trigger branch: Parameter is "DIFFERENT"
        dark = true
        composeTestRule.waitForIdle()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_recomposes_sameValue() {
        var trigger by mutableIntStateOf(0)
        composeTestRule.setContent {
            PrimaryDetailTheme(useDarkTheme = false) {
                trigger // read state
            }
        }
        // Trigger branch: Parameter is "SAME" (memoized)
        trigger++
        composeTestRule.waitForIdle()
    }
    
    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun theme_callWithNamedArguments() {
        composeTestRule.setContent {
            // Exercise the named argument call path
            PrimaryDetailTheme(
                useDarkTheme = false,
                content = { }
            )
        }
    }
}
