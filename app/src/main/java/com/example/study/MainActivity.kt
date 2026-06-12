package com.example.study

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.study.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        const val KOTLIN_FRAGMENT_AND_ACTIVITY_LIFE: String = "KotlinFragmentAndActivityLife"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(
            "ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " + javaClass.name + "onCreate"
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        //悬浮按钮
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()


        }


        val drawerLayout: DrawerLayout = binding.drawerLayout

        //侧边栏布局
        val navView: NavigationView = binding.navView

        //fragment布局
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )

        //toolbar自动更新标题
        setupActionBarWithNavController(navController, appBarConfiguration)

        //导航布局
        navView.setupWithNavController(navController)

        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onCreateOver")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onStart")
        super.onStart()
        Tools.getInstance().getTaskInfo(WeakReference(this))
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onStartOver")
    }

    override fun onNewIntent(intent: Intent) {
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onNewIntent")
        super.onNewIntent(intent)
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onNewIntentOver")
    }

    override fun onRestart() {
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onRestart")
        super.onRestart()
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onRestartOver")
    }

    override fun onResume() {
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onResume")
        super.onResume()
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onResumeOver")
    }

    override fun onPause() {
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onPause")
        super.onPause()
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onPauseOver")
    }

    override fun onStop() {
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onStop")
        super.onStop()
        Log.d("ActivityLife",
            "Thread:" + Thread.currentThread().name + "  " +javaClass.name + "onStopOver")
    }


}