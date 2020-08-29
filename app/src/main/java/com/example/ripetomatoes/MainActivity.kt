package com.example.ripetomatoes

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.ripetomatoes.ui.main.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {

    var jwtToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    fun showToast(msg: String, error: Boolean = false) {
        val toast = Toast.makeText(
            this.applicationContext,
            msg,
            Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP, 0, 0)
        val bgColor = if (error) Color.RED else Color.GREEN
        toast.view?.background?.setColorFilter(bgColor, PorterDuff.Mode.SRC_IN)
        toast.show()
    }
}

class RequestManager constructor(context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: RequestManager? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestManager(context).also {
                    INSTANCE = it
                }
            }
    }
    
    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }
    
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }
}
