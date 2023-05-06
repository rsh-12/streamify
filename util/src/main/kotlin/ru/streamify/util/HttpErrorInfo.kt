package ru.streamify.util

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

data class HttpErrorInfo(
    val timestamp: ZonedDateTime,
    val path: String,
    val httpStatus: HttpStatus,
    val message: String
)