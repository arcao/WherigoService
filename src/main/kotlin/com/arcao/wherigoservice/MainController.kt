package com.arcao.wherigoservice

import com.arcao.wherigoservice.exception.ServiceException
import com.arcao.wherigoservice.response.ErrorResponse
import com.arcao.wherigoservice.response.GuidToReferenceCodeResponse
import com.arcao.wherigoservice.response.TimeResponse
import com.arcao.wherigoservice.response.TimeResult
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController {
    val client by lazy {
        OkHttpClient.Builder().build()
    }

    @GetMapping("/api/getTime")
    fun getTime(): TimeResponse {
        return TimeResponse(TimeResult(System.currentTimeMillis()))
    }

    @GetMapping("/api/v2/guidToReferenceCode")
    fun guidToReferenceCode(@RequestParam("guid") guid: String?): GuidToReferenceCodeResponse {
        if (guid == null) {
            throw ServiceException(HttpStatus.BAD_REQUEST, "Missing guid parameter")
        }

        val request = Request.Builder()
            .url("https://www.geocaching.com/seek/cache_details.aspx?guid=$guid")
            .get()
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36"
            )
            .header(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
            )
            .header("Accept-Language", "en-US;q=0.9,en;q=0.8")
            .build()
        try {
            val body = client.newCall(request).execute().body()?.charStream()?.readText().orEmpty()
            val matches = PATTERN.find(body)
            if (matches == null || matches.groups.size != 2) {
                println(body)
                throw ServiceException(HttpStatus.NOT_FOUND, "Geocache not found")
            }

            return GuidToReferenceCodeResponse(requireNotNull(matches.groups[1]?.value))
        } catch (e: ServiceException) {
            throw e
        } catch (e: Exception) {
            throw ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, e.message.orEmpty())
        }
    }

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(exception: ServiceException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(exception.status.value(), exception.status.reasonPhrase, exception.message.orEmpty()),
            exception.status
        )
    }

    companion object {
        val PATTERN: Regex = Regex("(?:https://coord.info/|wp=)(GC[A-Z0-9]+)")
    }
}