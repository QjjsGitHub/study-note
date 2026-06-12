package com.example.study.ui.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.study.MainActivity
import com.example.study.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

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

        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onAttach"
        )
    }

    override fun onStart() {
        super.onStart()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStart"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onCreate"
        )
    }

    override fun onStop() {
        super.onStop()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onStop"
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onPause"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE,
            "Thread: " + Thread.currentThread().name
        )
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onResume"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroy"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDestroyView"
        )
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(
            MainActivity.KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE, javaClass.name.substring
                (javaClass.name.lastIndexOf(".") + 1) + "  :  " + "state" + "  :  " + "onDetach"
        )
    }
}