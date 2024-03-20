package com.trial.playlistmaker

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import java.util.Locale

class TrackAdapter(private val data: ArrayList<Track>,
                   private val onTrackClickListener: OnTrackClickListener
) :
    RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TrackViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data!!.size
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(data!![position])
        holder.itemView.setOnClickListener {
            onTrackClickListener.onTrackClick(data[holder.adapterPosition])
            val context = holder.itemView.context
            val audioPlayerIntent = Intent(context, AudioPlayerActivity::class.java)
            val gson = Gson()
            val json = gson.toJson(data[position])
            context.startActivity(audioPlayerIntent.putExtra(SearchActivity.TRACK_VALUE, json))
        }

    }
}
class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackName: TextView = itemView.findViewById(R.id.trackName)
    private val trackImage: ImageView = itemView.findViewById(R.id.trackImage)
    private val trackTime: TextView = itemView.findViewById(R.id.trackTime)
    private val trackArtist: TextView = itemView.findViewById(R.id.artistName)

    fun bind(model: Track) {
        trackName.text = model.trackName
        trackArtist.text = model.artistName
        if (!model.trackTime.isNullOrBlank()) {
            trackTime.text =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTime.toLong())
        } else {
            trackTime.text = "—"
        }
        Glide.with(this.itemView.context)
            .load(model.artworkUrl100)
            .fitCenter().dontAnimate()
            .placeholder(R.drawable.placeholder)
            .transform(RoundedCorners(dpToPx(2f, itemView.context))).into(trackImage)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics
        ).toInt()
    }
}