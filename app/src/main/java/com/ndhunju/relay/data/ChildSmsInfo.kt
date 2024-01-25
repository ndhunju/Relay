package com.ndhunju.relay.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ChildSmsInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var childUserId: String,
    val idInAndroidDb: String?,
    val threadId: String,
    val from: String,
    val body: String,
    val date: String,
    val type: String,
    val extra: String? = null
)