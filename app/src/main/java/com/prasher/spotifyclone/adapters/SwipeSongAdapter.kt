package com.prasher.spotifyclone.adapters

import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.data.entities.Song
import timber.log.Timber

class SwipeSongAdapter : BaseSongAdapter(R.layout.swipe_item){

    override val differ =  AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val tvPrimarySwipe : TextView? = holder.itemView.findViewById(R.id.tvPrimarySwipe)
        val tvSecondarySwipe : TextView? = holder.itemView.findViewById(R.id.tvSecondarySwipe)

        val song = songs[position]
        holder.apply {
            tvPrimarySwipe?.text = song.title
            tvSecondarySwipe?.text = song.subtitle

            Timber.tag("Firebase Data ImageURL").d(song.subtitle)
            Timber.tag("Firebase Data SongURL").d(song.imageURL)
            Timber.tag("Firebase Data Title").d(song.title)
            Timber.tag("Firebase Data Subtitle").d(song.songURL)

            itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, song)
                }
            }
        }
    }

    private var onClickListener : OnClickListener? = null
    interface OnClickListener{
        fun onClick(position : Int, song : Song)
    }
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}