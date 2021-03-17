package com.evolve.bara

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity: AppCompatActivity() {
    private lateinit var settingsContent: ScrollView
    private lateinit var settingsContentControls: LinearLayout

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings)
        val toolbar: Toolbar = findViewById(R.id.toolbar3)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        settingsContent = findViewById(R.id.setts)
        settingsContentControls = findViewById(R.id.sets)

        val edit1: TextInputEditText = findViewById(R.id.edit1)
        val edit2: TextInputEditText = findViewById(R.id.edit2)

        val switch: SwitchCompat = findViewById(R.id.switch1)

        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val prefeditor = pref.edit()

        edit1.setText(pref.getInt("cycles", 10).toString())
        edit2.setText(pref.getInt("bpm", 90).toString())

        switch.isChecked = pref.getBoolean("metro", false)

        edit1.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                prefeditor.putInt("cycles", edit1.text.toString().toInt())
                prefeditor.apply()
            }
        }
        edit2.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                prefeditor.putInt("bpm", edit2.text.toString().toInt())
                prefeditor.apply()
            }
        }
        /*edit2.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
                edit2.clearFocus()
                return@OnEditorActionListener true
            }
            false
        })*/
        }

    override fun onPause() {
        super.onPause()
        val edit1: TextInputEditText = findViewById(R.id.edit1)
        val edit2: TextInputEditText = findViewById(R.id.edit2)
        val switch: SwitchCompat = findViewById(R.id.switch1)
        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val prefeditor = pref.edit()
        prefeditor.putInt("cycles", edit1.text.toString().toInt())
        prefeditor.putInt("bpm", edit2.text.toString().toInt())
        prefeditor.putBoolean("metro", switch.isChecked)
        prefeditor.apply()
    }
}
