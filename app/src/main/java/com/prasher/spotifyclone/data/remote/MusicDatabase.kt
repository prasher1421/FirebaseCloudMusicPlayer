package com.prasher.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.prasher.spotifyclone.data.entities.Song
import com.prasher.spotifyclone.other.Constants.SONG_COLLECTION
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song>{
        return try {
            songCollection
                .get()
                .await()//to make it async
                .toObjects(Song::class.java)
        }catch (e:Exception){
            emptyList()
        }
    }
}