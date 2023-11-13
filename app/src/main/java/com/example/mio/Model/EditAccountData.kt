package com.example.mio.Model

data class EditAccountData(
    var gender : Boolean, //false 남 true여
    var verifySmoker : Boolean,//false 비흡, true 흡
    var accountNumber : String,
    var activityLocation : String
)