package com.example.mio.Model

data class ResultReverseGeocode(
    val documents: List<ReverseGeocodeDocument>
)

data class ReverseGeocodeDocument(
    val address: ReverseGeocodeAddress
)

data class ReverseGeocodeAddress(
    val region1DepthName: String,
    val region2DepthName: String,
    val region3DepthName: String,
    val mainAddressNo: String
)