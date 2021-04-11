package com.hxbreak.animalcrossingtools.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun Context.fileToUri(file: File): Uri {
    return FileProvider.getUriForFile(this, "com.hxbreak.animalcrossingtools.FileProvider", file)
}