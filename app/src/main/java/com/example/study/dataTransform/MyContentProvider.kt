package com.example.study.dataTransform

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri


//com.example.study.provider

class MyContentProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.example.study.provider"
        const val TABLE_NAME = "myData"
        val CONTENT_URI: Uri = "content://$AUTHORITY/$TABLE_NAME".toUri()

        //如果 URI 模式适用于单个行：android.cursor.item/
        //如果 URI 模式用于多行：android.cursor.dir/
        const val OPERATOR_ALL_DATA = 1
        const val OPERATOR_ITEM_DATA = 2

        /*private var myContentProvider: MyContentProvider? = null
           //这是 Kotlin 中特有的 get set 方法定义方式
           //  在成员变量的下面可以直接定义该成员的 get() set() 方法
           get() = synchronized(this) {
               if (myContentProvider == null) {
                   myContentProvider = MyContentProvider()
               }
               return myContentProvider
           }

       // 公共 ( 默认 ) 静态方法 , 获取 student 成员
       public fun getSingleton(): ContentProvider {
           //  !! 表示该对象必须不能为空
           return myContentProvider!!
       }*/

        //by lazy 线程是安全
        val myContentProvider: MyContentProvider by lazy { MyContentProvider() }
    }

    private lateinit var dbHelper: MyDatabaseHelper
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {

        addURI(AUTHORITY, TABLE_NAME, OPERATOR_ALL_DATA)

        /*DIARY_Dir是一个整数值，用来标识这个 URI 模式对应的操作。
        在后面的 ContentProvider 中，我们会使用这个整数值来确定执行哪些操作
        （例如查询、插入、更新、删除等）。DIARY_1Dir 是你在代码中定义的常量。
        比如后面的return when (uriMatcher.match(uri)) {
                  DIARY_DIR ->...}*/

        addURI(AUTHORITY, "$TABLE_NAME/#", OPERATOR_ITEM_DATA)

    }

    override fun onCreate(): Boolean {
        dbHelper = MyDatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return when (uriMatcher.match(uri)) {
            OPERATOR_ALL_DATA -> db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )

            OPERATOR_ITEM_DATA -> {
                val id = uri.lastPathSegment
                db.query(
                    TABLE_NAME, projection,
                    "id=?",
                    arrayOf(id),
                    null,
                    null,
                    sortOrder
                )
            }

            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            OPERATOR_ALL_DATA -> {
                val id = db.insert(TABLE_NAME, null, values)
                Uri.withAppendedPath(CONTENT_URI, id.toString())
            }

            else -> null
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db = dbHelper.writableDatabase
        return when (uriMatcher.match(uri)) {
            OPERATOR_ALL_DATA -> {
                db.update(TABLE_NAME, values, selection, selectionArgs)
            }

            OPERATOR_ITEM_DATA -> {
                val id = uri.lastPathSegment
                val r = db.update(TABLE_NAME, values, "id=?", arrayOf(id))
                r
            }

            else -> 0
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase

        return when (uriMatcher.match(uri)) {
            OPERATOR_ALL_DATA -> {
                val r = db.delete(TABLE_NAME, selection, selectionArgs)
                r
            }

            OPERATOR_ITEM_DATA -> {
                val id = uri.lastPathSegment
                val r = db.delete(TABLE_NAME, "id=?", arrayOf(id))
                r
            }

            else -> 0
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            OPERATOR_ALL_DATA -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE_NAME"
            OPERATOR_ITEM_DATA -> "vnd.android.cursor.item/vnd.$AUTHORITY.$TABLE_NAME"
            else -> null
        }
    }
}