package com.ndhunju.relay.data

import kotlinx.coroutines.flow.Flow

/**
 * Implements [SmsInfoRepository] using local database to persist data offline
 */
class OfflineSmsInfoRepository(private val smsInfoDao: SmsInfoDao): SmsInfoRepository {

    override fun getSmsInfo(id: Int): Flow<SmsInfo> = smsInfoDao.getSmsInfo(id)

    override fun getAllSmsInfo(): Flow<List<SmsInfo>> = smsInfoDao.getAllSmsInfo()

    override suspend fun insertSmsInfo(smsInfo: SmsInfo) = smsInfoDao.insert(smsInfo)

    override suspend fun updateSmsInfo(smsInfo: SmsInfo) = smsInfoDao.update(smsInfo)

    override suspend fun deleteSmsInfo(smsInfo: SmsInfo) {
        smsInfoDao.delete(smsInfo)
    }
}