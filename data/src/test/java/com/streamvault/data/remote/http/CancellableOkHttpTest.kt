package com.streamvault.data.remote.http

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.Test

class CancellableOkHttpTest {

    @Test
    fun `cancellation before headers cancels the call promptly`() = runBlocking {
        val started = CountDownLatch(1)
        val cancelled = CountDownLatch(1)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            started.countDown()
            while (!chain.call().isCanceled()) Thread.sleep(5)
            cancelled.countDown()
            throw IOException("cancelled")
        }.build()
        val job = launch(Dispatchers.IO) {
            client.newCall(request()).useCancellableResponse { Unit }
        }

        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue()
        job.cancelAndJoin()

        assertThat(cancelled.await(5, TimeUnit.SECONDS)).isTrue()
    }

    @Test
    fun `cancellation during a blocked body read cancels call and closes body`() = runBlocking {
        val cancelled = CountDownLatch(1)
        val readStarted = CountDownLatch(1)
        val bodyClosed = AtomicBoolean(false)
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(blockingBody(chain, readStarted, cancelled, bodyClosed))
                .build()
        }).build()
        val job = launch(Dispatchers.IO) {
            client.newCall(request()).useCancellableResponse { response ->
                response.body!!.source().read(Buffer(), 1)
            }
        }

        assertThat(readStarted.await(5, TimeUnit.SECONDS)).isTrue()
        withTimeout(5_000) {
            job.cancelAndJoin()
        }

        assertThat(cancelled.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(bodyClosed.get()).isTrue()
    }

    @Test
    fun `response closes when consumer throws`() = runBlocking {
        val bodyClosed = AtomicBoolean(false)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(finiteBody(bodyClosed))
                .build()
        }.build()

        val result = runCatching {
            client.newCall(request()).useCancellableResponse {
                throw IllegalStateException("parser failed")
            }
        }

        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        assertThat(bodyClosed.get()).isTrue()
    }

    private fun request() = Request.Builder().url("https://example.test/stream").build()

    private fun blockingBody(
        chain: Interceptor.Chain,
        readStarted: CountDownLatch,
        cancelled: CountDownLatch,
        closed: AtomicBoolean
    ): ResponseBody = object : ResponseBody() {
        override fun contentType(): MediaType? = null
        override fun contentLength(): Long = -1L
        override fun source(): BufferedSource = object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long {
                readStarted.countDown()
                while (!chain.call().isCanceled()) Thread.sleep(5)
                cancelled.countDown()
                throw IOException("cancelled")
            }

            override fun timeout(): Timeout = Timeout.NONE
            override fun close() {
                closed.set(true)
            }
        }.buffer()
    }

    private fun finiteBody(closed: AtomicBoolean): ResponseBody = object : ResponseBody() {
        override fun contentType(): MediaType? = null
        override fun contentLength(): Long = 0L
        override fun source(): BufferedSource = object : Source {
            override fun read(sink: Buffer, byteCount: Long): Long = -1L
            override fun timeout(): Timeout = Timeout.NONE
            override fun close() {
                closed.set(true)
            }
        }.buffer()
    }
}
