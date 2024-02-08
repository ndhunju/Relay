package com.ndhunju.relay.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and
 * retrieve of [SmsInfo] from a given data source.
 */
interface SmsInfoRepository {

    /**
     * Retrieve an [SmsInfo] from the given data source that matches with the [id]
     */
    fun getSmsInfo(id: Long): Flow<SmsInfo?>

    /**
     * Retrieve list of [SmsInfo] from the given data source
     * that matches with the list of [idInAndroidDbs]
     */
    suspend fun getSmsInfoForEachIdInAndroidDb(idInAndroidDbs: List<String>): List<SmsInfo?>

    /**
     * Retrieve all [SmsInfo] from the given data source
     */
    fun getAllSmsInfo(): Flow<List<SmsInfo>>

    /**
     * Insert [SmsInfo] in the data source
     */
    suspend fun insertSmsInfo(smsInfo: SmsInfo): Long


    /**
     * Update [SmsInfo] from the data source
     */
    suspend fun updateSmsInfo(smsInfo: SmsInfo)

    /**
     * Delete [SmsInfo] from the data source
     */
    suspend fun deleteSmsInfo(smsInfo: SmsInfo)


}