package com.hxbreak.animalcrossingtools.utils

import java.io.Closeable
import java.io.File

fun Closeable.closeQuietly(){
    try {
        close()
    }catch (e: Exception){
//        e.printStackTrace()
    }
}

fun File?.deleteQuietly(): Throwable? {
    try {
        this?.delete()
    }catch (e: Exception){
        return e
    }
    return null
}