package com.example.study.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.study.Coroutine.CoroutineActivity
import com.example.study.Handle.HandleActivity
import com.example.study.MainActivity
import com.example.study.activityMode.JavaActivity
import com.example.study.compose.ComposeActivity
import com.example.study.dataTransform.DataActivity
import com.example.study.databinding.FragmentHomeBinding
import com.example.study.fragment.BottomFragmentActivity
import com.example.study.fragment.FragmentActivity
import com.example.study.internet.InternetActivity
import com.example.study.recyclerView.RecyclerViewActivity
import com.example.study.remoteController.RemoteControlActivity
import com.example.study.ui.NormalActivity
import com.example.study.videoPlayer.VideoPlayerActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreateView"
        )

        val stringLengthFunc: (String) -> Int = { input ->
            input.length
        }

        var a: Int = stringLengthFunc("11")

        /*val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/

        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        val button: Button = binding.button
        /*
                button.setOnClickListener(
                    View.OnClickListener() {
                        fun onClick(v: View?) {
                            val intent: Intent = Intent(context, NormalActivity::class.java)
                            val bundle: Bundle = Bundle()
                            bundle.putInt("age", 18)

                            intent.putExtra("test", bundle)
                            startActivity(intent)
                            //startActivity(Intent(context,NormalActivity::class.java))
                        }
                    }
                )*/


        button.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent: Intent = Intent(context, NormalActivity::class.java)
                    val bundle: Bundle = Bundle()
                    bundle.putInt("age", 18)

                    intent.putExtra("test", bundle)
                    startActivity(intent)
                    //startActivity(Intent(context,NormalActivity::class.java))
                }
            }
        )

        binding.button43.setOnClickListener {
            startActivity(Intent(context, ComposeActivity::class.java))
        }

        binding.button1.setOnClickListener() {
            startActivity(Intent(context, JavaActivity::class.java))
        }

        binding.button9.setOnClickListener() {
            startActivity(Intent(context, FragmentActivity::class.java))
        }

        binding.button10.setOnClickListener() {
            startActivity(Intent(context, BottomFragmentActivity::class.java))
        }

        binding.button12.setOnClickListener() {

            val intent: Intent = Intent().setComponent(
                ComponentName(
                    "com.example.study",
                    "com.example.study.service.ServiceActivity"
                )
            )
            startActivity(intent)
        }

        binding.button49.setOnClickListener() {

            val intent: Intent = Intent().setComponent(
                ComponentName(
                    "com.example.study",
                    "com.example.study.workManager.WorkManagerActivity"
                )
            )
            startActivity(intent)
        }

        /* 线程 */
        binding.button19.setOnClickListener() {

            val intent: Intent = Intent().setComponent(
                ComponentName(
                    "com.example.study",
                    "com.example.study.multiThread.ThreadActivity"
                )
            )
            startActivity(intent)
        }

        binding.button25.setOnClickListener() {

            val intent: Intent = Intent().setComponent(
                ComponentName(
                    "com.example.study",
                    "com.example.study.broadcast.BroadcastActivity"
                )
            )
            startActivity(intent)
        }

        binding.button31.setOnClickListener() {

            val intent: Intent = Intent(context, HandleActivity::class.java)
            startActivity(intent)
        }

        binding.button33.setOnClickListener() {

            val intent: Intent = Intent(context, InternetActivity::class.java)
            startActivity(intent)
        }

        binding.button34.setOnClickListener() {

            val intent: Intent = Intent(context, RecyclerViewActivity::class.java)
            startActivity(intent)
        }

        binding.button44.setOnClickListener() {

            val intent: Intent = Intent(context, CoroutineActivity::class.java)
            startActivity(intent)
        }

        binding.button45.setOnClickListener() {

            val intent: Intent = Intent(context, DataActivity::class.java)
            startActivity(intent)
        }

        binding.button50.setOnClickListener { toFlow() }

        binding.button51.setOnClickListener {
            val intent: Intent = Intent(context, RemoteControlActivity::class.java)
            startActivity(intent)
        }

        binding.buttonVideoPlayer.setOnClickListener {
            startActivity(Intent(context, VideoPlayerActivity::class.java))
        }

        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreateViewOver"
        )
        return root
    }


    override fun onAttach(context: Context) {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onAttach"
        )
        super.onAttach(context)
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onAttachOver"
        )
    }

    override fun onStart() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStart"
        )
        super.onStart()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStartOver"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreate"
        )
        super.onCreate(savedInstanceState)
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreateOver"
        )
    }

    override fun onStop() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStop"
        )
        super.onStop()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStopOver"
        )
    }

    override fun onPause() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onPause"
        )
        super.onPause()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onPauseOver"
        )
    }

    override fun onResume() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onResume"
        )
        super.onResume()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onResumeOver"
        )
    }

    override fun onDestroy() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroy"
        )
        super.onDestroy()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroyOver"
        )
    }

    override fun onDestroyView() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroyView"
        )
        super.onDestroyView()
        _binding = null
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE,
            "Thread: " + Thread.currentThread().name
        )
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroyViewOver"
        )
    }

    override fun onDetach() {
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDetach"
        )
        super.onDetach()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDetachOver"
        )
    }

    fun toFlow() {

        val intent: Intent = Intent().setComponent(
            ComponentName(
                "com.example.study",
                "com.example.study.flow.FlowActivity"
            )
        )
        startActivity(intent)

    }
}