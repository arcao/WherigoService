package com.arcao.wherigoservice.response

class ErrorResponse(
    val statusCode : Int,
    val statusMessage : String,
    val errorMessage: String
)