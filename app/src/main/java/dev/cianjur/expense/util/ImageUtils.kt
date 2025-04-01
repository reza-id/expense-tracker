package dev.cianjur.expense.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import androidx.core.graphics.scale
import kotlin.math.max

class ImageUtils(private val context: Context) {

    private val imageQuality = 80
    private val thumbnailSize = 200 // pixels

    /**
     * Compress an image file to reduce size
     */
    fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val outputFile = createTempFile("compressed_")

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, out)
        }

        return outputFile
    }

    /**
     * Create a thumbnail from an image file
     */
    fun createThumbnail(file: File): File {
        val originalBitmap = BitmapFactory.decodeFile(file.absolutePath)

        // Calculate new dimensions while maintaining aspect ratio
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaleFactor = thumbnailSize.toFloat() / max(width, height)

        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        val thumbnailBitmap = originalBitmap.scale(newWidth, newHeight)

        val outputFile = createTempFile("thumbnail_")
        FileOutputStream(outputFile).use { out ->
            thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, out)
        }

        return outputFile
    }

    /**
     * Save image file to internal storage and return its URI
     */
    fun saveImageToInternalStorage(file: File): String {
        val directory = File(context.filesDir, "expense_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val outputFile = File(directory, "${UUID.randomUUID()}.jpg")
        file.copyTo(outputFile, overwrite = true)

        return outputFile.absolutePath
    }

    /**
     * Get file from URI
     */
    fun getFileFromUri(uriString: String): File? {
        return try {
            File(uriString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get content URI for file
     */
    fun getContentUri(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Create a temporary file
     */
    private fun createTempFile(prefix: String): File {
        val directory = File(context.cacheDir, "temp_images")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        return File.createTempFile(
            prefix + System.currentTimeMillis(),
            ".jpg",
            directory
        )
    }

    /**
     * Delete expired temporary files
     */
    fun cleanupTempFiles() {
        val tempDir = File(context.cacheDir, "temp_images")
        if (tempDir.exists()) {
            val currentTime = System.currentTimeMillis()
            val expiredTime = currentTime - (24 * 60 * 60 * 1000) // 24 hours

            tempDir.listFiles()?.forEach { file ->
                if (file.lastModified() < expiredTime) {
                    file.delete()
                }
            }
        }
    }
}
