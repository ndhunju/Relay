package com.ndhunju.relay.data

import com.ndhunju.relay.data.room.ChildSmsInfoDao
import kotlinx.coroutines.flow.Flow

class OfflineChildSmsInfoRepository(
    private val childSmsInfoDao: ChildSmsInfoDao
) : ChildSmsInfoRepository {

    override suspend fun insert(childSmsInfo: ChildSmsInfo): Long
    = childSmsInfoDao.insert(childSmsInfo)

    override fun getAllChildSmsInfo(childUserId: String): Flow<List<ChildSmsInfo>>
    = childSmsInfoDao.getAllChildSmsInfo(childUserId)

    override fun getLastSmsInfoOfChild(childUserId: String): Flow<List<ChildSmsInfo>>
    = childSmsInfoDao.getLastSmsOfChild(childUserId)

    override fun getAllChildSmsInfoOfChildAndThread(
        childUserId: String,
        threadId: String
    ): Flow<List<ChildSmsInfo>> = childSmsInfoDao.getAllChildSmsInfoOfChildAndThread(
        childUserId,
        threadId
    )

    override fun update(childSmsInfo: ChildSmsInfo)
    = childSmsInfoDao.update(childSmsInfo)

    override fun delete(childSmsInfo: ChildSmsInfo)
    = childSmsInfoDao.delete(childSmsInfo)

}