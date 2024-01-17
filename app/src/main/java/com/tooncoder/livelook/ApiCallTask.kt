package com.tooncoder.livelook

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiCallTask : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void): String? {
        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        var result: String? = null

        try {
            val url = URL("https://dummy.restapiexample.com/api/v1/employees")
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"

            // Read the response
            val inputStream = urlConnection.inputStream
            reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            result = stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return result
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // Log the API response
        Log.d("ApiCallTask", "API Response: $result")
    }
}
