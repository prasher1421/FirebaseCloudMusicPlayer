package com.prasher.spotifyclone.other

data class Resource<out T>(val status: Status, val data : T?, val message: String?) //T is the template class which will passed as resource
{
    companion object{
        //for case of success we will get a data
        fun <T> success(data : T?) = Resource(Status.SUCCESS, data, null)


        fun <T> error(message : String, data : T?) = Resource(Status.ERROR, data, message)


        fun <T> loading(data : T?) = Resource(Status.LOADING, data, null)

        //later used in ViewModel
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}