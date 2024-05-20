package com.trial.playlistmaker

import android.annotation.SuppressLint
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.trial.playlistmaker.SearchActivity.Companion.TRACK_VALUE
import com.trial.playlistmaker.databinding.ActivityAudioPlayerBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding
    private var playerState = STATE_DEFAULT
    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

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

        Glide.with(binding.ivPlayerImage)
            .load(track.artworkUrl100)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(binding.ivPlayerImage, 8f)))
            .placeholder(R.drawable.placeholder_audio_player)
            .into(binding.ivPlayerImage)

        if (!track.previewUrl.isNullOrEmpty()) {
            preparePlayer(track.previewUrl)
            timerRunnable = timerRunnable()
            binding.ivPlayButton.setOnClickListener {
                playbackControl()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        mediaPlayer.release()
    }

    private fun preparePlayer(previewUrl: String) {
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            binding.ivPlayButton.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(timerRunnable)
            binding.ivPlayButton.setImageResource(R.drawable.button_play)
            playerState = STATE_PREPARED
            binding.tvTrackTimeRemaining.text = getText(R.string.time)
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        binding.ivPlayButton.setImageResource(R.drawable.pause)
        playerState = STATE_PLAYING
        handler.removeCallbacks(timerRunnable)
        handler.post(timerRunnable)
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        binding.ivPlayButton.setImageResource(R.drawable.button_play)
        playerState = STATE_PAUSED
        handler.removeCallbacks(timerRunnable)
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun timerRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                binding.tvTrackTimeRemaining.text =
                    SimpleDateFormat(
                        "mm:ss",
                        Locale.getDefault()
                    ).format(mediaPlayer.currentPosition)
                handler.postDelayed(this, DELAY_MILLIS)
            }
        }
    }

    private fun formatMilliseconds(milliseconds: Long): String {
        val format = SimpleDateFormat("mm:ss", Locale.getDefault())
        return format.format(Date(milliseconds))
    }

    private fun dpToPx(view: View, dp: Float): Int {
        val displayMetrics = view.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
            .toInt()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY_MILLIS = 300L
    }
}


