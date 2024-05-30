package com.trial.playlistmaker.presentation.ui.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.trial.playlistmaker.Creator
import com.trial.playlistmaker.R
import com.trial.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.trial.playlistmaker.domain.models.Track
import com.trial.playlistmaker.presentation.ui.search.SearchActivity.Companion.TRACK_VALUE
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding
    private var playerState = STATE_DEFAULT
    private val handler = Handler(Looper.getMainLooper())
    private val playerInteractor = Creator.providePlayerInteractor()
    private val timerRunnable = object : Runnable {
        override fun run() {
            binding.tvTrackTimeRemaining.text = SimpleDateFormat(
                "mm:ss", Locale.getDefault()
            ).format(playerInteractor.getCurrentPosition())
            handler.postDelayed(this, DELAY_MILLIS)
        }
    }
    private val getStateRunnable = object : Runnable {
        override fun run() {
            playerState = playerInteractor.getState()
            when (playerState) {
                STATE_PLAYING, STATE_DEFAULT -> {
                    handler.postDelayed(this, DELAY_MILLIS)
                }

                STATE_PREPARED -> {
                    binding.ivPlayButton.setImageResource(R.drawable.button_play)
                    handler.removeCallbacks(timerRunnable)
                    binding.tvTrackTimeRemaining.text = getText(R.string.time)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener { finish() }

        val gson = Gson()
        val json = intent.getStringExtra(TRACK_VALUE)
        val track = gson.fromJson(json, Track::class.java)
        track.artworkUrl100 = track.getCoverArtwork()

        @SuppressLint("SimpleDateFormat")

        binding.tvTrackName.text = track.trackName
        binding.tvArtistName.text = track.artistName
        binding.tvTrackDuration.text = formatMilliseconds(track.trackTime.toLong())
        binding.tvTrackAlbum.text = track.collectionName
        try {
            binding.tvTrackYear.text = track.releaseDate.substring(0, 4)
        } catch (e: Exception) {
            binding.tvTrackYear.text = "-"
        }
        binding.tvTrackGenre.text = track.genre
        binding.tvTrackCountry.text = track.country

        Glide.with(binding.ivPlayerImage).load(track.artworkUrl100).centerCrop()
            .transform(RoundedCorners(dpToPx(binding.ivPlayerImage, 8f)))
            .placeholder(R.drawable.placeholder_audio_player).into(binding.ivPlayerImage)

        if (track.previewUrl.isNotEmpty()) {
            playerInteractor.preparePlayer(track.previewUrl)
            handler.post(getStateRunnable)

            binding.ivPlayButton.setOnClickListener {
                playerInteractor.playbackControl()
                playerState = playerInteractor.getState()
                when (playerState) {
                    STATE_PLAYING -> {
                        binding.ivPlayButton.setImageResource(R.drawable.pause)
                        handler.post(timerRunnable)
                        handler.post(getStateRunnable)
                    }

                    STATE_PAUSED -> {
                        pause()
                    }
                }
            }
        }
    }

    private fun pause() {
        binding.ivPlayButton.setImageResource(R.drawable.button_play)
        handler.removeCallbacks(getStateRunnable)
        handler.removeCallbacks(timerRunnable)
    }

    override fun onPause() {
        super.onPause()
        pause()
        playerInteractor.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerInteractor.releasePlayer()
    }

    private fun formatMilliseconds(milliseconds: Long): String {
        val format = SimpleDateFormat("mm:ss", Locale.getDefault())
        return format.format(Date(milliseconds))
    }

    private fun dpToPx(view: View, dp: Float): Int {
        val displayMetrics = view.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics).toInt()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY_MILLIS = 300L
    }
}