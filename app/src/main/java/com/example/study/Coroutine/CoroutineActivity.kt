package com.example.study.Coroutine

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.study.compose.ui.theme.StudyTheme
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import androidx.core.content.edit
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive

const val TAG: String = "CoroutineActivityLOG"
lateinit var job1: Job

class CoroutineActivity : ComponentActivity() {

    //val l = lifecycleScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sp = getSharedPreferences("mySp", MODE_PRIVATE)

        sp.edit {

            putString("name", "Ming")

            apply()

        }

        val age = sp.getInt("age", 18)

        val name = sp.getString("name", "null")

        Log.d(TAG, "name: " + name + "age: " + age)

        setContent {
            MainUi()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::job1.isInitialized) job1.cancel()
    }

}

suspend fun getData(): String {

    Log.d(TAG, "suspend:" + Thread.currentThread().name)


    // 5 秒超时

    val s = withContext(Dispatchers.IO) {

        try {
            withTimeout(5000) {
                repeat(3) {
                    Log.d(TAG, "suspend:" + Thread.currentThread().name)
                    delay(300)
                }



                "计算完毕"
            }
        } catch (e: TimeoutCancellationException) {
            "计算失败"
        }

    }

    return "result:$s"

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainUi() {

    val context = LocalContext.current
    val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope

    /*
    它的核心作用是：在 Composable 函数内部获取一个与当前组合生命周期绑定的
     CoroutineScope，以便在非挂起环境（如点击回调、普通逻辑块）中启动协程
     */
    val mySpace = rememberCoroutineScope()

    StudyTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(" 测试应用")
                    }, navigationIcon = {
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(Icons.Filled.Menu, null)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(Icons.Filled.Share, null)
                        }
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(Icons.Filled.Settings, null)
                        }
                    })
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* 操作 */

                        //子协程失败不影响其他协程
                        suspend fun example() = supervisorScope {
                            launch { throw RuntimeException(TAG + "子协程1失败") } // 不影响 others
                            launch { delay(1000); println(TAG + "子协程2成功完成") } // 会执行
                        }

                        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                            println("Caught exception: ${throwable.message}")
                        }



                        job1 = CoroutineScope(Dispatchers.Main).launch(exceptionHandler) {

                            example()

                            Log.d(TAG, "main:" + Thread.currentThread().name)

                            //delay(1000)

                            //throw RuntimeException(TAG + "子协程1失败")

                            val s1 = async { getData() }

                            val s2 = async { getData() }

                            // 如果 s1 失败，s2 会被自动取消 ✅
                            // 如果外部协程被取消，两个子协程都会被取消 ✅

                            val t = Triple(s1.await(), s2.await(), null)

                            Toast.makeText(context, t.first + t.second, Toast.LENGTH_SHORT)
                                .show()

                            var a = 0
                            var b = 0

                            //在协程空间内，执行1个或多个withContext是顺序同步执行的

                            withContext(Dispatchers.IO) {
                                if (!isActive) return@withContext
                                //cancel()
                                Log.d(
                                    TAG,
                                    "a1:" + Thread.currentThread().name + System.currentTimeMillis()
                                        .toString()
                                )
                                a = 1
                                delay(2000)
                                Log.d(
                                    TAG,
                                    "a2:" + Thread.currentThread().name + System.currentTimeMillis()
                                        .toString()
                                )
                            }

                            withContext(Dispatchers.IO) {
                                Log.d(
                                    TAG,
                                    "b1:" + Thread.currentThread().name + System.currentTimeMillis()
                                        .toString()
                                )
                                b = 2
                                Log.d(
                                    TAG,
                                    "b2:" + Thread.currentThread().name + System.currentTimeMillis()
                                        .toString()
                                )
                            }

                            Log.d(
                                TAG,
                                "t1:" + Thread.currentThread().name + System.currentTimeMillis()
                                    .toString()
                            )
                            Toast.makeText(context, "a+b= $a$b", Toast.LENGTH_SHORT)
                                .show()
                            Log.d(
                                TAG,
                                "t2:" + Thread.currentThread().name + System.currentTimeMillis()
                                    .toString()
                            )

                        }


                        lifecycleScope.launch(Dispatchers.Main) {

                            val s = "立即执行"
                            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()

                        }

                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加")
                }
            }
        ) { paddingValues ->

            val state = remember { mutableStateOf(0) }

            LaunchedEffect(state.value) {
                if (state.value > 0) {
                    val s = "午时已到"
                    Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {


                val textValue = remember { "111" }

                Text(
                    text = textValue,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Button(onClick = {

                    mySpace.launch(Dispatchers.IO) {
                        repeat(10) {
                            delay(5000)
                            state.value++
                            Log.d(TAG, TAG + state)
                        }
                    }

                }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("点击我")
                }


            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainUiPreview() {
    MainUi()
}