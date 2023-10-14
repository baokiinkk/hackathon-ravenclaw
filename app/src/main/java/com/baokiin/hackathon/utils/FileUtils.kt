package com.baokiin.hackathon.utils

import android.os.Environment
import com.baokiin.hackathon.data.BitmapModel

object FileUtils {
    fun getAllFilePaths(): List<BitmapModel> {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+"/Data")
        return directory.listFiles()?.mapIndexed { index, file ->
            BitmapModel(
                id = index.toString(),
                name = file.name,
                path = file.absolutePath,
                size = file.length().toString()
            )
        } ?: emptyList()

    }
}
