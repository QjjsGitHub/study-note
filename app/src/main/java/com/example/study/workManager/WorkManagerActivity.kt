package com.example.study.workManager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.work.Data
import androidx.work.WorkInfo
import com.example.study.workManager.ui.theme.StudyTheme
import java.util.UUID
import kotlin.getValue


const val TAG: String = "WorkManagerActivityLOG"

class WorkManagerActivity : ComponentActivity() {

    //by 必须是val类型
    private val myViewModel: MyViewModel by viewModels {
        MyViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* var myViewModel: MyViewModel =
             ViewModelProvider(
                 this,
                 MyViewModelFactory(LocalContext.current)
             ).get(MyViewModel::class.java)*/

        enableEdgeToEdge()
        setContent {
            StudyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainUI(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        myViewModel = myViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainUI(name: String, modifier: Modifier = Modifier, myViewModel: MyViewModel) {

    myViewModel
    val myWorkInfos = remember { myViewModel.getMyWorkInfos() }
    var result = remember { mutableStateOf<String>("") }

    var ids: Triple<UUID, UUID, UUID>

    val lifecycleOwner = LocalLifecycleOwner.current

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally)

    {


        Spacer(Modifier.height(36.dp))

        Button(onClick = {

            ids = myViewModel.getData("inputData")

            myViewModel.getMyWorkInfos().removeObservers(lifecycleOwner)


            myViewModel.getMyWorkInfos()
                .observe(lifecycleOwner, Observer() { workInfos ->

                    if (workInfos != null) {

                        for (workInfo in workInfos) {

                            val task =
                                when (workInfo.id) {
                                    ids.first -> "one"
                                    ids.second -> "two"
                                    ids.third -> "third"
                                    else -> "other"
                                }

                            if (task != "other") {

                                when (workInfo.state) {
                                    WorkInfo.State.ENQUEUED -> {
                                        Log.d(TAG, task + "任务已入队")
                                    }

                                    WorkInfo.State.RUNNING ->
                                        Log.d(TAG, task + "任务执行中")

                                    WorkInfo.State.SUCCEEDED -> {
                                        Log.d(TAG, task + "任务成功")

                                        val outputData: Data = workInfo.outputData
                                        val r = outputData.getString("string")
                                        result.value = r ?: "null"
                                    }

                                    WorkInfo.State.FAILED -> {
                                        Log.d(TAG, task + "任务失败")
                                        val outputData: Data = workInfo.outputData
                                        val r = outputData.getString("string")
                                        result.value = r ?: "null"
                                    }

                                    WorkInfo.State.BLOCKED ->
                                        Log.d(TAG, task + "任务被阻塞")

                                    WorkInfo.State.CANCELLED ->
                                        Log.d(TAG, task + "任务已取消")
                                }
                            }
                        }
                    }


                })

        }
        ) {
            Text("获取数据")
        }

        Button(onClick = {

            myViewModel.getData("")

        }) { Text("循环获取数据") }

        Spacer(Modifier.height(36.dp))

        Button(onClick = {

            myViewModel.cancelWork()

        }
        ) {
            Text("取消任务")
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Hello ${result.value}!",
            modifier = Modifier
        )

    }


}

@Preview(showBackground = true)
@Composable
fun MainUIPreview() {
    val context = LocalContext.current
    // Provide a local instance of MyViewModel for the preview to avoid using the uninitialized activity-level lateinit property.
    val myViewModel = remember { MyViewModel(context) }
    StudyTheme {
        MainUI("Android", myViewModel = myViewModel)
    }
}