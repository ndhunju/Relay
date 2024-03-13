package com.ndhunju.relay.data.room

import androidx.room.TypeConverter
import com.ndhunju.relay.api.Result

object TypeConverters {

    @TypeConverter
    fun fromCustomType(value: Result<Void>): String {
        return when(value) {
            is Result.Success -> "Success"
            is Result.Failure -> "Failure"
            is Result.Pending -> "Pending"
        }
    }

    @TypeConverter
    fun toCustomType(value: String): Result<Void> {
        return when (value) {
            "Success" -> Result.Success()
            "Failure" -> Result.Failure()
            else -> Result.Pending()
        }
    }

}