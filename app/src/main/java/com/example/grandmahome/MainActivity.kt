package com.example.grandmahome

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val Ink = Color(0xFF233B43)
private val Paper = Color(0xFFFAF8F1)
private val HomeColors =
    lightColorScheme(
        primary = Color(0xFF235F53),
        onPrimary = Color.White,
        background = Paper,
        surface = Paper,
        onSurface = Ink,
        onBackground = Ink,
    )

class MainActivity : ComponentActivity() {
    private var session by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideStatusBar()
        setContent {
            MaterialTheme(colorScheme = HomeColors) { key(session) { GrandmaHome(this) } }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideStatusBar()
    }

    private fun hideStatusBar() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    override fun onStop() {
        super.onStop()
        session++ // ホームに戻ったときは設定メニューを閉じる。
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        session++
    }

    internal fun open(intent: Intent, missing: String = "この機能を開けませんでした") {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, missing, Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            Toast.makeText(this, missing, Toast.LENGTH_LONG).show()
        }
    }

    internal fun openPackage(name: String, label: String, fallback: Intent? = null) {
        val intent = packageManager.getLaunchIntentForPackage(name) ?: fallback
        if (intent == null) Toast.makeText(this, "${label}がインストールされていません", Toast.LENGTH_LONG).show()
        else open(intent, "${label}を開けませんでした")
    }

    internal fun chooseHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (
                roles.isRoleAvailable(RoleManager.ROLE_HOME) &&
                    !roles.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                open(roles.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        open(Intent(Settings.ACTION_HOME_SETTINGS))
    }
}

@Composable
internal fun GrandmaHome(activity: MainActivity) {
    var admin by remember { mutableStateOf(false) }
    val gate = remember { ClockTapGate() }
    var now by remember { mutableStateOf(ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))) }
    var battery by remember { mutableIntStateOf(-1) }
    var charging by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"))
            delay(1000)
        }
    }
    DisposableEffect(Unit) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
                    battery = if (level >= 0 && scale > 0) level * 100 / scale else -1
                    charging = (intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0
                }
            }
        activity.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        onDispose { activity.unregisterReceiver(receiver) }
    }
    BackHandler { admin = false }
    Surface(Modifier.fillMaxSize(), color = Paper) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val screenHeight = maxHeight
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = screenHeight)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    Modifier.fillMaxWidth()
                        .testTag("home_clock")
                        .clickable(role = Role.Button, onClickLabel = "時計") {
                            if (gate.tap(SystemClock.elapsedRealtime())) admin = true
                        }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        now.format(DateTimeFormatter.ofPattern("H:mm", Locale.JAPAN)),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                        letterSpacing = 1.sp,
                        lineHeight = 72.sp,
                    )
                    Text(
                        now.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.JAPAN)),
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HomeTile(
                        "LINE",
                        R.drawable.ic_chat,
                        Color(0xFFE0F1DB),
                        Color(0xFF236334),
                        Modifier.weight(1f),
                    ) {
                        activity.openPackage("jp.naver.line.android", "LINE")
                    }
                    HomeTile(
                        "カメラ",
                        R.drawable.ic_camera,
                        Color(0xFFE1ECF5),
                        Color(0xFF265579),
                        Modifier.weight(1f),
                    ) {
                        activity.open(
                            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                            "カメラを開けませんでした",
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HomeTile(
                        "写真",
                        R.drawable.ic_photo,
                        Color(0xFFFFE8D0),
                        Color(0xFF855022),
                        Modifier.weight(1f),
                    ) {
                        activity.openPackage("com.google.android.apps.photosgo", "Gallery")
                    }
                    HomeTile(
                        "連絡先",
                        R.drawable.ic_person,
                        Color(0xFFEEE4F3),
                        Color(0xFF6A467C),
                        Modifier.weight(1f),
                    ) {
                        activity.open(
                            Intent(Intent.ACTION_VIEW)
                                .setType(ContactsContract.Contacts.CONTENT_TYPE),
                            "連絡先を開けませんでした",
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { activity.open(Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 92.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF235F53)),
                    contentPadding = PaddingValues(20.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_phone), null, Modifier.size(38.dp))
                    Spacer(Modifier.width(18.dp))
                    Text("電話", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Row(
                    Modifier.padding(top = 14.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_battery),
                        "電池残量",
                        Modifier.size(26.dp),
                        tint = if (battery in 0..20) Color(0xFF9D3B23) else Ink,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (battery < 0) "電池 確認中" else "$battery%${if (charging) " ・ 充電中" else ""}",
                        fontSize = 20.sp,
                        color = Ink,
                    )
                }
            }
        }
    }
    if (admin) AdminDialog(activity, onDismiss = { admin = false })
}

@Composable
private fun HomeTile(
    label: String,
    icon: Int,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            modifier
                .heightIn(min = 144.dp)
                .combinedClickable(role = Role.Button, onClick = onClick, onLongClick = {}),
        shape = RoundedCornerShape(28.dp),
        color = background,
        contentColor = foreground,
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(painterResource(icon), null, Modifier.size(48.dp))
            Text(label, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdminDialog(activity: MainActivity, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("家族用の設定") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdminButton("ホームアプリに設定／解除") {
                    onDismiss()
                    activity.chooseHome()
                }
                AdminButton("Android設定") {
                    onDismiss()
                    activity.open(Intent(Settings.ACTION_SETTINGS))
                }
                AdminButton("Wi-Fi") {
                    onDismiss()
                    activity.open(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
                AdminButton("Playストア") {
                    onDismiss()
                    activity.openPackage("com.android.vending", "Playストア")
                }
                Text("ボタンの位置・大きさは固定です。通知欄や他のアプリの操作は制限しません。", fontSize = 13.sp)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("ホームに戻る") } },
    )
}

@Composable
private fun AdminButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        Text(label, fontSize = 17.sp)
    }
}
