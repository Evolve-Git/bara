
package com.evolve.bara

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    var busy = false

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setOnMenuItemClickListener(
                Toolbar.OnMenuItemClickListener { item: MenuItem? ->
                    when (item!!.itemId) {
                        R.id.action_settings -> {
                            val intent = Intent(this@FullscreenActivity, SettingsActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) //This line is optional, better to use it because it won't create multiple instances of the launching Activity.
                            startActivity(intent)
                        }
                    }
                    true
                }
        )

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content)
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        val pref: SharedPreferences = this.getSharedPreferences("settings",Context.MODE_PRIVATE)
        val fab: FloatingActionButton = findViewById(R.id.floatingActionButton)

        var isOn = false

        var metroOn = false
        val mp = MediaPlayer.create(this, R.raw.m1)

        var counter = 0
        var tstr = ""

        fun gen():String {
            val pool = "lrLR"
            var rstr = ""
            for(i in 1..19){
                when (i){
                    5,10,15 -> rstr += " "
                    else -> rstr += pool.random()
                }
            }
            return rstr
        }

        var stimer = object: CountDownTimer((60/pref.getInt("bpm", 90).toFloat()*1000*16).toLong(), (60/pref.getInt("bpm", 90).toFloat()*1000).toLong()){
            override fun onTick(millisUntilFinished: Long) {
                if (metroOn) {
                    mp.start()
                }
                var st_tstr = tstr
                when (counter){
                    4,9,14 -> counter += 1
                    19 -> counter = 0
                }
                st_tstr = st_tstr.replaceRange(counter..counter,"<sup>"+st_tstr[counter]+"</sup>")
                //st_tstr = st_tstr.removeRange(counter..counter)
                GlobalScope.launch(Dispatchers.Main) { // launch a new coroutine in background and continue
                    delay(200)
                    fullscreenContent.setText(HtmlCompat.fromHtml(st_tstr,HtmlCompat.FROM_HTML_MODE_LEGACY))
                }
                //fullscreenContent.setText(HtmlCompat.fromHtml(st_tstr,HtmlCompat.FROM_HTML_MODE_LEGACY))
                //fullscreenContent.text = st_tstr
                //fullscreenContent.text = millisUntilFinished.toString()
                counter += 1
            }
            override fun onFinish() {
                //fullscreenContent.text = gen()
            }
        }

        var timer = object: CountDownTimer((60/pref.getInt("bpm", 90).toFloat()*1000*16).toLong()*pref.getInt("cycles", 10).toLong(),
                                            (60/pref.getInt("bpm", 90).toFloat()*1000*16).toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                //fullscreenContent.text = millisUntilFinished.toString()
                //fullscreenContent.text = gen()
                stimer.cancel()
                tstr = gen()
                counter = 0
                stimer.start()
            }
            override fun onFinish() {
                isOn = false
                fab.setImageResource(android.R.drawable.ic_media_play)
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#669900")))
                fullscreenContent.text = getString(R.string.fin)
            }
        }

        fab.setOnClickListener(){
            if (isOn){
                isOn = false
                stimer.cancel()
                timer.cancel()
                fab.setImageResource(android.R.drawable.ic_media_play)
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#669900")))
                fullscreenContent.text = getString(R.string.main)
            }
            else{
                isOn = true
                fab.setImageResource(android.R.drawable.picture_frame)
                fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#b00020")))
                //fullscreenContent.text = (60/pref.getInt("bpm", 90).toFloat()*1000*16).toLong().toString()
                metroOn = pref.getBoolean("metro", false)
                timer.start()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    private fun onClick(){

    }

}
