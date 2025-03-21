package com.gdsc.mio.model

import android.os.Parcelable
import com.gdsc.mio.RoadAddress
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// 검색 결과를 담는 클래스
@Parcelize
data class ResultSearchAddress(
    var meta: Meta, // 장소 메타데이터
    var documents: List<AddressData> // 검색 결과
) : Parcelable

@Parcelize
data class Meta(
    var total_count: Int, // 검색어에 검색된 문서 수
    var pageable_count: Int, // total_count 중 노출 가능 문서 수, 최대 45 (API에서 최대 45개 정보만 제공)
    var is_end: Boolean, // 현재 페이지가 마지막 페이지인지 여부, 값이 false면 page를 증가시켜 다음 페이지를 요청할 수 있음
    //var same_name: RegionInfo // 질의어의 지역 및 키워드 분석 정보
) : Parcelable

@Parcelize
data class AddressData(
    var address_name: String, // 전체 지번 주소
    var address_type: String, // 전체 지번 타입
    var x: String, // X 좌표값 혹은 longitude
    var y: String, // Y 좌표값 혹은 latitude
    @SerializedName("address")
    var address : Address?,
    @SerializedName("road_address")
    var road_address : RoadAddress?,
) : Parcelable

@Parcelize
data class Address(
    var address_name : String, //전체 지번 주소
    var region_1depth_name : String, //지역 1 Depth, 시도 단위
    var region_2depth_name : String, //지역 2 Depth, 구 단위
    var region_3depth_name : String, //지역 3 Depth, 동 단위
    var region_3depth_h_name : String, //지역 3 Depth, 행정동 명칭
    var x : String, //longitude
    var y : String, //latitude
) : Parcelable
