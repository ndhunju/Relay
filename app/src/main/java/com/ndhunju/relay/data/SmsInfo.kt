package com.ndhunju.relay.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ndhunju.relay.service.Result

@Entity
@androidx.room.TypeConverters(TypeConverters::class)
data class SmsInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Assign the id a default value of 0, which is necessary for the id to auto generate id values
    val idInAndroidOsTable: String,
    val threadId: String,
    val date: String,
    val syncStatus: Result?
)