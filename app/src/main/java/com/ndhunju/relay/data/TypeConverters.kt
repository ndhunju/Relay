package com.ndhunju.relay.data

import androidx.room.TypeConverter
import com.ndhunju.relay.api.Result

class TypeConverters {

    @TypeConverter
    fun fromCustomType(value: Result): String {
        return when(value) {
            is Result.Success -> "Success"
            is Result.Failure -> "Failure"
            Result.Pending -> "Pending"
        }
    }

    @TypeConverter
    fun toCustomType(value: String): Result {
        return when (value) {
            "Success" -> Result.Success()
            "Failure" -> Result.Failure()
            else -> Result.Pending
        }
    }

}