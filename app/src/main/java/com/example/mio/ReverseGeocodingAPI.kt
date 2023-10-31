package com.example.mio

import com.example.mio.Model.ResultReverseGeocode
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ReverseGeocodingAPI {
    @GET("v2/local/geo/coord2address")
    fun getReverseGeocode(
        @Header("Authorization") apiKey: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double
    ): Call<ResultReverseGeocode>
}