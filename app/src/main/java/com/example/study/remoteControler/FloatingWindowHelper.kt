package com.example.study.remoteControler

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.graphics.Color

class FloatingWindowHelper(
    private val context: Context,
    private val onClose: () -> Unit
) {
    private var windowManager: WindowManager? = null
    private var floatingContainer: FrameLayout? = null
    private var surfaceContainer: FrameLayout? = null

    var surfaceView: SurfaceView? = null
        private set

    fun show() {
        if (!Settings.canDrawOverlays(context)) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 1. 悬浮窗外层容器 (负责拖拽和调整大小)
        floatingContainer = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#333333")) // 深色边框
            setPadding(4, 4, 4, 4)
        }

        // 2. 画面容器 (负责旋转)
        surfaceContainer = FrameLayout(context)
        surfaceView = SurfaceView(context)
        surfaceContainer?.addView(surfaceView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        floatingContainer?.addView(surfaceContainer)

        // 3. 关闭按钮
        val closeBtn = Button(context).apply {
            text = "关闭"
            textSize = 12f
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
            setOnClickListener { dismiss() }
        }
        floatingContainer?.addView(closeBtn, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.TOP or Gravity.END })

        // 4. 配置 WindowManager 参数
        val layoutParams = WindowManager.LayoutParams(
            450, 800, // 初始尺寸 (竖屏)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // 5. 实现拖拽逻辑
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        floatingContainer?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingContainer, layoutParams)
                    true
                }
                else -> false
            }
        }

        windowManager?.addView(floatingContainer, layoutParams)
    }

    // 核心：处理来自服务端的旋转指令
    fun rotate(degree: Float) {
        // 1. 让包含 SurfaceView 的容器旋转
        surfaceContainer?.animate()?.rotation(degree)?.setDuration(300)?.start()

        // 2. 动态调整悬浮窗的宽高比例，使其适应横屏或竖屏
        floatingContainer?.let { container ->
            val params = container.layoutParams as WindowManager.LayoutParams
            if (degree == 90f || degree == 270f) {
                params.width = 800 // 横屏宽度
                params.height = 450 // 横屏高度
            } else {
                params.width = 450 // 竖屏宽度
                params.height = 800 // 竖屏高度
            }
            windowManager?.updateViewLayout(container, params)
        }
    }

    fun dismiss() {
        floatingContainer?.let {
            windowManager?.removeView(it)
            floatingContainer = null
        }
        onClose()
    }
}