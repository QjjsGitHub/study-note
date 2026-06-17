package com.example.study.remoteControler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.net.wifi.WifiManager
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.study.remoteControler.ui.theme.StudyTheme
import com.example.study.remoteControler.viewmodel.RemoteControlViewModel
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

class RemoteControlActivity : ComponentActivity() {

    private lateinit var viewModel: RemoteControlViewModel
    private lateinit var mediaProjectionManager: MediaProjectionManager

    // 服务端：录屏权限回调
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val mediaProjection = mediaProjectionManager.getMediaProjection(result.resultCode, result.data!!)
            mediaProjection?.let { viewModel.startAsServer(it) }
        } else {
            Toast.makeText(this, "必须同意录屏权限才能作为服务端！", Toast.LENGTH_SHORT).show()
        }
    }

    // 客户端：悬浮窗权限回调
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "悬浮窗权限已授予，请再次点击连接", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "未授予悬浮窗权限，无法开启控制端！", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        setContent {
            StudyTheme {
                viewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                val localIp = remember { getLocalIpAddress(context) }
                var ipInput by remember { mutableStateOf("") }

                // 初始化悬浮窗管理器
                val floatingHelper = remember {
                    FloatingWindowHelper(context) {
                        // 当悬浮窗关闭时，断开客户端连接 (需在ViewModel中实现disconnect)
                        // viewModel.disconnect()
                    }
                }

                // 监听服务端的旋转指令并同步给悬浮窗
                LaunchedEffect(uiState.serverRotation) {
                    if (!uiState.isServer && uiState.isConnected) {
                        floatingHelper.rotate(uiState.serverRotation)
                    }
                }

                // 页面销毁时清理悬浮窗
                DisposableEffect(Unit) {
                    onDispose { floatingHelper.dismiss() }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.fillMaxSize().padding(innerPadding), color = Color(0xFFF3F4F6)) {

                        if (!uiState.isServer) {
                            // ==========================================
                            // 未连接 / 准备面板
                            // ==========================================
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
                                                // 检查悬浮窗权限
                                                if (!Settings.canDrawOverlays(context)) {
                                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                                                    overlayPermissionLauncher.launch(intent)
                                                    return@Button
                                                }

                                                // 弹出悬浮窗，并将悬浮窗里的 Surface 交给解码器渲染！
                                                floatingHelper.show()
                                                floatingHelper.surfaceView?.let { surface ->
                                                    viewModel.connectAsClient(ipInput, surface.holder.surface)
                                                    // 提示用户可以切到后台了
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
                            // ==========================================
                            // 服务端：投屏中控制面板
                            // ==========================================
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