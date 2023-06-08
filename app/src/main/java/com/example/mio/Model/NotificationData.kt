package com.example.mio.Model

data class NotificationData(
    var notificationPos : Int,
    var writeUserId : String, //
    var notificationContentText : String, //알람 내용..? 굳이필요?
    var isApply : Boolean, //참석 체크
    var applyDate : String //참석 체크한 날짜

)
