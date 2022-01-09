package com.arcao.wherigoservice.response

import com.fasterxml.jackson.annotation.JsonProperty

class TimeResponse(
    @JsonProperty("TimeResult")
    val timeResult: TimeResult
) : BaseV1Response(Status.OK)

class TimeResult(
    @JsonProperty("Time")
    val time: Long
)