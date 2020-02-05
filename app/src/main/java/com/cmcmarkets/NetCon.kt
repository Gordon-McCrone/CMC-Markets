package com.cmcmarkets

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class NetCon : AsyncTask<String, Unit, String>()   {

    var TAG = "*5*"

    lateinit var delegateToCall: NetConProtocol
    lateinit var completeURL: URL
    lateinit var httpClient: HttpURLConnection
    //    lateinit var os: OutputStream
//    lateinit var writer: BufferedWriter
//    lateinit var JSONbody: String

    override fun doInBackground(vararg params: String): String? {

        httpClient = completeURL.openConnection() as HttpURLConnection
        httpClient.setReadTimeout(10000)
        httpClient.setConnectTimeout(10000)
        httpClient.requestMethod = "POST"

        httpClient.instanceFollowRedirects = false
        httpClient.doOutput = true
        httpClient.doInput = true
        httpClient.useCaches = false
        httpClient.setRequestProperty("Content-Type", "application/json; charset=utf-8")

        // This is where I would handle 7 different failure types, and 1 success type.
        // Currently only handling 1 failure type and 1 success type
        try {
            httpClient.connect()
            val os = httpClient.getOutputStream()
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
//            writer.write(JSONbody)
//            Log.d(TAG, "JSONbody: ${JSONbody}")
            writer.flush()
            writer.close()
            os.close()

            if (httpClient.responseCode == HttpURLConnection.HTTP_OK) {
                val stream = BufferedInputStream(httpClient.inputStream)
                val jsonString: String = readStream(inputStream = stream)
                Log.d(TAG, "jsonString: ${jsonString}")
                val gson = Gson()
                val map: Map<*, *> = gson.fromJson(jsonString, MutableMap::class.java)
                delegateToCall.Success(map)
//                return data
            } else {
                println("ERROR ${httpClient.responseCode}")
                val map: MutableMap<String, String> = HashMap()
                map.put("error", "HTTP")
                // By passing back the code, where a 401 Unauthorized is received, then the caller can redirect to the login page.
                map.put("code", httpClient.responseCode.toString())
                delegateToCall.Fail((map))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            httpClient.disconnect()
        }

        return null
    }

    // NetConProtocol will call the caller. If the caller had more than one type of request then I would have a parameter for call type,
    // which would be passed back to the caller so it can identify what end point was called and behave accordingly.
    // Additionally if this were using tokens for authentication then this would be passed as a parameter and added to the URL
    fun Post(delegate: NetConProtocol, url: String) {

        delegateToCall = delegate

        completeURL = URL(Constants.urlBase + url)
    }

    // I would have another method for GET with similar parameters. This forces clarity on the caller as:
    // netCon.Post vs netCon.Get

    fun readStream(inputStream: BufferedInputStream): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        bufferedReader.forEachLine { stringBuilder.append(it) }
        return stringBuilder.toString()
    }
}