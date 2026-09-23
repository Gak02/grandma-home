package com.example.grandmahome

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun fixedButtonsAndBatteryAreVisible() {
        listOf("LINE", "カメラ", "写真", "連絡先", "電話").forEach {
            compose.onNodeWithText(it).assertIsDisplayed()
        }
        compose.onNodeWithContentDescription("電池残量").assertIsDisplayed()
    }

    @Test
    fun longPressDoesNotOpenEditing() {
        compose.onNodeWithText("LINE").performTouchInput { longClick() }
        listOf("LINE", "カメラ", "写真", "連絡先", "電話").forEach {
            compose.onNodeWithText(it).assertIsDisplayed()
        }
        compose.onNodeWithText("削除").assertDoesNotExist()
    }

    @Test
    fun registeredAsHomeCandidate() {
        val intent =
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(compose.activity.packageName)
        val candidates =
            compose.activity.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        assertTrue(candidates.any { it.activityInfo.name == MainActivity::class.java.name })
    }

    @Test
    fun fiveClockTapsOpenSettingsWithoutPin() {
        repeat(4) { compose.onNodeWithTag("home_clock").performClick() }
        compose.onNodeWithText("Android設定").assertDoesNotExist()
        compose.onNodeWithTag("home_clock").performClick()
        compose.onNodeWithText("Android設定").assertIsDisplayed()
        compose.onNodeWithText("管理者PIN").assertDoesNotExist()
        compose.onNodeWithText("ホームに戻る").performClick()
        compose.onNodeWithText("LINE").assertIsDisplayed()
    }

    @Test
    fun settingsAreAbsentFromTheHomeScreen() {
        compose.onNodeWithText("家族用の設定").assertDoesNotExist()
        compose.onNodeWithText("Android設定").assertDoesNotExist()
        compose.onNodeWithText("Wi-Fi").assertDoesNotExist()
        compose.onNodeWithText("Playストア").assertDoesNotExist()
    }
}
