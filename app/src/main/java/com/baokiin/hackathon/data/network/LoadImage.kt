package com.baokiin.hackathon.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest

object LoadImage {
    val cache: DoubleCache = DoubleCache()

    fun ImageView.load(url: String) {
        tag = url
        val bitmap: Bitmap? = cache.get(url)
        bitmap?.let {
            setImageBitmap(getScaledBitmap(bitmap,60,60))
        }
    }

    fun clearDiskCache() {
        cache.clear()
    }
}

class DoubleCache : ImageCache {

    private val memCache = MemoryCache()
    private val diskCache = DiskCache()

    override fun get(url: String): Bitmap? {
        return memCache.get(url) ?: diskCache.get(url)
    }

    override fun put(url: String, bitmap: Bitmap?) {
        memCache.put(url, bitmap)
    }

    override fun clear() {
        memCache.clear()
    }
}
private fun getScaledBitmap(b: Bitmap?, reqWidth: Int, reqHeight: Int): Bitmap? {
    b?.let {
        val bWidth = b.width
        val bHeight = b.height
        var nWidth = reqWidth
        var nHeight = reqHeight
        val parentRatio = reqHeight.toFloat() / reqWidth
        nHeight = bHeight
        nWidth = (reqWidth * parentRatio).toInt()
        return Bitmap.createScaledBitmap(b, nWidth, nHeight, true)
    }
    return null
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
    override fun get(url: String): Bitmap {
        return BitmapFactory.decodeFile(url)
//        val file = File(url)
//        return file.readBytes().toBitmap()
    }

    override fun clear() {}

    override fun put(url: String, bitmap: Bitmap?) {
    }

    private fun Bitmap.getBytes(): ByteArray {
        val bos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, bos)
        return bos.toByteArray()
    }


}


fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, size)
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

interface ImageCache {
    fun put(url: String, bitmap: Bitmap?)
    fun get(url: String): Bitmap?
    fun clear()
}
