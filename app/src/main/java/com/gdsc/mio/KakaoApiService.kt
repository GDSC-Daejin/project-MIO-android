package com.gdsc.mio

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


// Kakao API로부터 받아온 응답을 처리하기 위한 데이터 클래스 정의
@Parcelize
data class SearchResult(
    val documents: List<PlaceDocument>
) : Parcelable

@Parcelize
data class PlaceDocument(
    val road_address: RoadAddress
) : Parcelable

@Parcelize
data class RoadAddress(
    @SerializedName("address_name")
    val address_name : String?,
    @SerializedName("building_name")
    val building_name: String?
) : Parcelable