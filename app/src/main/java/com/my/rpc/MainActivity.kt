package com.my.rpc

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.my.rpc.preference.getLanguageConfig
import com.my.rpc.ui.theme.RpcTheme
import com.my.rpc.ui.theme.LocalDarkTheme
import com.my.rpc.ui.theme.LocalDynamicColorSwitch
import com.my.rpc.ui.theme.SettingsProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var usageAccessStatus: MutableState<Boolean>
    private lateinit var notificationListenerAccess: MutableState<Boolean>

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usageAccessStatus = mutableStateOf(this.hasUsageAccess())
        notificationListenerAccess = mutableStateOf(this.hasNotificationAccess())
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        if (Build.VERSION.SDK_INT < 33) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(getLanguageConfig())
            )
        }
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            SettingsProvider(windowSizeClass.widthSizeClass) {
                RpcTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    isDynamicColorEnabled = LocalDynamicColorSwitch.current,
                ) {
                    Rpc(
                        usageAccessStatus = usageAccessStatus,
                        notificationListenerAccess = notificationListenerAccess,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationListenerAccess.value = hasNotificationAccess()
        usageAccessStatus.value = hasUsageAccess()
    }

    @Suppress("DEPRECATION")
    private fun Context.hasUsageAccess(): Boolean {
        return try {
            val packageManager: PackageManager = this.packageManager
            val applicationInfo = packageManager.getApplicationInfo(this.packageName, 0)
            val appOpsManager = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo.uid,
                applicationInfo.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun Context.hasNotificationAccess(): Boolean {
        val enabledNotificationListeners = Settings.Secure.getString(
            this.contentResolver, "enabled_notification_listeners"
        )
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(this.packageName)
    }

    companion object {
        fun setLanguage(locale: String) {
            val localeListCompat =
                if (locale.isEmpty()) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(locale)
            AppCompatDelegate.setApplicationLocales(localeListCompat)
        }
    }
}