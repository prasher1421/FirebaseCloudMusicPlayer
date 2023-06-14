package com.prasher.spotifyclone.other


//T is the template class which will passed as resource
//Response given by API as suggested by Google
data class Response<out T>(val status: Status, val data : T?, val message: String?)
{
    companion object{
        //for case of success we will get a data
        fun <T> success(data : T?) = Response(Status.SUCCESS, data, null)
        fun <T> error(message : String, data : T?) = Response(Status.ERROR, data, message)
        fun <T> loading(data : T?) = Response(Status.LOADING, data, null)

        //later used in ViewModel
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}