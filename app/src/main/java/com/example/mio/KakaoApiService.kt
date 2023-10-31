package com.example.mio

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

const val BASE_URL = "https://dapi.kakao.com/"
const val KAKAO_API_KEY = BuildConfig.map_api_key

interface KakaoApiService {
    @Headers("Authorization: KakaoAK $KAKAO_API_KEY")
    @GET("v2/local/geo/coord2address")
    suspend fun getAddress(
        @Query("x") longitude: Double,
        @Query("y") latitude: Double
    ): AddressResponse
}

data class AddressResponse(
    val documents: List<Document>
)

data class Document(
    val address: Address
)

data class Address(
    val address_name: String
)

object RetrofitClient {
    val apiService: KakaoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoApiService::class.java)
    }
}