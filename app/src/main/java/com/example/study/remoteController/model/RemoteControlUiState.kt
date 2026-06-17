package com.example.study.remoteController.model

// 定义 UI 状态
data class RemoteControlUiState(
    val isConnected: Boolean = false,
    val isServer: Boolean = false, // 当前设备是服务端还是客户端
    val serverRotation: Float = 0f, // 关键：服务端的旋转角度 (0, 90, 180, 270)
    val errorMessage: String? = null
)

// 定义网络通信接口 (你需要用 Socket 或 WebRTC 来实现它)
interface NetworkRepository {
    fun startServer(port: Int, onClientConnected: () -> Unit)
    fun connectToServer(ip: String, port: Int)

    // 发送与接收旋转指令
    fun sendRotation(degree: Float)
    fun observeRotation(onRotationReceived: (Float) -> Unit)

    // 发送与接收视频流 (H.264)
    fun sendVideoFrame(frameData: ByteArray)
    fun observeVideoFrames(onFrameReceived: (ByteArray) -> Unit)
}