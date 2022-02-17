package com.arcao.wherigoservice.exception

import org.springframework.http.HttpStatus

class ServiceException(
    val status: HttpStatus,
    message: String
) : Exception(message)