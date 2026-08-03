package com.example.kiblasalat.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object FileDownloader {

    suspend fun download(
        urlStr: String,
        destinationFile: File,
        onProgress: (Float) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext false
            }

            val fileLength = connection.contentLength
            val input = BufferedInputStream(connection.inputStream)
            val output = FileOutputStream(destinationFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count
                if (fileLength > 0) {
                    onProgress(total.toFloat() / fileLength.toFloat())
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (destinationFile.exists()) {
                destinationFile.delete()
            }
            false
        }
    }
}
