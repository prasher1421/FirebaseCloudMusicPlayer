package com.prasher.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.prasher.spotifyclone.R
import com.prasher.spotifyclone.data.entities.Song
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide : RequestManager
) :RecyclerView.Adapter<SongAdapter.SongViewHolder>(){

    class SongViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val tvPrimary =
    }

    private val diffCallbak = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            //checks mediaID is the same
            return oldItem.mediaID == newItem.mediaID
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this,diffCallbak)

    var songs : List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {

        }
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}