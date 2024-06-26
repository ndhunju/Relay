package com.ndhunju.relay.api

import android.app.Application
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ndhunju.relay.api.response.Settings
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.service.analyticsprovider.AnalyticsProvider
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.util.CurrentUser
import com.ndhunju.relay.util.User
import com.ndhunju.relay.util.connectivity.NetworkConnectionChecker
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

private val TAG = ApiInterfaceFireStoreImpl::class.simpleName

/**
 * Implements [ApiInterface] using [Firebase.firestore]
 */
class ApiInterfaceFireStoreImpl(
    private val application: Application,
    private val fireStore: FirebaseFirestore,
    private val gson: Gson,
    private val currentUser: CurrentUser,
    private val analyticsProvider: AnalyticsProvider
) : ApiInterface {

    private val context by lazy { application }

    private val userId: String
        get() {
           return currentUser.user.id
        }

    /**
     * Holds [CollectionReference] for "User" collection
     */
    private val userCollection by lazy { fireStore.collection(Collections.User) }
    private val settingsCollection by lazy { fireStore.collection(Collections.Settings) }
    private val parentChildCollection by lazy { fireStore.collection(Collections.ParentChild) }
    private val messageCollection by lazy { fireStore.collection(Collections.Message) }
    private val messageFetcherCollection by lazy { fireStore.collection(Collections.MessageFetcher) }
    private val pushNotificationCollection by lazy { fireStore.collection(Collections.PushNotificationToken) }

    // TypeToken used for parsing list
    private val listOfStringType = object : TypeToken<List<String>>(){}.type

    override suspend fun getSettings(): Result<Settings> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            val settings = settingsCollection.get().await().documents.firstOrNull()
                ?.toObject(Settings::class.java)
            Result.Success(settings)
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            analyticsProvider.logEvent("didFailToGetSettings", ex.message)
            Result.Failure(ex)
        }

    }

    /**
     * Creates user in the cloud database.
     * TODO: Nikesh - Use third party Identity Provider to authenticate and create user
     */
    override suspend fun postUser(
        name: String?,
        phone: String?,
    ): Result<String> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        val newUser = makeMapForUserCollection(name, phone)

        return try {
            val userWithPhoneNumber = userCollection
                .whereEqualTo(UserEntry.Phone, phone)
                .get().await().documents

            return if (userWithPhoneNumber.isNotEmpty()) {
                // Check if this number should be bypassed for duplication. This happens when Google
                // Play store tests our app with the same number to login again and again
                if (getSettings().getDataOrNull<Settings>()?.byPassAccountCreationNumber == phone) {
                    Result.Success(userWithPhoneNumber.first().id)
                } else {
                    Result.Failure(PhoneAlreadyRegisteredException("$phone already registered."))
                }
            } else {
                val newUserId = userCollection.add(newUser).await().id
                Result.Success(newUserId)
            }
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            analyticsProvider.logEvent("didFailToCreateUser", ex.message)
            Result.Failure(ex)
        }
    }

    /**
     * Updates user in cloud database
     */
    override suspend fun putUser(
        name: String?,
    ): Result<Nothing> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        // If null is passed, use existing values
        val finalName = name ?: currentUser.user.name

        return try {
            userCollection.document(userId).update(
                makeMapForUserCollection(name = finalName).toMap()
            ).await()
            Result.Success()
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            analyticsProvider.logEvent("didFailToUpdateUser", ex.message)
            Result.Failure(ex)
        }
    }

    /**
     * Returns a map that corresponds to the fields of "User" collection in the cloud database.
     * This function skips the value that is null
     */
    private fun makeMapForUserCollection(
        name: String? = null,
        phone: String? = null,
    ) = hashMapOf(
        UserEntry.Name to name,
        UserEntry.Phone to phone,
    ).filter {
        // Don't include in the map if the value is null as
        // Firebase overrides existing value with null
        it.value?.isNotEmpty() == true
    }

    override suspend fun postPairWithParent(
        childUserId: String,
        parentPhone: String
    ): Result<String> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            // Check that such parent phone address already exists
            val queryByPhone = userCollection.whereEqualTo(UserEntry.Phone, parentPhone)
            val querySnapshot = queryByPhone.get().await()
            if (querySnapshot.isEmpty) {
                return Result.Failure(UserNotFoundException("Parent phone id not found"))
            }
            val parentUserId = querySnapshot.documents.first().id

            // Check if childUserId exists as well
            val queryById = userCollection.document(childUserId).get().await()
            if (queryById.exists().not()) {
                return Result.Failure(UserNotFoundException("Child user not found"))
            }

            // Register this pairing in the database
            parentChildCollection.add(hashMapOf(
                ParentChild.ChildUserId to childUserId,
                ParentChild.ParentUserId to parentUserId
            )).await()

            Result.Success(parentUserId)
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            Result.Failure(ex)
        }

    }

    override suspend fun postUnPairWithParent(
        childUserId: String,
        parentUserId: String
    ): Result<Boolean> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            val parentChildDocs = parentChildCollection
                .whereEqualTo(ParentChild.ParentUserId, parentUserId)
                .whereEqualTo(ParentChild.ChildUserId, childUserId)
                .get().await().documents

            // There should be only one entry but in case there are multiple, log it
            if (parentChildDocs.size > 1) {
                analyticsProvider.logEvent(
                    "didFindMultipleEntryForSameParentChildPair",
                    "${parentChildDocs.size} entries"
                )
            }

            parentChildDocs.forEach { parentChildDoc ->
                parentChildCollection.document(parentChildDoc.id).delete().await()
            }

            return Result.Success(true)

        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            Result.Failure(ex)
        }

    }

    @Deprecated("See parent message")
    override suspend fun postPairWithChild(
        childPhoneNumber: String,
        pairingCode: String
    ): Result<String> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            val queryByPhoneNumber = userCollection
                .whereEqualTo(UserEntry.Phone, childPhoneNumber)
                .whereEqualTo("Key", pairingCode)
            val querySnapshot = queryByPhoneNumber.get().await()
            if (querySnapshot.isEmpty) {
                return Result.Failure(UserNotFoundException("Matching child user not found"))
            }
            val childUserId = querySnapshot.documents.first().id

            // Register this pairing in the database
            parentChildCollection.add(hashMapOf(
                ParentChild.ChildUserId to childUserId,
                ParentChild.ParentUserId to currentUser.user.id
            )).await()

            Result.Success(childUserId)
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            Result.Failure(ex)
        }

    }

    override suspend fun getChildUsers(parentUserId: String): Result<List<Child>> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            // Fetch the list of child user ids for passed parent
            val childUserIds = parentChildCollection
                .whereEqualTo(ParentChild.ParentUserId, parentUserId)
                .get()
                .await()
                .documents.map { it.get(ParentChild.ChildUserId) as String }
                .distinct() // Filter out duplicates
                .filter { it.isNotEmpty() } // Filter out empty strings

            // Fetch the phone number of each child user
            val childList = childUserIds.map { childUserId ->
                val phoneNumber = userCollection
                    .whereEqualTo(FieldPath.documentId(), childUserId)
                    .get()
                    .await()
                    .documents.first()
                    .get(UserEntry.Phone) as String
                Child(childUserId, phoneNumber)
            }

            Result.Success(childList)
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            return Result.Failure(ex)
        }

    }

    override suspend fun getParentUsers(childUserId: String): Result<List<User>> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            // Fetch the list of child user ids for passed parent
            val parentUserIds = parentChildCollection
                .whereEqualTo(ParentChild.ChildUserId, childUserId)
                .get()
                .await()
                .documents.map { it.get(ParentChild.ParentUserId) as String }
                .distinct() // Filter out duplicates
                .filter { it.isNotEmpty() } // Filter out empty strings

            // Fetch the phone number of each parent user
            val parentList = parentUserIds.map { parentUserId ->
                val phone = userCollection
                    .whereEqualTo(FieldPath.documentId(), parentUserId)
                    .get()
                    .await()
                    .documents.first()
                    .get(UserEntry.Phone) as String
                User(id = parentUserId, phone = phone)
            }

            Result.Success(parentList)
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            return Result.Failure(ex)
        }

    }

    override suspend fun getMessagesFromChildUser(
        childUserId: String
    ): Result<List<MessageEntry>> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            val messageEntries = messageCollection
                .whereEqualTo(MessageEntry.SenderUserId, childUserId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    MessageEntry(
                        idInServer = doc.id,
                        senderUserId = doc.get(MessageEntry.SenderUserId) as String,
                        sentDate = doc.get(MessageEntry.SentDate) as String,
                        payLoad = doc.get(MessageEntry.PayLoad) as String
                    )
                }
            Result.Success(messageEntries)
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            Result.Failure(ex)
        }
    }

    override suspend fun postDidSaveFetchedMessages(
        childSmsInfoList: List<ChildSmsInfo>
    ): Result<Nothing> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        return try {
            val failedMessageIds = arrayListOf<String>()
            childSmsInfoList.forEach{ childSmsInfo ->
                // Ideally, we should use "Functions" feature of Firestore where the logic
                // there would delete the message only after all the parents have fetched
                // and saved the messages. But since "Functions" feature costs money,
                // taking this workaround
                val isSuccess = removeCurrentUserFromFetcherList(childSmsInfo)
                if (isSuccess.not()) failedMessageIds.add(childSmsInfo.idInServerDb)
                //Log.d(TAG, "notifyDidSaveFetchedMessages: updated for ${childSmsInfo.idInServerDb}")
            }

            if (failedMessageIds.isNotEmpty()) {
                // Log all the failed transactions
                analyticsProvider.logEvent(
                    "didFailToNotifyFetchedMessageForSome",
                    "${failedMessageIds.size}/${childSmsInfoList.size}"
                )
            }
            Result.Success()
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            Result.Failure(ex)
        }
    }

    /**
     * Removes current user from the list of FetcherUserIds list stored in
     * [messageFetcherCollection]. Also, deletes the entire document/entry
     * in [messageFetcherCollection] and [messageCollection] if current
     * user was the last fetcher.
     */
    private suspend fun removeCurrentUserFromFetcherList(childSmsInfo: ChildSmsInfo): Boolean {
        return try {
            fireStore.runTransaction { tx ->
                runBlocking {
                    removeCurrentUserFromFetcherListHelper(childSmsInfo, tx)
                }
            }.await()
            true
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * Just a helper for [removeCurrentUserFromFetcherList]
     */
    private suspend fun removeCurrentUserFromFetcherListHelper(
        childSmsInfo: ChildSmsInfo,
        tx: Transaction
    ) {
        // Get ID of the document that stores childSmsInfo
        val childSmsInfoDocId = messageFetcherCollection
            .whereEqualTo(MessageFetcher.MessageId, childSmsInfo.idInServerDb)
            .get().await().documents.firstOrNull()?.id ?: throw Exception("Not found")

        // Get reference to the documents since tx need a doc ref
        val childSmsInfoDocRef = messageFetcherCollection.document(childSmsInfoDocId)
        val messageDocRef = messageCollection.document(childSmsInfo.idInServerDb)
        // Get doc snapshot of the same document via tx so that it is always latest
        val childSmsInfoTxDocSnap = tx.get(childSmsInfoDocRef)

        // Retrieve the value stored in "ParentUserIds" field
        val parentUserIds = gson.fromJson<List<String>>(
            childSmsInfoTxDocSnap.get(MessageFetcher.FetcherUserIds) as String,
            listOfStringType
        )

        //Log.d(TAG, "updateEntriesInDatabaseWithTx: " +
        //        "Stored parentUserIds ${childSmsInfoTxDocSnap.get("FetcherUserIds") as String}")

        // Remove current user from the list
        val remainingParents = parentUserIds?.filter { parentUserId ->
            parentUserId != currentUser.user.id
        }

        // Check if remainingParent is empty
        if (remainingParents?.isEmpty() == true) {
            // Delete the entry for current childSmsInfo in MessageFetcher collection
            tx.delete(childSmsInfoDocRef)
            // Delete the entry for current childSmsInfo in Message collection
            tx.delete(messageDocRef)
        } else {
            tx.update(
                childSmsInfoDocRef,
                MessageFetcher.FetcherUserIds,
                gson.toJson(remainingParents)
            )
        }
    }

    /**
     * Pushes [message] to the cloud database.
     */
    override suspend fun postMessage(message: MessageEntry): Result<Nothing> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        if (currentUser.isUserSignedIn().not()) {
            return Result.Failure(UserSignedOutException("User is not signed in."))
        }

        val mapOfMessageField = mapOf(
            MessageEntry.SenderUserId to message.senderUserId,
            MessageEntry.SentDate to message.sentDate,
            MessageEntry.PayLoad to message.payLoad
        )

        return try {
            val messageIdInServer = messageCollection.add(mapOfMessageField).await().id
            message.idInServer = messageIdInServer
            // Store all the parent Ids as fetcherUserIds that needs to fetch this message
            // before it could be deleted from the database
            messageFetcherCollection.add(hashMapOf(
                MessageFetcher.MessageId to messageIdInServer,
                MessageFetcher.FetcherUserIds to gson.toJson(
                    currentUser.user.getParentUsers().map { it.id }
                )
            ))
            Result.Success()
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            analyticsProvider.logEvent("didFailToPushMessage", ex.message)
            Result.Failure(ex)
        }
    }

    override suspend fun postUserPushNotificationToken(token: String): Result<Nothing> {

        val exception = checkForCommonExceptions()
        if (exception != null) return Result.Failure(exception)

        if (currentUser.isUserSignedIn().not()) {
            return Result.Failure(UserSignedOutException("User is not signed in."))
        }

        return try {
            val existingPushNotificationTokenEntry = pushNotificationCollection.whereEqualTo(
                PushNotificationToken.UserId,
                currentUser.user.id
            ).get().await().documents.firstOrNull()

            if (existingPushNotificationTokenEntry != null) {
                pushNotificationCollection.document(existingPushNotificationTokenEntry.id)
                    .update(PushNotificationToken.Token, token).await()
            } else {
                pushNotificationCollection.add(
                    hashMapOf(
                        PushNotificationToken.UserId to currentUser.user.id,
                        PushNotificationToken.Token to token
                    )
                ).await()
            }

            Result.Success()
        } catch (ex: Exception) {
            logIfFirebaseException(ex)
            analyticsProvider.logEvent("didFailToPostPushNotificationToken", ex.message)
            Result.Failure()
        }

    }

    private fun checkForCommonExceptions(): Exception? {
        if (NetworkConnectionChecker.checkIfDeviceHasInternet(context).not()) {
            return NetworkNotFoundException
        }

        return null
    }

    private fun logIfFirebaseException(ex: Exception) {
        if (ex is FirebaseFirestoreException) {
            analyticsProvider.logEvent(
                "didCatchFirebaseFirestoreException",
                "${ex.message}\nStack: ${Throwable().stackTrace.getOrNull(1)}"
            )
        }
    }

    /**
     * Represents all the [CollectionReference] we have
     */
    object Collections {
        const val User = "User"
        const val ParentChild = "ParentChild"
        const val Message = "Message"
        const val MessageFetcher = "MessageFetcher"
        const val PushNotificationToken = "PushNotificationToken"
        const val Settings = "Settings"
    }

    /**
     * Represent fields of User collection
     */
    object UserEntry {
        const val Name = "Name"
        const val Phone = "Phone"
    }

    /**
     * Represent fields of ParentChild collection
     */
    object ParentChild {
        const val ChildUserId = "ChildUserId"
        const val ParentUserId = "ParentUserId"
    }

    /**
     * Represent fields of MessageFetcher collection
     */
    object MessageFetcher {
        const val MessageId = "MessageId"
        const val FetcherUserIds = "FetcherUserIds"
    }

    /**
     * Represents fields of [Collections.PushNotificationToken] collection
     */
    object PushNotificationToken {
        const val UserId = "UserId"
        const val Token = "Token"
    }
}

/**
 * Exception thrown when user is not registered already
 */
class UserNotFoundException(message: String): Exception(message)

/**
 * Exception thrown when phone number is already used
 */
class PhoneAlreadyRegisteredException(message: String): Exception(message)

/**
 * Exception thrown when user is not signed in
 */
class UserSignedOutException(message: String): Exception(message)

/**
 * Exception thrown when device is not connected to the network/internet
 */
object NetworkNotFoundException: Exception() {
    private fun readResolve(): Any = NetworkNotFoundException
}