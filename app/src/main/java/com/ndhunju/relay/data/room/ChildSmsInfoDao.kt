package com.ndhunju.relay.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ndhunju.relay.data.ChildSmsInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildSmsInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(childSmsInfo: ChildSmsInfo): Long

    @Query("SELECT * FROM ChildSmsInfo WHERE childUserId=:childUserId")
    fun getAllChildSmsInfo(childUserId: String): Flow<List<ChildSmsInfo>>

    @Query("SELECT *, MAX(date) AS max_date\n" +
            "    FROM ChildSmsInfo\n" +
            "    WHERE childUserId = :childUserId\n" +
            "    GROUP BY threadId")
    fun getLastSmsOfChild(childUserId: String): Flow<List<ChildSmsInfo>>

    @Query("SELECT * FROM ChildSmsInfo WHERE childUserId=:childUserId AND threadId=:threadId")
    fun getAllChildSmsInfoOfChildAndThread(
        childUserId: String,
        threadId: String
    ): Flow<List<ChildSmsInfo>>

    @Update
    fun update(childSmsInfo: ChildSmsInfo)

    @Delete
    fun delete(childSmsInfo: ChildSmsInfo)
}