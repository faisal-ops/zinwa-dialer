package com.zinwa.dialer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zinwa.dialer.ui.BgPage
import com.zinwa.dialer.ui.BgSurface
import com.zinwa.dialer.ui.BgElevated
import com.zinwa.dialer.ui.TextPrimary
import com.zinwa.dialer.ui.TextSecondary
import com.zinwa.dialer.ui.AccentGreen
import com.zinwa.dialer.ui.theme.DialerTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DialerTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

// ── Constants ────────────────────────────────────────────────────────────────

private const val PREFS_NAME = "zinwa_settings"
private const val KEY_BLOCK_UNKNOWN = "block_unknown_callers"
private const val KEY_VIBRATE_RINGING = "vibrate_when_ringing"
private const val KEY_DTMF_TONES = "play_dtmf_tones"
private const val KEY_CALLER_ID_ANNOUNCE = "caller_id_announcement"
private const val KEY_FLIP_TO_SILENCE = "flip_to_silence"

// ── Main settings screen ─────────────────────────────────────────────────────

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Settings list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Display Options ──────────────────────────────────────────
            item { SectionHeader("Display options") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Visibility,
                    title = "Sort order & name format",
                    subtitle = "Contact display preferences",
                    onClick = {
                        launchSafe(context, Intent(Settings.ACTION_DISPLAY_SETTINGS))
                    }
                )
            }

            // ── Sounds & Vibration ───────────────────────────────────────
            item { SectionHeader("Sounds & vibration") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.MusicNote,
                    title = "Phone ringtone",
                    subtitle = "Default ringtone",
                    onClick = {
                        launchSafe(context, Intent(Settings.ACTION_SOUND_SETTINGS))
                    }
                )
            }
            item {
                SettingsToggle(
                    icon = Icons.Default.Vibration,
                    title = "Vibrate when ringing",
                    prefKey = KEY_VIBRATE_RINGING,
                    prefs = prefs
                )
            }
            item {
                SettingsToggle(
                    icon = Icons.Default.Dialpad,
                    title = "Play DTMF tones",
                    prefKey = KEY_DTMF_TONES,
                    prefs = prefs,
                    defaultValue = true
                )
            }

            // ── Quick Responses ──────────────────────────────────────────
            item { SectionHeader("Quick responses") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.QuestionAnswer,
                    title = "Respond via SMS",
                    subtitle = "Edit quick reply messages",
                    onClick = {
                            val intent = Intent("android.telecom.action.SHOW_RESPOND_VIA_SMS_SETTINGS")
                        launchSafe(context, intent, fallbackAction = "android.settings.CALL_SETTINGS")
                    }
                )
            }

            // ── Call Settings ────────────────────────────────────────────
            item { SectionHeader("Calls") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Call,
                    title = "Call settings",
                    subtitle = "Call forwarding, waiting, barring",
                    onClick = {
                        launchSafe(context, Intent("android.settings.CALL_SETTINGS"))
                    }
                )
            }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Phone,
                    title = "Calling accounts",
                    subtitle = "SIM & calling preferences",
                    onClick = {
                        launchSafe(
                            context,
                            Intent("android.telecom.action.CHANGE_PHONE_ACCOUNTS"),
                            fallbackAction = "android.settings.CALL_SETTINGS"
                        )
                    }
                )
            }

            // ── Blocked Numbers ──────────────────────────────────────────
            item { SectionHeader("Blocked numbers") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Block,
                    title = "Blocked numbers",
                    subtitle = "Manage blocked callers",
                    onClick = {
                        launchSafe(
                            context,
                            Intent("android.telecom.action.MANAGE_BLOCKED_NUMBERS"),
                            fallbackAction = Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            fallbackData = "package:${context.packageName}"
                        )
                    }
                )
            }
            item {
                SettingsToggle(
                    icon = Icons.Default.Block,
                    title = "Block unknown callers",
                    prefKey = KEY_BLOCK_UNKNOWN,
                    prefs = prefs
                )
            }

            // ── Voicemail ────────────────────────────────────────────────
            item { SectionHeader("Voicemail") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Voicemail,
                    title = "Voicemail",
                    subtitle = "Visual voicemail, greeting, PIN",
                    onClick = {
                        launchSafe(context, Intent("android.settings.CALL_SETTINGS"))
                    }
                )
            }

            // ── Accessibility ────────────────────────────────────────────
            item { SectionHeader("Accessibility") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Settings,
                    title = "Accessibility",
                    subtitle = "TTY mode, hearing aids",
                    onClick = {
                        launchSafe(
                            context,
                            Intent("android.telecom.action.SHOW_CALL_ACCESSIBILITY_SETTINGS"),
                            fallbackAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                    }
                )
            }
            item {
                SettingsToggle(
                    icon = Icons.Default.Notifications,
                    title = "Caller ID announcement",
                    prefKey = KEY_CALLER_ID_ANNOUNCE,
                    prefs = prefs
                )
            }
            item {
                SettingsToggle(
                    icon = Icons.Default.Phone,
                    title = "Flip to silence",
                    prefKey = KEY_FLIP_TO_SILENCE,
                    prefs = prefs
                )
            }

            // ── Assisted Dialing ─────────────────────────────────────────
            item { SectionHeader("Advanced") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Phone,
                    title = "Assisted dialing",
                    subtitle = "Auto-correct numbers when roaming",
                    onClick = {
                        launchSafe(
                            context,
                            Intent("com.android.dialer.settings.SHOW_ASSISTED_DIALING_SETTINGS"),
                            fallbackAction = "android.settings.CALL_SETTINGS"
                        )
                    }
                )
            }

            // ── About ────────────────────────────────────────────────────
            item { SectionHeader("About") }
            item {
                SettingsNavItem(
                    icon = Icons.Default.Info,
                    title = "About Zinwa Dialer",
                    subtitle = "Version ${getAppVersion(context)}",
                    onClick = {
                        launchSafe(
                            context,
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    }
                )
            }

            // Bottom spacing
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Reusable components ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AccentGreen,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BgElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF555555),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    prefKey: String,
    prefs: SharedPreferences,
    defaultValue: Boolean = false
) {
    var checked by remember { mutableStateOf(prefs.getBoolean(prefKey, defaultValue)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                checked = !checked
                prefs.edit().putBoolean(prefKey, checked).apply()
            }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BgElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = { newVal ->
                checked = newVal
                prefs.edit().putBoolean(prefKey, newVal).apply()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentGreen,
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = BgElevated
            )
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun launchSafe(
    context: Context,
    intent: Intent,
    fallbackAction: String? = null,
    fallbackData: String? = null
) {
    runCatching { context.startActivity(intent) }
        .onFailure {
            if (fallbackAction != null) {
                runCatching {
                    context.startActivity(Intent(fallbackAction).apply {
                        if (fallbackData != null) data = Uri.parse(fallbackData)
                    })
                }
            }
        }
}

private fun getAppVersion(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (_: Exception) {
        "1.0"
    }
}
