package com.tomclaw.appsend.util

import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

interface DownloadManager {

    fun status(appId: String): Observable<Int>

    fun download(appId: String, url: String)

    fun cancel(appId: String)

}

class DownloadManagerImpl(
    private val dir: File,
    private val schedulers: SchedulersFactory,
) : DownloadManager {

    private val executor = Executors.newSingleThreadExecutor()

    private val packages = HashMap<String, BehaviorRelay<Int>>()
    private val downloads = HashMap<String, Future<*>>()

    override fun status(appId: String): Observable<Int> {
        return packages[appId] ?: let {
            val relay = BehaviorRelay.createDefault(IDLE)
            packages[appId] = relay
            relay
        }
    }

    override fun download(appId: String, url: String) {
        val relay = packages[appId] ?: BehaviorRelay.createDefault(AWAIT) // TODO: remove relay on dispose if state is terminating; set default state COMPLETED if apk file exist and ready
        val file = File(dir, "$appId.apk") // TODO: make file name human-readable
        downloads[appId] = executor.submit {
            val success = downloadBlocking(
                url,
                file,
                progressCallback = { percent -> relay.accept(percent) },
                errorCallback = { relay.accept(ERROR) })
            if (success) {
                relay.accept(COMPLETED)
            }
        }
        packages[appId] = relay
    }

    override fun cancel(appId: String) {
        downloads.remove(appId)?.cancel(true)
        packages.remove(appId)
    }

    private fun downloadBlocking(
        url: String,
        file: File,
        progressCallback: (Int) -> Unit,
        errorCallback: (Throwable) -> Unit
    ): Boolean {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            LegacyLogger.log(String.format("Download app url: %s", url))
            val u = URL(url)
            connection = u.openConnection() as HttpURLConnection
            with(connection) {
                connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                requestMethod = HttpUtil.GET
                useCaches = false
                doInput = true
                doOutput = true
                instanceFollowRedirects = false
            }
            connection.connect()
            val responseCode = connection.responseCode
            input = if (responseCode >= HttpUtil.SC_BAD_REQUEST) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            val total = connection.contentLength
            if (total <= 0) {
                errorCallback(IOException("ContentLength is not defined"))
                return false
            }
            file.parentFile?.mkdirs()
            if (file.exists()) {
                file.delete()
            }
            output = FileOutputStream(file)
            val buffer = VariableBuffer()
            var cache: Int
            var read: Long = 0
            var percent = 0
            buffer.onExecuteStart()
            while (input.read(buffer.calculateBuffer()).also { cache = it } != -1) {
                buffer.onExecuteCompleted(cache)
                output.write(buffer.buffer, 0, cache)
                output.flush()
                read += cache.toLong()
                val p = (100 * read / total).toInt()
                if (p > percent) {
                    progressCallback(percent)
                    percent = p
                }
                buffer.onExecuteStart()
                Thread.sleep(10)
            }
            progressCallback(100)
            return true
        } catch (ex: Throwable) {
            LegacyLogger.log("Exception while application downloading", ex)
            errorCallback(ex)
        } finally {
            connection?.disconnect()
            HttpUtil.closeSafely(input)
            HttpUtil.closeSafely(output)
        }
        return false
    }

}

const val ERROR: Int = -3
const val IDLE: Int = -2
const val AWAIT: Int = -1
const val COMPLETED: Int = 101
