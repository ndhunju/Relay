package com.ndhunju.relay.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsInfoDao {

    /**
     * @Note: The database operations can take a long time to execute, so they need to run on a
     * separate thread. Room doesn't allow database access on the main thread. Hence use suspend
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smsInfo: SmsInfo): Long
    // TODO: Handle the situation when the SMS was sent when the device was offline.
    //  The respective SmsInfo is stores this Sms with syncStatus to Pending

    @Update
    suspend fun update(smsInfo: SmsInfo)

    /**
     * @Note: The @Delete annotation deletes an item or a list of items. You need to pass the
     * entities you want to delete. If you don't have the entity, you might have to fetch it
     * before calling the delete() function.
     */
    @Delete
    suspend fun delete(smsInfo: SmsInfo)

    /**
     * @Note: With Flow as the return type, you receive notification whenever the data in the
     * database changes. The Room keeps this Flow updated for you, which means you only need
     * to explicitly get the data once. Because of the Flow return type, Room also runs the
     * query on the background thread. You don't need to explicitly make it a suspend function
     * and call it inside a coroutine scope.
     */
    @Query("SELECT * FROM SmsInfo WHERE id = :id")
    fun getSmsInfo(id: Int): Flow<SmsInfo?>

    @Query("SELECT * FROM SmsInfo WHERE idInAndroidOsTable = :idInAndroidDb")
    suspend fun getSmsInfoByIdInAndroidDb(idInAndroidDb: String): List<SmsInfo>

    /**
     * @Note: Room keeps this Flow updated for you, which means you only need to explicitly
     * get the data once.
     */
    @Query("SELECT * From SmsInfo ORDER BY date DESC")
    fun getAllSmsInfo(): Flow<List<SmsInfo>>

}