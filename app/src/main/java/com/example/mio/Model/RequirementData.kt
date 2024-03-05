package com.example.mio.Model

class RequirementData(
     var isFirstVF: FirstVF,
     var isSecondVF: SecondVF,
     var isThirdVF: ThirdVF,
     var isFourthVF : FourthVF
)

data class FirstVF(
    var isTitle : Boolean,
    var isCalendar : Boolean,
    var isTime : Boolean,
    var isFirst : Boolean,
)

data class SecondVF(
    var isPlaceName : Boolean,
    var isPlaceRode : Boolean,
    var isSecond : Boolean,
)

data class ThirdVF(
    var isAmount: Boolean,
    //등/하교 체크
    var isGSchool : Boolean,
    var isASchool: Boolean,
    //성별
    var isMGender : Boolean,
    var isWGender: Boolean,
    //흡연
    var isSmoke : Boolean,
    var isNSmoke : Boolean,
    var isThird : Boolean,
)

data class FourthVF(
    var isContent : Boolean,
    var isFourth : Boolean
)

