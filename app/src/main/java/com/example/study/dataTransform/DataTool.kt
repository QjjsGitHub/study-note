package com.example.study.dataTransform

import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FilenameFilter
import java.lang.ref.WeakReference


class DataTool private constructor(private val contextWeakReference: WeakReference<Context>) {

    suspend fun getImagesNew(): Array<String> = withContext(Dispatchers.IO) {
        val imageList = mutableListOf<String>()
        val context = contextWeakReference.get() ?: return@withContext emptyArray<String>()

        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                if (path != null) {
                    imageList.add(path)
                }
            }
        }
        return@withContext imageList.toTypedArray()
    }

    suspend fun getImages(): String {

        val context = contextWeakReference.get() ?: return "null"

        val mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // 只查询jpeg和png的图片
        val cursor: Cursor? = context.contentResolver.query(
            mImageUri, null,
            (MediaStore.Images.Media.MIME_TYPE + "=? or "
                    + MediaStore.Images.Media.MIME_TYPE + "=?"),
            arrayOf<String>("image/jpeg", "image/png"),
            MediaStore.Images.Media.DATE_MODIFIED
        )

        cursor?.use {
            while (it.moveToNext()) {


                //获取数据库中图片路径：/storage/emulated/0/DCIM/Camera/IMG20160501152640.jpg
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))

                //获取父目录：/storage/emulated/0/DCIM/Camera
                val parentFile = File(path).getParentFile()

                //没有父目录，跳出本次循环
                if (parentFile == null) continue


                //父目录的绝对路径：/storage/emulated/0/DCIM/Camera

                val dirPath = parentFile.getAbsolutePath()


                if (parentFile.list() != null) {
                    //根据父文件夹，过滤出所有以jpg,png,jpeg结尾的文件的数量
                    val imgCount = parentFile.list(object : FilenameFilter {
                        override fun accept(dir: File?, name: String): Boolean {
                            return name.endsWith(".jpg") || name.endsWith(".png")
                                    || name.endsWith(".jpeg")
                        }
                    }).size


                }

            }
        }

        cursor?.close()

        return ""

    }

    suspend fun sharePreferencesTest(): Pair<Int, String> {

        val context = contextWeakReference.get() ?: return 0 to "null"

        val sp = context.getSharedPreferences("mySp", MODE_PRIVATE)

        sp.edit {

            putString("name", "Ming")

        }

        val age = sp.getInt("age", 18)

        val name = sp.getString("name", "null") ?: "null"

        Log.d(TAG, "name: " + name + "age: " + age)

        delay(2000)

        return age to name
    }

    suspend fun testContentProvider(
        context1: Context,
        type: Int,
        position: Int = 0,
        title: String = "title",
        data: Int = 0,
        content: String = "content"
    ) {

        val context = contextWeakReference.get() ?: return

        val contentResolver = context.contentResolver

        //构造 content:// 开头的 Uri，它用于标识数据库中的某条数据。
        val myUri = "content://com.example.study.provider/myData".toUri()

        /*id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT,
        date INTEGER,
        content TEXT*/

        when (type) {
            1 -> {
                // 1. 准备数据
                val values = ContentValues()
                values.put("title", title)
                values.put("date", data)
                values.put("content", content)

                // 2. 插入数据
                val insertUri = contentResolver.insert(myUri, values)

                Log.d(TAG, insertUri.toString())
            }

            2 -> {
                val projection = arrayOf("id", "title", "date", "content")
                val selection = "title LIKE ?"
                val selectionArgs = arrayOf("%$title%")
                val sortOrder = "date DESC"

                // 查询操作
                val myCursor =
                    contentResolver.query(myUri, projection, selection, selectionArgs, sortOrder)
                myCursor?.use {
                    while (it.moveToNext()) {
                        val id = it.getInt(it.getColumnIndexOrThrow("id"))
                        val title = it.getString(it.getColumnIndexOrThrow("title"))
                        val date = it.getLong(it.getColumnIndexOrThrow("date"))
                        val content = it.getString(it.getColumnIndexOrThrow("content"))

                        println(TAG + "ID: $id, Title: $title, Date: $date, Content: $content")
                    }
                }
                myCursor?.close()
            }

            3 -> {
                // 1. 构建包含 ID 的 Uri
                val queryUri = Uri.withAppendedPath(myUri, "$position")
                // 2. 查询数据
                val cursor1 = contentResolver.query(queryUri, null, null, null, null)
                // 3. 读取第一条数据
                if (cursor1 != null && cursor1.moveToFirst()) {
                    cursor1.let {
                        val id = it.getInt(it.getColumnIndexOrThrow("id"))
                        val title = it.getString(it.getColumnIndexOrThrow("title"))
                        val date = it.getLong(it.getColumnIndexOrThrow("date"))
                        val content = it.getString(it.getColumnIndexOrThrow("content"))
                        println(TAG + "ID: $id, Title: $title, Date: $date, Content: $content")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "ID: $id, Title: $title, Date: $date, Content: $content",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                cursor1?.close()
            }

            4 -> {
                // 修改数据
                // 1. 构建包含 ID 的 Uri
                val updateUri = Uri.withAppendedPath(myUri, "$position")

                // 1. 准备数据
                val values1 = ContentValues()
                values1.put("title", title)
                values1.put("date", data)
                values1.put("content", content)

                val result = contentResolver.update(updateUri, values1, null, null)
            }

            5 -> {
                // 修改数据
                // 1. 准备数据
                val selection1 = "title LIKE ?"
                val selectionArgs1 = arrayOf("%$title%")

                val values2 = ContentValues()
                values2.put("title", title)
                values2.put("date", data)

                val result1 = contentResolver.update(myUri, values2, selection1, selectionArgs1)
            }

            6 -> {
                //删除数据
                val deleteUri = Uri.withAppendedPath(myUri, "$position")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    contentResolver.delete(deleteUri, null)
                }
            }


            7 -> {
                //删除数据
                val selection2 = "content LIKE ?"
                val selectionArgs2 = arrayOf("%$content%")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    contentResolver.delete(myUri, selection2, selectionArgs2)
                }
            }
        }

    }


    companion object {
        private const val TAG = "DataTool"
        private var instance: DataTool? = null

        fun getInstance(context: WeakReference<Context>): DataTool {
            return instance ?: synchronized(this) {
                instance ?: DataTool(context).also { instance = it }
            }
        }

    }


}