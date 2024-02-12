package com.ndhunju.relay.api

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.service.AnalyticsManager
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

private val TAG = ApiInterfaceFireStoreImpl::class.simpleName

/**
 * Implements [ApiInterface] using [Firebase.firestore]
 */
class ApiInterfaceFireStoreImpl(
    private val gson: Gson,
    private val currentUser: CurrentUser,
    private val analyticsManager: AnalyticsManager
) : ApiInterface {

    private var userId: String
        get() {
           return currentUser.user.id
        }
        set(value) {
            currentUser.user = currentUser.user.copy(id = value)
        }

    /**
     * Holds [CollectionReference] for "User" collection
     */
    private val userCollection = Firebase.firestore.collection(Collections.User)
    private val parentChildCollection = Firebase.firestore.collection(Collections.ParentChild)
    private val messageCollection = Firebase.firestore.collection(Collections.Message)
    private val messageFetcherCollection = Firebase.firestore.collection(Collections.MessageFetcher)

    // TypeToken used for parsing list
    private val listOfStringType = object : TypeToken<List<String>>(){}.type

    /**
     * Creates user in the cloud database.
     * TODO: Nikesh - Use third party Identity Provider to authenticate and create user
     */
    override suspend fun createUser(
        name: String?,
        email: String?,
        phone: String?,
        deviceId: String?,
        pushNotificationToken: String?,
    ): Result {
        val newUser = makeMapForUserCollection(name, email, phone, deviceId, pushNotificationToken)

        val userWithEmailExits = userCollection
            .whereEqualTo(User.Email, email)
            .get().await().documents.isNotEmpty()

        if (userWithEmailExits) {
            return Result.Failure(
                EmailAlreadyExistException("User with email $email already exits.")
            )
        }

        return try {
            userId = userCollection.add(newUser).await().id
            Result.Success(userId)
        } catch (ex: Exception) {
            Log.d(TAG, "createUser: Failed with $ex")
            Result.Failure(ex)
        }
    }

    /**
     * Updates user in cloud database
     */
    override suspend fun updateUser(
        name: String?,
        phone: String?,
    ): Result {
        // If null is passed, use existing values
        val finalName = name ?: currentUser.user.name
        val finalPhone = phone ?: currentUser.user.phone

        return try {
            userCollection.document(userId).update(
                makeMapForUserCollection(name = finalName, phone = finalPhone).toMap()
            ).await()
            Result.Success()
        } catch (ex: Exception) {
            Log.d(TAG, "updateUser: Failed to update user")
            Result.Failure(ex)
        }
    }

    /**
     * Returns a map that corresponds to the fields of "User" collection in the cloud database.
     * This function skips the value that is null
     */
    private fun makeMapForUserCollection(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        deviceId: String? = null,
        pushNotificationToken: String? = null
    ) = hashMapOf(
        User.Name to name,
        User.Email to email,
        User.Phone to phone,
        User.DeviceId to deviceId,
        User.PushNotificationToken to pushNotificationToken
    ).filter {
        // Don't include in the map if the value is null as
        // Firebase overrides existing value with null
        it.value?.isNotEmpty() == true
    }

    override suspend fun pairWithParent(childUserId: String, parentEmailAddress: String): Result {
        val flow = MutableStateFlow<Result>(Result.Pending)
        // TODO: Nikesh - check if user has already paired with 3 parents
        // Check that such parent email address already exists
        return try {
            val queryByEmail = userCollection.whereEqualTo("Email", parentEmailAddress)
            val querySnapshot = queryByEmail.get().await()
            if (querySnapshot.isEmpty) {
                return Result.Failure(EmailNotFoundException("Parent email id not found"))
            }
            val parentUserId = querySnapshot.documents.first().id

            // Register this pairing in the database
            parentChildCollection.add(hashMapOf(
                ParentChild.ChildUserId to childUserId,
                ParentChild.ParentUserId to parentUserId
            )).await()

            Result.Success(parentUserId)
        } catch (ex: Exception) {
            Result.Failure(ex)
        }

    }

    override suspend fun fetchChildUsers(parentUserId: String): Result {
        return try {
            // Fetch the list of child user ids for passed parent
            val childUserIds = parentChildCollection
                .whereEqualTo(ParentChild.ParentUserId, parentUserId)
                .get()
                .await()
                .documents.map { it.get(ParentChild.ChildUserId) as String }
                .distinct() // Filter out duplicates
                .filter { it.isNotEmpty() } // Filter out empty strings

            // Fetch the email of each child user
            val childList = childUserIds.map { childUserId ->
                val email = userCollection
                    .whereEqualTo(FieldPath.documentId(), childUserId)
                    .get()
                    .await()
                    .documents.first()
                    .get(User.Email) as String
                Child(childUserId, email)
            }

            Result.Success(childList)
        } catch (ex: Exception) {
            return Result.Failure(ex)
        }

    }

    override suspend fun fetchMessagesFromChildUsers(childUserIds: List<String>): Result {
        val childSmsInfoList = mutableListOf<ChildSmsInfo>()
        try {
            for (childUserId2 in childUserIds) {
                val childSmsInfo = messageCollection
                    .whereEqualTo(MessageCollection.SenderUserId, childUserId2)
                    .get()
                    .await()
                    .documents
                    .map { doc ->
                        gson.fromJson(
                            doc.get(MessageCollection.PayLoad) as String,
                            ChildSmsInfo::class.java
                        ).apply {
                            childUserId = childUserId2
                            idInServerDb = doc.id
                        }
                    }

                childSmsInfoList.addAll(childSmsInfo)
            }
            return Result.Success(childSmsInfoList)
        } catch (ex: Exception) {
            return Result.Failure(ex)
        }
    }

    override suspend fun notifyDidSaveFetchedMessages(
        childSmsInfoList: List<ChildSmsInfo>
    ): Result {
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
                Log.d(TAG, "notifyDidSaveFetchedMessages: " +
                            "Failed ${failedMessageIds.size}/${childSmsInfoList.size}. "
                )
            }
            Result.Success()
        } catch (ex: Exception) {
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
            Firebase.firestore.runTransaction { tx ->
                runBlocking {
                    removeCurrentUserFromFetcherListHelper(childSmsInfo, tx)
                }
            }.await()
            Log.d(TAG, "updateEntriesInDatabase: Finished")
            true
        } catch (ex: Exception) {
            Log.d(TAG, "updateEntriesInDatabase: Failed with $ex")
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
    override suspend fun pushMessage(message: Message): Result {

        if (currentUser.isUserSignedIn().not()) {
            return Result.Failure(UserSignedOutException("User is not signed in."))
        }

        val userId = currentUser.user.id
        // Write a message to the database
        // TODO: Nikesh - Also, encrypt the messages before sending it with a key from the user
        val newMessage = hashMapOf(
            MessageCollection.PayLoad to gson.toJson(message),
            MessageCollection.SenderUserId to userId
        )

        return try {
            val messageIdInServer = messageCollection.add(newMessage).await().id
            // Store all the parent Ids as fetcherUserIds that needs to fetch this message
            // before it could be deleted from the database
            messageFetcherCollection.add(hashMapOf(
                MessageFetcher.MessageId to messageIdInServer,
                MessageFetcher.FetcherUserIds to gson.toJson(currentUser.user.parentUserIds)
            ))
            Result.Success()
        } catch (ex: Exception) {
            // TODO: Nikesh - Log this error in Firebase
            // TODO: Nikesh - Make a logger class
            Log.d(TAG, "pushMessageToServer: $ex")
            Result.Failure(ex)
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
    }

    /**
     * Represent fields of Message collection
     */
    object MessageCollection {
        const val SenderUserId = "SenderUserId"
        const val PayLoad = "PayLoad"
    }

    /**
     * Represent fields of User collection
     */
    object User {
        const val Name = "Name"
        const val Phone = "Phone"
        const val DeviceId = "DeviceId"
        const val PushNotificationToken = "PushNotificationToken"
        const val Email = "Email"
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
}

/**
 * Exception thrown when email address is not registered already
 */
class EmailNotFoundException(message: String): Exception(message)

/**
 * Exception thrown when email address is already used
 */
class EmailAlreadyExistException(message: String): Exception(message)

/**
 * Exception thrown when user is not signed in
 */
class UserSignedOutException(message: String): Exception(message)