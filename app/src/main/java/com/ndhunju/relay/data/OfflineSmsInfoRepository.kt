package com.ndhunju.relay.data

import com.ndhunju.relay.data.room.SmsInfoDao
import kotlinx.coroutines.flow.Flow

/**
 * Implements [SmsInfoRepository] using local database to persist data offline
 */
class OfflineSmsInfoRepository(private val smsInfoDao: SmsInfoDao): SmsInfoRepository {

    override fun getSmsInfo(id: Long): Flow<SmsInfo?> = smsInfoDao.getSmsInfo(id)

    override suspend fun getSmsInfoForEachIdInAndroidDb(
        idInAndroidDbs: List<String>
    ): List<SmsInfo?> {
        return idInAndroidDbs.map { idInAndroidDb ->
            // Although it should not happen, multiple row could have been created for same Message
            smsInfoDao.getSmsInfoByIdInAndroidDb(idInAndroidDb).firstOrNull()
        }
    }

    override fun getAllSmsInfo(): Flow<List<SmsInfo>> = smsInfoDao.getAllSmsInfo()

    override suspend fun insertSmsInfo(smsInfo: SmsInfo) = smsInfoDao.insert(smsInfo)

    override suspend fun updateSmsInfo(smsInfo: SmsInfo) = smsInfoDao.update(smsInfo)

    override suspend fun deleteSmsInfo(smsInfo: SmsInfo) {
        smsInfoDao.delete(smsInfo)
    }
}