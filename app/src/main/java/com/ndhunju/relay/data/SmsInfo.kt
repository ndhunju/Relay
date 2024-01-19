package com.ndhunju.relay.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ndhunju.relay.api.Result
import com.ndhunju.relay.data.room.TypeConverters

@Entity(
    indices = [Index("idInAndroidOsTable", unique = true)]
)
@androidx.room.TypeConverters(TypeConverters::class)
data class SmsInfo(
    // Give id a default value of 0, which is necessary for the id to auto generate id values
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idInAndroidOsTable: String,
    val threadId: String,
    val date: String,
    val syncStatus: Result?
)