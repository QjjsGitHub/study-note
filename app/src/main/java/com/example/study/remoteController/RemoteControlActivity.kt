package com.example.study.remoteController

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.study.remoteController.service.ScreenCaptureService
import com.example.study.remoteController.ui.theme.StudyTheme
import com.example.study.remoteController.viewmodel.RemoteControlViewModel
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

class RemoteControlActivity : ComponentActivity() {

    private lateinit var viewModel: RemoteControlViewModel
    private lateinit var mediaProjectionManager: MediaProjectionManager

    // ==========================================
    // 权限回调区域
    // ==========================================

    // 1. Android 13+ 通知权限回调 (前台服务必须要有通知)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "未授予通知权限，服务端可能在后台被杀", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. 控制端：悬浮窗权限回调
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "悬浮窗权限已授予，请再次点击连接", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "未授予悬浮窗权限，无法开启控制端！", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. 🌟 核心修改：服务端录屏权限回调
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // 注意这里：授权成功后，我们不再直接生成 MediaProjection！
            // 而是启动前台服务，让服务去生成，以满足 Android API 29+ 的安全限制。

            val intent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("RESULT_CODE", result.resultCode)
                putExtra("DATA", result.data)
            }

            // 监听服务就绪的回调，拿到 Projection 后再去启动 ViewModel 的推流网络逻辑
            ScreenCaptureService.onServiceStarted = { projection ->
                viewModel.startAsServer(projection)
            }

            // 启动前台服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

        } else {
            Toast.makeText(this, "必须同意录屏权限才能作为服务端！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // 请求 Android 13 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            StudyTheme {
                viewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                val localIp = remember { getLocalIpAddress(context) }
                var ipInput by remember { mutableStateOf("") }

                val floatingHelper = remember {
                    FloatingWindowHelper(context) {
                        // 悬浮窗关闭时的回调
                    }
                }

                // 监听服务端的旋转指令并同步给悬浮窗
                LaunchedEffect(uiState.serverRotation) {
                    if (!uiState.isServer && uiState.isConnected) {
                        floatingHelper.rotate(uiState.serverRotation)
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        floatingHelper.dismiss()
                        // Activity销毁时停止服务
                        stopService(Intent(context, ScreenCaptureService::class.java))
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.fillMaxSize().padding(innerPadding), color = Color(0xFFF3F4F6)) {

                        if (!uiState.isServer) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("局域网悬浮窗远控", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                                Text("状态: ${uiState.message}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))

                                // --- 1. 服务端操作区 ---
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("作为【被控端】(服务端)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column {
                                                Text("本机局域网 IP:", color = Color.Gray, fontSize = 12.sp)
                                                Text(localIp, color = Color(0xFF009688), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            }
                                            IconButton(
                                                onClick = { shareIpAddress(context, localIp) },
                                                modifier = Modifier.background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                                            ) { Icon(Icons.Default.Share, contentDescription = "分享IP", tint = Color(0xFF00796B)) }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent()) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("启动服务端 (准备投屏)") }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // --- 2. 客户端操作区 ---
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("作为【控制端】(客户端)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = ipInput,
                                            onValueChange = { ipInput = it },
                                            label = { Text("输入被控端 IP") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                if (ipInput.isBlank()) {
                                                    Toast.makeText(context, "请输入 IP", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                if (!Settings.canDrawOverlays(context)) {
                                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                                                    overlayPermissionLauncher.launch(intent)
                                                    return@Button
                                                }

                                                floatingHelper.show()
                                                floatingHelper.surfaceView?.let { surface ->
                                                    viewModel.connectAsClient(ipInput, surface.holder.surface)
                                                    Toast.makeText(context, "悬浮窗已开启，可返回桌面观看", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                                        ) { Text("开启悬浮窗并连接") }
                                    }
                                }
                            }
                        } else {
                            // 服务端：投屏中控制面板
                            Column(modifier = Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("正在作为被控端投屏中...", color = Color.White, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(32.dp))
                                Text("模拟物理屏幕旋转：", color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(onClick = { viewModel.sendRotation(0f) }) { Text("竖屏 (0°)") }
                                    Button(onClick = { viewModel.sendRotation(90f) }) { Text("横屏 (90°)") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLocalIpAddress(context: Context): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return if (ipAddress == 0) {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                var fallbackIp = "请连接WiFi"
                for (intf in interfaces) {
                    for (addr in intf.inetAddresses) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            fallbackIp = addr.hostAddress ?: "未知IP"
                        }
                    }
                }
                fallbackIp
            } catch (ex: Exception) { "解析失败" }
        } else {
            String.format(Locale.getDefault(), "%d.%d.%d.%d", ipAddress and 0xff, ipAddress shr 8 and 0xff, ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff)
        }
    }

    private fun shareIpAddress(context: Context, ip: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "我的投屏服务端IP是: $ip")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "分享 IP"))
    }
}