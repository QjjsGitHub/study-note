package com.example.study.dataTransform

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "MyDb", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE myData (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                date INTEGER,
                content TEXT
            )
        """.trimIndent()
        //trimIndent()是 Kotlin 中用于移除多行字符串（三重引号 """）中每行公共前导空白（空格或制表符）的扩展函数，保留相对缩进结构

        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS myData")
        onCreate(db)
    }
}
