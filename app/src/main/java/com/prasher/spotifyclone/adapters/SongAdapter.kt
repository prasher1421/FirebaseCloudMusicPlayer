package com.prasher.spotifyclone.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.data.entities.Song
import timber.log.Timber
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide : RequestManager
) : BaseSongAdapter(R.layout.list_item){

    override val differ =  AsyncListDiffer(this,diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val tvPrimary : TextView? = holder.itemView.findViewById(R.id.tvPrimary)
        val tvSecondary : TextView? = holder.itemView.findViewById(R.id.tvSecondary)
        val ivItemImage : ImageView? = holder.itemView.findViewById(R.id.ivItemImage)

        val song = songs[position]
        holder.apply {
            tvPrimary?.text = song.title
            tvSecondary?.text = song.subtitle

            Timber.tag("Firebase Data ImageURL").d(song.imageURL)
            Timber.tag("Firebase Data SongURL").d(song.songURL)
            Timber.tag("Firebase Data Title").d(song.title)
            Timber.tag("Firebase Data Subtitle").d(song.subtitle)

            glide
                .load(song.imageURL)
                .into(ivItemImage!!)

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