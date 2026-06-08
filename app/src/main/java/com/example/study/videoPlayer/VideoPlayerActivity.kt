package com.example.study.videoPlayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.study.videoPlayer.model.VideoItem
import com.example.study.videoPlayer.ui.screens.VideoListScreen
import com.example.study.videoPlayer.ui.screens.VideoPlayerScreen
import com.example.study.videoPlayer.ui.theme.VideoPlayerTheme

class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerTheme {
                var currentVideo by remember { mutableStateOf<VideoItem?>(null) }

                AnimatedContent(
                    targetState = currentVideo,
                    transitionSpec = {
                        if (targetState != null) {
                            // 进入播放器：从右侧滑入
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it / 3 } + fadeOut())
                        } else {
                            // 返回列表：从左侧滑入
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it / 3 } + fadeOut())
                        }
                    },
                    label = "video_navigation"
                ) { video ->
                    if (video != null) {
                        VideoPlayerScreen(
                            video = video,
                            onBack = { currentVideo = null }
                        )
                    } else {
                        VideoListScreen(
                            onVideoClick = { currentVideo = it }
                        )
                    }
                }
            }
        }
    }
}
