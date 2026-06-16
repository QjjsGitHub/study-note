package com.example.study.videoPlayer

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.Fetcher
import coil.fetch.FetchResult
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Coil 自定义加载器：视频缩略图抓取器
 * 负责拦截视频 content URI，并使用系统 ContentResolver 或 ThumbnailUtils 生成视频封面。
 */
class VideoThumbnailFetcher(
    private val resolver: ContentResolver,
    private val uri: Uri,
    private val resources: android.content.res.Resources,
    private val options: Options,
) : Fetcher {

    companion object {
        /** 
         * 限制全局并发缩略图生成的数量。
         * 设置为 3 可以确保在快速滑动列表时，系统不会因为瞬间启动大量硬件解码任务而导致 UI 卡顿或 OOM。
         */
        private val semaphore = Semaphore(3)
    }

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        // 使用信号量控制并发，确保资源利用率平稳
        val bitmap: Bitmap? = semaphore.withPermit {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android Q (API 29) 及以上：使用官方推荐的 loadThumbnail 接口
                val size = options.size
                val width = size.width.pxOrElse { 350 }.coerceIn(96, 320)
                val height = size.height.pxOrElse { 200 }.coerceIn(54, 180)
                
                try {
                    resolver.loadThumbnail(uri, Size(width, height), null)
                } catch (_: Exception) {
                    null
                }
            } else {
                // ── Android Q 以下：兼容旧版 API ──────────
                // 旧版本不支持直接从 Uri 加载缩略图，需要先解析出物理文件路径。
                @Suppress("DEPRECATION")
                android.media.ThumbnailUtils.createVideoThumbnail(
                    getPathFromUri(uri) ?: "",
                    MediaStore.Video.Thumbnails.MINI_KIND
                )
            }
        }

        bitmap ?: throw IOException("Could not load thumbnail for $uri")

        DrawableResult(
            drawable = bitmap.toDrawable(resources),
            isSampled = true, // 标记为采样后的图像，优化缓存占用
            dataSource = DataSource.DISK,
        )
    }

    /**
     * 辅助方法：通过 Content URI 查询数据库获取文件的物理路径。
     * 仅用于 Android 10 以下的兼容性逻辑。
     */
    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf<String>(MediaStore.Video.Media.DATA)
        return try {
            resolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** 通过构造函数传入 Context，并存为私有属性，以便在 create 中使用 */
    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(
            data: Uri,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher? {
            if (data.scheme != "content") return null
            return VideoThumbnailFetcher(context.contentResolver, data, context.resources, options)
        }
    }
}

/** 辅助扩展：将 Coil 的 Dimension 转为像素值，如果是 Original 则使用默认值 */
private inline fun coil.size.Dimension.pxOrElse(block: () -> Int): Int {
    return if (this is coil.size.Dimension.Pixels) this.px else block()
}
