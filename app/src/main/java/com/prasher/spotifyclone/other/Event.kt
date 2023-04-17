package com.prasher.spotifyclone.other

open class Event<out T>(private val data : T)  {


    //initially it will be false
    //so will be true afterwards to call it once
    var hasBeenHandled = false
        private set


    fun getContentIfNotHandled() : T? {
        return if (hasBeenHandled) {//if we already handled the event
            null
        } else{
            hasBeenHandled = true
            data
        }
    }

    //if we need to use data later
    fun peekContent()= data

}