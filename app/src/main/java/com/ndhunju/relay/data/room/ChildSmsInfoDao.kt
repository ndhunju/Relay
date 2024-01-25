package com.ndhunju.relay.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ndhunju.relay.data.ChildSmsInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildSmsInfoDao {

    @Insert
    suspend fun insert(childSmsInfo: ChildSmsInfo): Long

    @Query("SELECT * FROM ChildSmsInfo WHERE childUserId=:childUserId")
    fun getAllChildSmsInfo(childUserId: String): Flow<List<ChildSmsInfo>>

    @Update
    fun update(childSmsInfo: ChildSmsInfo)

    @Delete
    fun delete(childSmsInfo: ChildSmsInfo)
}