package com.ndhunju.relay.util.gson

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.data.room.TypeConverters
import java.lang.reflect.Type

class ResultSerializer: JsonSerializer<Result<Void>> {
    override fun serialize(
        src: Result<Void>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(if (src == null) "" else TypeConverters.fromCustomType(src))
    }

}