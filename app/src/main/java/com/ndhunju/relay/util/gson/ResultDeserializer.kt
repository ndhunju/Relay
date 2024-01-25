package com.ndhunju.relay.util.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.data.room.TypeConverters
import java.lang.reflect.Type

class ResultDeserializer: JsonDeserializer<Result?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Result? {
        return if (json == null || json.toString().isEmpty() || json.toString() == "null")
            null
        else
            TypeConverters.toCustomType(json.toString())
    }

}