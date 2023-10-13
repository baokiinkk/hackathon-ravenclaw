package com.baokiin.hackathon.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import com.baokiin.hackathon.R
import com.baokiin.hackathon.ui.main.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object LoadImage {
    val cache: DoubleCache = DoubleCache()

    fun ImageView.load(url: String) {
        tag = url
        var bitmap: Bitmap? = cache.get(url)
        if (bitmap == null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    withContext(Dispatchers.IO) {
                        val urlConnection = URL(url).openConnection() as HttpURLConnection
                        val bytes = urlConnection.inputStream.readBytes()
                        urlConnection.disconnect()
                        bitmap =
                            BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size,
                                BitmapFactory.Options()
                            )
                        cache.put(url, bitmap)
                    }
                    if (tag == url)
                        setImageBitmap(bitmap)

                } catch (e: Exception) {
                    setImageResource(R.drawable.ic_launcher_background)
                }
            }
        } else {
            setImageBitmap(bitmap)
        }
    }

    fun clearDiskCache() {
        cache.clearFileExprie()
    }
}

class DoubleCache() : ImageCache {

    private val memCache = MemoryCache()
    private val diskCache = DiskCache()

    override fun get(url: String): Bitmap? {
        return memCache.get(url) ?: diskCache.get(url)
    }

    override fun put(url: String, bitmap: Bitmap?) {
        memCache.put(url, bitmap)
        diskCache.put(url, bitmap)
    }

    override fun clear() {
        memCache.clear()
    }

    override fun clearFileExprie() {
        diskCache.clearFileExprie()
    }
}

class MemoryCache : ImageCache {
    private val cache: LruCache<String, Bitmap>

    init {
        val maxMemory: Long = Runtime.getRuntime().maxMemory() / 1024
        val cacheSize: Int = (maxMemory / 4).toInt()

        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return (bitmap?.rowBytes ?: 0) * (bitmap?.height ?: 0) / 1024
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String?,
                oldValue: Bitmap?,
                newValue: Bitmap?
            ) {
                super.entryRemoved(evicted, key, oldValue, newValue)
            }
        }
    }

    override fun put(url: String, bitmap: Bitmap?) {
        bitmap?.let {
            cache.put(url, it)
        }
    }

    override fun get(url: String): Bitmap? {
        return cache.get(url)
    }

    override fun clear() {
        cache.evictAll()
    }
}

class DiskCache() : ImageCache {
    companion object {
        private const val DELTA_TIME = 60 * 60 * 2 * 1000
    }

    override fun get(url: String): Bitmap? {
        val file = File(MyApplication.getApplication().cacheDir, url.md5())
        return if (file.exists()) {
            if (file.lastModified() + DELTA_TIME < System.currentTimeMillis()) {
                file.delete()
                null
            } else {
                file.readBytes().toBitmap()
            }
        } else {
            null
        }
    }

    override fun clear() {}

    override fun put(url: String, bitmap: Bitmap?) {
        bitmap?.let {
            try {
                val file = File(MyApplication.getApplication().cacheDir, url.md5())
                if (!file.exists())
                    file.parentFile?.mkdir()
                file.writeBytes(it.getBytes())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun clearFileExprie() {
        MyApplication.getApplication().cacheDir.listFiles()?.forEach {
            clearFile(it)
        }
    }

    private fun clearFile(file: File) {
        if (file.exists() && (file.lastModified() + DELTA_TIME < System.currentTimeMillis())) {
            file.delete()
        }
    }

    private fun Bitmap.getBytes(): ByteArray {
        val bos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, bos)
        return bos.toByteArray()
    }

    private fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, size)
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}

interface ImageCache {
    fun put(url: String, bitmap: Bitmap?)
    fun get(url: String): Bitmap?
    fun clear()
    fun clearFileExprie() {}
}