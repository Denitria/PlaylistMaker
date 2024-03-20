package com.trial.playlistmaker

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.trial.playlistmaker.SearchActivity.Companion.TRACK_VALUE

class TrackHistoryAdapter(private val searchHistoryList: List<Track>
) :
    RecyclerView.Adapter<TrackViewHolder>() {
    private var tracksHistoryList: MutableList<Track> = mutableListOf()
    private val limit = 10
    private var listener: OnTrackClickListener? = null

    fun setOnItemClickListener(listener: OnTrackClickListener) {
        this.listener = listener
    }
    fun updateTracks(newTracks: MutableList<Track>) {
        val oldTracks = tracksHistoryList
        tracksHistoryList.clear()
        tracksHistoryList.addAll(newTracks)

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldTracks.size
            }

            override fun getNewListSize(): Int {
                return newTracks.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldTracks[oldItemPosition].trackId == newTracks[newItemPosition].trackId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldTracks[oldItemPosition] == newTracks[newItemPosition]
            }
        })

        diffResult.dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TrackViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (tracksHistoryList.size > limit) {
            limit
        } else {
            tracksHistoryList.size
        }
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracksHistoryList[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AudioPlayerActivity::class.java)
            val gson = Gson()
            val json = gson.toJson(tracksHistoryList[position])
            intent.putExtra(TRACK_VALUE, json)
            holder.itemView.context.startActivity(intent)
        }
    }
}