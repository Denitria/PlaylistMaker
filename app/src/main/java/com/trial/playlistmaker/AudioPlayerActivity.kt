package com.trial.playlistmaker

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        binding.tvTrackYear.text = track.releaseDate.substring(0, 4)
        binding.tvTrackGenre.text = track.genre
        binding.tvTrackCountry.text = track.country

        Glide.with(binding.ivPlayerImage)
            .load(track.artworkUrl100)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(binding.ivPlayerImage, 8f)))
            .placeholder(R.drawable.placeholder_audio_player)
            .into(binding.ivPlayerImage)
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
}


