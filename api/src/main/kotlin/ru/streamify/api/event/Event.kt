package ru.streamify.api.event

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import java.time.ZonedDateTime

class Event<K, T>() {

    enum class Type {
        CREATE, DELETE
    }

    var eventType: Type? = null
    var key: K? = null
    var data: T? = null

    @JsonSerialize(using = ZonedDateTimeSerializer::class)
    val eventCreatedAt: ZonedDateTime = ZonedDateTime.now();

    constructor(eventType: Type?, key: K?) : this() {
        this.eventType = eventType
        this.key = key
    }

    constructor(eventType: Type?, key: K?, data: T?) : this(eventType, key) {
        this.data = data
    }
}