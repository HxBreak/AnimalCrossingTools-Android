package com.hxbreak.animalcrossingtools.services

import android.content.Context
import java.io.File

object FileProviderExt {
    fun mediaDir(context: Context): File{
        val mediaDir = File(context.filesDir, "private_media")
        if (!mediaDir.exists()) mediaDir.mkdir()
        return mediaDir
    }
}