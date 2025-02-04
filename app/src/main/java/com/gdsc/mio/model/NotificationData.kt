package com.gdsc.mio.model

data class NotificationData(
    var notificationPos : Int,
    var writeUserId : String, //
    var notificationContentText : PostData, //이건 클릭 시 내용을 위해 필요
    var isApply : Boolean, //참석 체크
    var applyDate : String //참석 체크한 날짜

) : java.io.Serializable
