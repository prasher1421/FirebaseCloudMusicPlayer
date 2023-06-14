package com.prasher.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.prasher.spotifyclone.data.entities.Song

abstract class BaseSongAdapter(
    private val layoutId : Int
) : RecyclerView.Adapter<BaseSongAdapter.SongViewHolder>(){

    class SongViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    //diffUtil library for performant rv
    protected val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            //checks mediaID is the same
            return oldItem.mediaID == newItem.mediaID
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ : AsyncListDiffer<Song>

    var songs : List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)
    //any change is made setter is activated

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}