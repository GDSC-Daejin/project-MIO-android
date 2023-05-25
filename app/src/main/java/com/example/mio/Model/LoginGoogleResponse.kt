package com.example.mio.Model

data class LoginGoogleResponse(
    var access_token: String = "",
    var expires_in: Int = 0,
    var scope: String = "",
    var token_type: String = "",
    var id_token: String = "",
)