package com.example.mio.model

enum class AccountStatus(val status: String) {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    companion object {
        // 서버에서 받은 문자열로부터 AccountStatus를 가져오기
        fun fromStatus(status: String): AccountStatus? {
            return values().find { it.status == status }
        }
    }

    // AccountStatus를 서버에 전송할 문자열로 변환하기
    fun toStatus(): String {
        return status
    }
}