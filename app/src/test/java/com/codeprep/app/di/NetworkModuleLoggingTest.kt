package com.codeprep.app.di

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import kotlin.concurrent.thread
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the BLOCKER regression: the user's AI API key must never reach log
 * output. OkHttp's HttpLoggingInterceptor redacts NOTHING by default, so
 * NetworkModule has to redact the Authorization header explicitly.
 */
class NetworkModuleLoggingTest {

    @Test
    fun `log output never contains the Authorization header value`() {
        val logs = mutableListOf<String>()
        val secretKey = "sk-test-secret-value-12345"

        val server = ServerSocket(0)
        val serverThread = thread(name = "test-http-server") {
            server.accept().use { socket ->
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                var line = reader.readLine()
                while (line != null && line.isNotEmpty()) {
                    line = reader.readLine()
                }
                val response =
                    "HTTP/1.1 200 OK\r\nContent-Length: 2\r\nConnection: close\r\n\r\nOK"
                socket.getOutputStream().apply {
                    write(response.toByteArray())
                    flush()
                }
            }
        }

        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(
                    NetworkModule.createHttpLoggingInterceptor(
                        HttpLoggingInterceptor.Logger { logs += it }
                    )
                )
                .build()

            client.newCall(
                Request.Builder()
                    .url("http://127.0.0.1:${server.localPort}/chat/completions")
                    .header("Authorization", "Bearer $secretKey")
                    .build()
            ).execute().use { }
        } finally {
            serverThread.join(5_000)
            server.close()
        }

        assertTrue(
            "The request headers should have been captured: $logs",
            logs.any { it.contains("Authorization") }
        )
        assertFalse(
            "Log output must never contain the API key value. Logs: $logs",
            logs.any { it.contains(secretKey) }
        )
    }
}
