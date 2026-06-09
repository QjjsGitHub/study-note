package com.example.study.videoPlayer.ui.screens

import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.theme.VideoAccent
import com.example.study.videoPlayer.ui.theme.VideoControlBg
import com.example.study.videoPlayer.ui.theme.VideoOnSurfaceVariant
import com.example.study.videoPlayer.ui.theme.VideoPrimary
import com.example.study.videoPlayer.ui.theme.VideoSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 缩略图内存缓存，按 KB 计算单张占用，最多缓存 50 张 */
private val thumbnailCache = object : LruCache<String, Bitmap>(50) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onRefresh: () -> Unit = {},
    onScan: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = VideoPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "本地视频",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 搜索 */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("刷新列表") },
                                onClick = {
                                    showMenu = false
                                    onRefresh()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("排序方式") },
                                onClick = { showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("扫描目录") },
                                onClick = {
                                    showMenu = false
                                    onScan()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (videos.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = VideoOnSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无本地视频",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VideoOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右上角「扫描目录」查找视频文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = VideoOnSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 头部统计信息
                item(span = { GridItemSpan(2) }) {
                    StorageInfoHeader(videos)
                }
                // 视频列表
                items(videos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) }
                    )
                }
                // 底部间距
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } // end else
    }
}

@Composable
private fun StorageInfoHeader(videos: List<VideoItem>) {
    // remember 缓存 sumOf 结果，避免每次重组都遍历全列表
    val totalCount = videos.size
    val totalGb = remember(videos) {
        val totalBytes = videos.sumOf { it.fileSizeBytes }
        totalBytes / (1024.0 * 1024.0 * 1024.0)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VideoSurfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "共 $totalCount 个视频",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "总大小 %.1f GB".format(totalGb),
                style = MaterialTheme.typography.bodySmall,
                color = VideoOnSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "本地存储",
            style = MaterialTheme.typography.labelSmall,
            color = VideoPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(VideoPrimary.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun VideoCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VideoSurfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
    ) {
        Column {
            // 缩略图占位区域
            //ThumbnailPlaceholder(video)

            // 视频信息
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.formattedDuration,
                        style = MaterialTheme.typography.labelSmall,
                        color = VideoOnSurfaceVariant
                    )
                    Text(
                        text = " · ",
                        style = MaterialTheme.typography.labelSmall,
                        color = VideoOnSurfaceVariant
                    )
                    Text(
                        text = video.formattedSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = VideoOnSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = video.resolution,
                    style = MaterialTheme.typography.labelSmall,
                    color = VideoPrimary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ThumbnailPlaceholder(video: VideoItem) {
    val context = LocalContext.current
    var thumbnail by remember(video.thumbnailPath) { mutableStateOf<Bitmap?>(null) }

    // 异步加载视频缩略图
    LaunchedEffect(video.thumbnailPath) {
        val path = video.thumbnailPath ?: return@LaunchedEffect

        // 1. 先查内存缓存
        thumbnailCache.get(path)?.let {
            thumbnail = it
            return@LaunchedEffect
        }

        // 2. 缓存未命中，在 IO 线程加载
        val bitmap = withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(
                        path.toUri(),
                        Size(512, 288),
                        null
                    )
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }

        // 3. 写缓存 + 更新 UI
        if (bitmap != null) {
            thumbnailCache.put(path, bitmap)
            thumbnail = bitmap
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(VideoSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // 视频缩略图
        val bitmap = thumbnail
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 播放图标（有缩略图时半透明叠加）
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = if (bitmap != null) Color.White.copy(alpha = 0.9f) else VideoPrimary.copy(alpha = 0.7f),
            modifier = Modifier.size(40.dp)
        )

        // 时长标签（右下角）
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(VideoControlBg)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = video.formattedDuration,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // 分辨率标签（左上角）
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(VideoAccent.copy(alpha = 0.8f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = video.resolution,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
