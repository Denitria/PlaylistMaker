package com.trial.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val settingsButton = findViewById<Button>(R.id.buttonSettings)
        settingsButton.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        val mediatekaButton = findViewById<Button>(R.id.buttonMediateka)
        mediatekaButton.setOnClickListener {
            val mediatekaIntent = Intent(this, MediatekaActivity::class.java)
            startActivity(mediatekaIntent)
        }

        val searchButton = findViewById<Button>(R.id.buttonSearch)
        searchButton.setOnClickListener {
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
        }

    }
}
