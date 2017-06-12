package com.fotoable.piano.utils


import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ResourceUtils {
    internal val TAG = ResourceUtils::class.java.name

    fun applicationFilesDir(context: Context): String {
        return context.filesDir.absolutePath
    }

    fun cacheDir(context: Context): String {
        return context.cacheDir.absolutePath
    }

    fun isCached(filename: String, context: Context): Boolean {
        return File(cacheDir(context) + "/" + filename).exists()
    }

    fun getCacheSize(context: Context): Long {
        return context.cacheDir.freeSpace
    }

    fun fileForAsset(context: Context, fileName: String): File? {
        val file = File(context.filesDir, fileName)
        if (file.exists() || extractAsset(context, fileName, file)) {
            return file
        }
        Log.e(TAG, "Couldn't extract asset: " + fileName)
        return null
    }

    //    public static boolean extractNamedResource(Context context, String name, File sfFile) {
    //        return false;
    //    }

    fun extractAsset(context: Context, path: String, file: File): Boolean {
        try {
            extractStreamToFile(context.assets.open(path), file, true)
            return true
        } catch (e: IOException) {
            return false
        }

    }

    @Throws(IOException::class)
    fun extractStreamToFile(`in`: InputStream, file: File, overwrite: Boolean) {
        val buffer = ByteArray(2048)
        val bin = BufferedInputStream(`in`, 2048)
        if (overwrite || !file.exists()) {
            file.parentFile.mkdirs()
            val bos = BufferedOutputStream(FileOutputStream(file), 2048)
            while (true) {
                val nRead = bin.read(buffer, 0, 2048)
                if (nRead <= 0) {
                    break
                }
                bos.write(buffer, 0, nRead)
            }
            bos.flush()
            bos.close()
        }
        `in`.close()
    }

    fun createJPGInPublicPicturesDirectory(): File? {
        return createFileInPublicDirectory(Environment.DIRECTORY_PICTURES, "IMG_", ".jpg")
    }

    fun createFileInPublicDirectory(directoryType: String, fileNamePrefix: String, fileExtension: String): File? {
        val storageDirectory = File(Environment.getExternalStoragePublicDirectory(directoryType), "smule")
        if (storageDirectory.exists() || storageDirectory.mkdirs()) {
            return File(storageDirectory.path + File.separator + fileNamePrefix + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + fileExtension)
        }
        Log.d(TAG, "Failed to create directory that would contain file!")
        return null
    }

    @Throws(IOException::class)
    fun readRawResource(context: Context, resourceId: Int): String {
        val `is` = context.resources.openRawResource(resourceId)
        val writer = StringWriter()
        val buffer = CharArray(1024)
        try {
            val reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
            while (true) {
                val n = reader.read(buffer)
                if (n == -1) {
                    break
                }
                writer.write(buffer, 0, n)
            }
            return writer.toString()
        } finally {
            `is`.close()
        }
    }

    fun sdcardDir(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }


}
