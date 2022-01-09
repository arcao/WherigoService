package com.arcao.wherigoservice.response

import com.fasterxml.jackson.annotation.JsonProperty

abstract class BaseV1Response(
    @JsonProperty("Status")
    val status: Status
)

class Status(
    @JsonProperty("Code")
    val code: Int,
    @JsonProperty("Text")
    val text: String
) {
    companion object {
        val OK = Status(0, "OK")
    }
}