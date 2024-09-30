package com.example.mio

import android.location.Address
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

const val BASE_URL = "https://dapi.kakao.com/"
const val KAKAO_API_KEY = BuildConfig.map_api_key

interface KakaoApiService {
    @GET("v2/local/search/address.json")
    //https://developers.kakao.com/docs/latest/ko/local/dev-guide#coord-to-address-response-body-meta
    //위 보고 수정해보기
    fun searchAddress(
        @Query("query") query: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radius: Int = 1000, // 반경 설정 (미터)
        @Query("sort") sort: String = "accuracy", // 정확도 기준으로 정렬
        @Query("page") page: Int = 10,
        @Query("size") size: Int = 10,
        @Header("Authorization") apiKey: String
    ): Call<SearchResult>
}

// Kakao API로부터 받아온 응답을 처리하기 위한 데이터 클래스 정의
data class SearchResult(
    val documents: List<PlaceDocument>
)

data class PlaceDocument(
    val road_address: RoadAddress
)

data class RoadAddress(
    @SerializedName("address_name")
    val address_name : String?,
    @SerializedName("building_name")
    val building_name: String?
)

data class AddressName(
    val address_name : Address
)