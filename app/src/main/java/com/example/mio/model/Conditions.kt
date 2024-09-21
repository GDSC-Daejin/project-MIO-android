package com.example.mio.model

//버튼의 활성화 여부를 판단하기 위한 클래스
class Conditions (
    var isSClicked: Boolean,
    var isSmClicked: Boolean,
    var isGClicked: Boolean
) {
    fun shouldEnableButton(): Boolean {
        return isSClicked && isSmClicked && isGClicked
    }
}