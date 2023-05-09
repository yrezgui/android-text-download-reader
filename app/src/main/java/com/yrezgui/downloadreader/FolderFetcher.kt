package com.yrezgui.downloadreader

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class TextFile(val filename: String, val size: Long, val path: String) {
    val content get() = File(path).readText()
}

suspend fun folderFetch(): List<TextFile> = withContext(Dispatchers.IO) {
    val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    Log.d("folderFetch", "so far so good")
    folder
        .listFiles { file ->
            Log.d("listFiles", file.name)
            file.name.endsWith("txt")
        }
        ?.map { file ->
            Log.d("map", file.name)
            TextFile(file.name, file.length(), file.absolutePath)
        }
        ?: emptyList()
}