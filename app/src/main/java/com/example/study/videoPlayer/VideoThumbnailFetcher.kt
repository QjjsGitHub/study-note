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
 * Coil Fetcher：将 video content URI 转成 [ContentResolver.loadThumbnail] 缩略图。
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
         * 设置为 3 可以确保不会在滑动时瞬间压死磁盘 IO 和解码器。
         */
        private val semaphore = Semaphore(1)
    }

    override suspend fun fetch(): FetchResult = withContext(Dispatchers.IO) {
        // 使用信号量控制并发，实现“一个一个（或少量并行）”加载
        val bitmap: Bitmap? = semaphore.withPermit {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val size = options.size
                val width = size.width.pxOrElse { 350 }.coerceIn(96, 320)
                val height = size.height.pxOrElse { 200 }.coerceIn(54, 180)
                
                try {
                    resolver.loadThumbnail(uri, Size(width, height), null)
                } catch (_: Exception) {
                    null
                }
            } else {
                // Android Q 以下的兼容处理
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
            isSampled = true,
            dataSource = DataSource.DISK,
        )
    }

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
