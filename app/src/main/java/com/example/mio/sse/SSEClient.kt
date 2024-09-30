package com.example.mio.sse

import android.util.Log
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class SSEClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .build()

    fun startListening(url: String, callback: (String) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .addHeader("Accept", "text/event-stream")
            .build()

        val eventSourceListener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                // SSE 이벤트 수신
                callback(data)  // 이벤트 데이터를 콜백을 통해 전달
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                // 오류 처리
                t?.printStackTrace()
            }

            override fun onOpen(eventSource: EventSource, response: Response) {
                // SSE 연결 열림
                println("SSE Connection opened")
            }

            override fun onClosed(eventSource: EventSource) {
                // SSE 연결 닫힘
                println("SSE Connection closed")
            }
        }

        EventSources.createFactory(client).newEventSource(request, eventSourceListener)
    }
}
