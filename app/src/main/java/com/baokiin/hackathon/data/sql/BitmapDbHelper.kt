package com.baokiin.hackathon.data.sql

import android.content.ContentValues
import android.content.Context
import com.baokiin.hackathon.data.BitmapModel

class BitmapDbHelper(context: Context) : SqlDbHelper(
    context,
    DATABASE_NAME,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "BitmapData.db"
    }

    override fun createTable() = "CREATE TABLE ${BITMAP_TABLE.TABLE_NAME} (" +
            "${BITMAP_TABLE.COLUMN_NAME} TEXT PRIMARY KEY," +
            "${BITMAP_TABLE.COLUMN_PATH} TEXT," +
            "${BITMAP_TABLE.COLUMN_SIZE} TEXT)"

    override fun deleteTable() = "DROP TABLE IF EXISTS ${BITMAP_TABLE.TABLE_NAME}"

    fun insertBitmap(data: BitmapModel) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(BITMAP_TABLE.COLUMN_NAME, data.name)
            put(BITMAP_TABLE.COLUMN_PATH, data.path)
            put(BITMAP_TABLE.COLUMN_SIZE, data.size)
        }
        db?.insert(BITMAP_TABLE.TABLE_NAME, null, values)
    }

    fun getBitmapsByPage(pageNumber: Int, pageSize: Int): List<BitmapModel> {
        val items = mutableListOf<BitmapModel>()
        val db = this.readableDatabase
        val page = (pageNumber - 1) * pageSize
        val cursor = db.rawQuery(
            "SELECT * FROM " + BITMAP_TABLE.TABLE_NAME + " LIMIT ? OFFSET ?",
            arrayOf(pageSize.toString(), page.toString())
        )
        try {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(
                        cursor.getColumnIndexOrThrow(BITMAP_TABLE.COLUMN_ID)
                    ).orEmpty()
                    val name = cursor.getString(
                        cursor.getColumnIndexOrThrow(BITMAP_TABLE.COLUMN_NAME)
                    ).orEmpty()
                    val path = cursor.getString(
                        cursor.getColumnIndexOrThrow(BITMAP_TABLE.COLUMN_PATH)
                    ).orEmpty()
                    val size = cursor.getString(
                        cursor.getColumnIndexOrThrow(BITMAP_TABLE.COLUMN_SIZE)
                    ).orEmpty()
                    items.add(
                        BitmapModel(
                            id = id,
                            name = name,
                            path = path,
                            size = size
                        )
                    )
                } while (cursor.moveToNext())
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        cursor.close()
        return items
    }

}

object BITMAP_TABLE {
    const val TABLE_NAME = "bitmap"
    const val COLUMN_ID = "id"
    const val COLUMN_NAME = "name"
    const val COLUMN_PATH = "path"
    const val COLUMN_SIZE = "size"

}
