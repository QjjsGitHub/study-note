package com.example.study.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class FlowViewModel : ViewModel() {

    // 输入框内容（StateFlow）
    val query = MutableStateFlow("")

    // 搜索结果 Flow
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResult: StateFlow<List<String>> = query
        .debounce(300)                 // 防抖：停止输入 300ms 才搜索
        .distinctUntilChanged()        // 输入一样就不重复搜索
        .flatMapLatest { text ->       // 新输入来了就取消旧搜索
            search(text)               // suspend 函数
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    // 模拟搜索 API
    private fun search(text: String): Flow<List<String>> = flow {
        delay(200) // 模拟网络延迟
        emit(listOf("结果1: $text", "结果2: $text"))
    }

}