package com.baokiin.hackathon.data.network

import android.os.Build
import android.util.Log
import com.baokiin.hackathon.data.InfoResponse
import com.baokiin.hackathon.extension.fromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*


class HttpUtils {

    companion object {

        private const val CONNECT_TIMEOUT = 5 * 1000

        private const val READ_TIMEOUT = 10 * 1000

        private lateinit var instance: HttpUtils

        fun g(): HttpUtils {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = HttpUtils()
                }
                return instance
            }
        }
    }

    fun createConnection(url: String?): HttpURLConnection {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.connectTimeout = CONNECT_TIMEOUT
        urlConnection.readTimeout = READ_TIMEOUT
        return urlConnection
    }

    fun getInputStream(url: String?): InputStream? {
        var inputStream: InputStream? = null
        try {
            val conn = createConnection(url)
            conn.requestMethod = "GET"
            inputStream = conn.inputStream
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return inputStream
    }


    inline fun <reified T> requestApi(url: String, apiResult: ApiResult<T>) {
        apiResult.loading(true)
        var connection: HttpURLConnection? = null
        val buffer = StringBuffer()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    connection = createConnection(url)
                    connection?.requestMethod = "GET"
                    val inputStream = connection?.inputStream
                    val bf = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                    var line: String? = ""
                    while (bf.readLine().also { line = it } != null) {
                        buffer.append(line)
                    }
                    bf.close()
                    inputStream?.close()
                }
                apiResult.successful(buffer.toString().fromJson())
            } catch (exception: MalformedURLException) {
                apiResult.error(exception.message.toString())
            } catch (exception: IOException) {
                apiResult.error(exception.message.toString())
            } finally {
                apiResult.loading(isLoading = false)
                connection?.disconnect()
            }
        }
    }

//    fun getString(url: String?): String? {
//        var result: String? = null
//        var `is`: InputStream? = null
//        var br: BufferedReader? = null
//        try {
//            `is` = getInputStream(url)
//            br = BufferedReader(InputStreamReader(`is`, CHARSET))
//            var line: String? = null
//            val sb = StringBuffer()
//            while (br.readLine().also { line = it } != null) {
//                sb.append(line)
//            }
//            result = sb.toString()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            try {
//                br?.close()
//            } catch (e: IOException) {
//            }
//            try {
//                `is`?.close()
//            } catch (e: IOException) {
//            }
//        }
//        return result
//    }
//
//    fun postString(url: String?, params: String?): String? {
//        var result: String? = null
//        var os: OutputStream? = null
//        var `is`: InputStream? = null
//        var br: BufferedReader? = null
//        try {
//            val conn = createConnection(url)
//            conn.requestMethod = "POST"
//            conn.doOutput = true
//            conn.doInput = true
//            conn.useCaches = false
//            // conn.setRequestProperty(field, newValue);//header
//            conn.setRequestProperty("Content-Type", "application/json; charset=" + CHARSET)
//            // conn.setRequestProperty("Connection", "keep-alive");
//            // conn.setRequestProperty("User-Agent",
//            // "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
//            if (params != null) {
//                os = conn.outputStream
//                val dos = DataOutputStream(os)
//                dos.write(params.toByteArray(charset(CHARSET)))
//                dos.flush()
//                dos.close()
//            }
//            `is` = conn.inputStream
//            br = BufferedReader(InputStreamReader(`is`, CHARSET))
//            var line: String? = null
//            val sb = StringBuffer()
//            while (br.readLine().also { line = it } != null) {
//                sb.append(line)
//            }
//            result = sb.toString()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            try {
//                os?.close()
//            } catch (e: IOException) {
//            }
//            try {
//                br?.close()
//            } catch (e: IOException) {
//            }
//            try {
//                `is`?.close()
//            } catch (e: IOException) {
//            }
//        }
//        return result
//    }

    @Throws(IOException::class)
    protected fun shouldBeProcessed(conn: HttpURLConnection): Boolean {
        return conn.responseCode == 200
    }

    protected fun disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false")
        }
    }

    private fun setupSSl() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(NullHostNameVerifier())
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private inner class NullHostNameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            Log.i("RestUtilImpl", "Approving certificate for $hostname")
            return true
        }
    }
}

interface ApiResult<T> {
    fun loading(isLoading: Boolean)
    fun successful(data: T)
    fun error(error: String)
}
