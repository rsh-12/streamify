package ru.streamify.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.streamify.api.exception.BadRequestException
import ru.streamify.api.exception.InvalidInputException
import ru.streamify.api.exception.NotFoundException
import java.time.ZonedDateTime

@RestControllerAdvice
class GlobalControllerExceptionHandler {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestExceptions(request: ServerHttpRequest, ex: BadRequestException): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.BAD_REQUEST, request, ex)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundExceptions(request: ServerHttpRequest, ex: NotFoundException): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, request, ex)
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException::class)
    fun handleInvalidInputException(request: ServerHttpRequest, ex: InvalidInputException): HttpErrorInfo {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, ex)
    }

    private fun createHttpErrorInfo(httpStatus: HttpStatus, request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        val path = request.path.pathWithinApplication().value()
        val message = ex.message ?: ""

        log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message)

        return HttpErrorInfo(ZonedDateTime.now(), path, httpStatus, message)
    }
}