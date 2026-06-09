package com.example.study.videoPlayer

import android.content.ContentResolver
import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import android.util.Log
import android.util.Size
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 全局缩略图缓存 — 按需加载，自动淘汰，缺了重新拉。
 */
object ThumbnailCache {

    // 20 MB，超出自动淘汰最久未用的；UI 层发现缓存缺失会重新 load
    private val cache = object : LruCache<String, Bitmap>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    /** 同步读缓存（O(1)），可能返回 null */
    fun get(key: String?): Bitmap? = key?.let { cache.get(it) }

    /**
     * 按需加载单张缩略图。先查缓存，未命中则在 IO 线程调 loadThumbnail。
     * 返回 null 表示系统无法生成该视频的缩略图。
     */
    suspend fun load(contentResolver: ContentResolver, path: String): Bitmap? {
        cache.get(path)?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val b = contentResolver.loadThumbnail(path.toUri(), Size(512, 288), null)
                    if (b != null) cache.put(path, b)
                    b
                } else null
            } catch (e: Exception) {
                Log.w(TAG, "缩略图加载失败: $path", e)
                null
            }
        }
    }

    private const val TAG = "ThumbnailCache"
}
