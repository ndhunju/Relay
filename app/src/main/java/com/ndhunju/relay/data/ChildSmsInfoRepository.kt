package com.ndhunju.relay.data

import kotlinx.coroutines.flow.Flow

interface ChildSmsInfoRepository {
    suspend fun insert(childSmsInfo: ChildSmsInfo): Long
    fun getAllChildSmsInfo(childUserId: String): Flow<List<ChildSmsInfo>>
    fun getLastSmsInfoOfChild(childUserId: String): Flow<List<ChildSmsInfo>>

    /**
     * Returns [ChildSmsInfo] for passed [childUserId] and [threadId]
     */
    fun getAllChildSmsInfoOfChildAndThread(
        childUserId: String,
        threadId: String
    ): Flow<List<ChildSmsInfo>>

    fun update(childSmsInfo: ChildSmsInfo)
    fun delete(childSmsInfo: ChildSmsInfo)
}