package com.trial.playlistmaker.domain.api

import com.trial.playlistmaker.domain.models.Track

interface OnTrackClickListener {
    fun onTrackClick(track: Track)
}