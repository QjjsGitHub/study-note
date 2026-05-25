package com.example.study

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.study", appContext.packageName)


        //定义一个长度50的数组，记录花费n抽，抽完三个大奖的次数，默认值0
        val lotteryResults = IntArray(50) { 0 }

        //循环100万次
        for (k in 0 until 1000000) {

            //大奖剩余个数
            var j = 3

            //开抽
            for (i in 0 until 50) {

                //从1到奖池剩余奖励个数中随机一个数,中将之后奖励会减少，所以减i。
                val randomIntValue = Random.nextInt(1, 51 - i)

                if (j == 3) {
                    //一个大奖没中进入判断

                    //假定 1 2 3 为大奖，抽中大奖就减少一个
                    if (randomIntValue == 1 || randomIntValue == 2 || randomIntValue == 3) {
                        j--
                    }
                } else if (j == 2) {
                    //大奖只剩两个了进入判断

                    //假定 1 2 为大奖，抽中大奖就减少一个
                    if (randomIntValue == 1 || randomIntValue == 2) {
                        j--
                    }

                } else if (j == 1) {
                    //大奖只剩一个了进入判断

                    //假定 1 为大奖，抽中大奖就减少一个
                    if (randomIntValue == 1) {
                        //此时三个大奖抽完结束抽奖，记录抽（i+1）次抽完大奖的次数
                        lotteryResults[i] = 1 + lotteryResults[i]
                        break
                    }

                } else {
                    break
                }

            }
        }

        var probabilityResult = ""

        var exceed = 100.000

        var probability = 0.000

        for (i in 0 until 50) {

            probability = (lotteryResults[i] / 10000.000)

            exceed = exceed - probability

            probabilityResult =
                probabilityResult + "次数为" + (i + 1) + "的概率：" + probability + "%" + "\n超过" + exceed + "%玩家" + "\n"

        }

        Log.d("probabilityResult", probabilityResult)

    }
}