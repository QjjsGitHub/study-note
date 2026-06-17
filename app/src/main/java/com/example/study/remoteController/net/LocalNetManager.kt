package com.example.study.remoteController.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket

class LocalNetManager {
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null

    // 服务端启动监听 (端口 10086)
    suspend fun startServer(onConnected: () -> Unit) = withContext(Dispatchers.IO) {
        try {
            val serverSocket = ServerSocket(10086)
            socket = serverSocket.accept() // 阻塞直到客户端连接
            outputStream = DataOutputStream(socket!!.getOutputStream())
            withContext(Dispatchers.Main) { onConnected() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // 客户端连接到服务端
    suspend fun connectToServer(ip: String, onConnected: () -> Unit) = withContext(Dispatchers.IO) {
        try {
            socket = Socket(ip, 10086)
            inputStream = DataInputStream(socket!!.getInputStream())
            withContext(Dispatchers.Main) { onConnected() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // 发送视频帧或控制指令
    // type: 1 = 视频流(H264), 2 = 旋转指令
    fun sendData(type: Int, data: ByteArray) {
        try {
            outputStream?.writeInt(type)
            outputStream?.writeInt(data.size)
            outputStream?.write(data)
            outputStream?.flush()
        } catch (e: Exception) { e.printStackTrace() }
    }

    // 持续接收数据
    suspend fun receiveData(onVideoFrame: (ByteArray) -> Unit, onRotate: (Float) -> Unit) = withContext(Dispatchers.IO) {
        try {
            while (socket?.isConnected == true) {
                val type = inputStream?.readInt() ?: break
                val size = inputStream?.readInt() ?: break
                val data = ByteArray(size)
                inputStream?.readFully(data)

                if (type == 1) {
                    onVideoFrame(data)
                } else if (type == 2) {
                    // 解析旋转角度，假设把 Float 转成了 4 字节 ByteArray 发送
                    val degree = String(data).toFloatOrNull() ?: 0f
                    withContext(Dispatchers.Main) { onRotate(degree) }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}