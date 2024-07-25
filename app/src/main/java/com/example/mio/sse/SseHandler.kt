package com.example.mio.sse

import android.content.Context
import android.util.Log
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler

class SseHandler(private val context: Context) : BackgroundEventHandler {

    override fun onOpen() {
        // SSE 연결 성공시 처리 로직 작성
        Log.d("SSE", "SSE연결 성공")
    }

    override fun onClosed() {
        // SSE 연결 종료시 처리 로직 작성
        Log.d("SSE", "SSE 안전 종료")
    }

    override fun onMessage(event: String?, messageEvent: MessageEvent?) {

        Log.d("SSE", "Received data: ${messageEvent?.data}")
    }

    override fun onComment(comment: String?) {
    }

    override fun onError(t: Throwable?) {
        Log.d("SSE", "SSE연결 실패")
        Log.e("SSE", t.toString())
    }
}