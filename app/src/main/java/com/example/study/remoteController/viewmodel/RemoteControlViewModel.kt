package com.example.study.remoteController.viewmodel

import android.media.projection.MediaProjection
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.study.remoteController.media.H264Decoder
import com.example.study.remoteController.media.H264Encoder
import com.example.study.remoteController.net.LocalNetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val isConnected: Boolean = false,
    val isServer: Boolean = false,
    val serverRotation: Float = 0f,
    val message: String = "输入服务端IP进行连接"
)

class RemoteControlViewModel : ViewModel() {
    private val netManager = LocalNetManager()
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var decoder: H264Decoder? = null
    private var encoder: H264Encoder? = null

    // 客户端行为：连接服务端并准备解码
    fun connectAsClient(ip: String, surface: Surface) {
        _uiState.update { it.copy(message = "连接中...") }
        viewModelScope.launch {
            netManager.connectToServer(ip) {
                _uiState.update { it.copy(isConnected = true, isServer = false) }
                // 初始化解码器，绑定 SurfaceView
                decoder = H264Decoder(surface)

                // 开始接收数据
                viewModelScope.launch {
                    netManager.receiveData(
                        onVideoFrame = { frame -> decoder?.decodeFrame(frame) },
                        onRotate = { degree -> _uiState.update { it.copy(serverRotation = degree) } }
                    )
                }
            }
        }
    }

    // 服务端行为：开启监听并准备编码推流
    fun startAsServer(mediaProjection: MediaProjection) {
        _uiState.update { it.copy(message = "等待客户端连接...") }
        viewModelScope.launch {
            netManager.startServer {
                _uiState.update { it.copy(isConnected = true, isServer = true) }
                // 初始化编码器，并开始捕获屏幕
                encoder = H264Encoder(mediaProjection, onFrameEncoded = { frame ->
                    // 通过 Socket 发送视频流 (Type = 1)
                    netManager.sendData(1, frame)
                })
                encoder?.start()
            }
        }
    }

    // 服务端物理屏幕旋转时调用此方法同步给客户端
    fun sendRotation(degree: Float) {
        if (_uiState.value.isServer && _uiState.value.isConnected) {
            val degreeBytes = degree.toString().toByteArray()
            viewModelScope.launch(Dispatchers.IO) {
                netManager.sendData(2, degreeBytes) // 发送旋转指令 (Type = 2)
            }
        }
    }
}