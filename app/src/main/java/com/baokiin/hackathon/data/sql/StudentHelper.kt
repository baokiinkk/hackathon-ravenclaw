package com.baokiin.hackathon.data.sql

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import androidx.core.database.getStringOrNull
import java.io.File

class StudentDbHelper(context: Context) : SqlDbHelper(
    context,
    DATABASE_NAME,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "FeedReader.db"
    }

    object STUDENT_TABLE : BaseColumns {
        const val TABLE_NAME = "entry"
        const val COLUMN_FULL_NAME = "name"
        const val COLUMN_AGE = "age"
        const val COLUMN_SEX = "sex"
    }

    override fun createTable() = "CREATE TABLE ${STUDENT_TABLE.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
            "${STUDENT_TABLE.COLUMN_FULL_NAME} TEXT," +
            "${STUDENT_TABLE.COLUMN_AGE} TEXT," +
            "${STUDENT_TABLE.COLUMN_SEX} TEXT)"

    override fun deleteTable() = "DROP TABLE IF EXISTS ${STUDENT_TABLE.TABLE_NAME}"

    fun insertStudent(data: StudentModel) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(STUDENT_TABLE.COLUMN_FULL_NAME, data.name)
            put(STUDENT_TABLE.COLUMN_AGE, data.age)
            put(STUDENT_TABLE.COLUMN_SEX, data.sex)
        }
        // Insert the new row, returning the primary key value of the new row
        db?.insert(STUDENT_TABLE.TABLE_NAME, null, values)
    }

    fun getStudent(id: Int): StudentModel? {
        val db = this.readableDatabase
        val cursor = db.query(
            STUDENT_TABLE.TABLE_NAME,
            null,
            BaseColumns._ID + " = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        return cursor?.let {
            it.moveToFirst()
            val data = StudentModel(
                it.getStringOrNull(0),
                it.getStringOrNull(1),
                it.getStringOrNull(2),
                it.getStringOrNull(3),
            )
            it.close()
            data
        } ?: run {
            null
        }
    }

    fun getAllStudents(): List<StudentModel> {
        val studentList = mutableListOf<StudentModel>()
        val query = "SELECT * FROM" + STUDENT_TABLE.TABLE_NAME
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)
        cursor?.let {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val student = StudentModel(
                    cursor.getStringOrNull(0),
                    cursor.getStringOrNull(1),
                    cursor.getStringOrNull(2),
                    cursor.getStringOrNull(3),
                )
                studentList.add(student)
                cursor.moveToNext()
            }
        }
        cursor.close()
        return studentList
    }

    fun updateStudent(student: StudentModel) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(STUDENT_TABLE.COLUMN_FULL_NAME, student.name)
        values.put(STUDENT_TABLE.COLUMN_AGE, student.age)
        values.put(STUDENT_TABLE.COLUMN_AGE, student.sex)
        db.update(
            STUDENT_TABLE.TABLE_NAME,
            values,
            BaseColumns._ID + " = ?",
            arrayOf<String>(java.lang.String.valueOf(student.id))
        )
        db.close()
    }

    fun deleteStudent(studentId: Int) {
        val db = this.writableDatabase
        db.delete(
            STUDENT_TABLE.TABLE_NAME,
            BaseColumns._ID + " = ?",
            arrayOf(studentId.toString())
        )
        db.close()
    }

    fun getItemsByPage(pageNumber: Int, itemsPerPage: Int): List<StudentModel> {
        val items = mutableListOf<StudentModel>()
        val db = this.readableDatabase
        val offset = (pageNumber - 1) * itemsPerPage
        val cursor = db.rawQuery(
            "SELECT * FROM " + STUDENT_TABLE.TABLE_NAME + " LIMIT ? OFFSET ?",
            arrayOf(itemsPerPage.toString(), offset.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                var index = 0
//                File("s","").listFiles().forEach {
//                    insert(it.name)
//                    index++
//                    if(index == page)
//                        callbackUI
//                }
                val id = cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(STUDENT_TABLE.COLUMN_FULL_NAME))
                val age = cursor.getString(cursor.getColumnIndexOrThrow(STUDENT_TABLE.COLUMN_AGE))
                val sex = cursor.getString(cursor.getColumnIndexOrThrow(STUDENT_TABLE.COLUMN_SEX))
                items.add(
                    StudentModel(
                        id = id,
                        name = name,
                        age = age,
                        sex = sex
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }
}

class StudentModel(
    val id: String?,
    val name: String?,
    val age: String?,
    val sex: String?
)

data class PersonModel(
    val students: List<StudentModel>
)
