package com.ndhunju.relay.api

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

private val TAG = ApiInterfaceFireStoreImpl::class.simpleName

/**
 * Implements [ApiInterface] using [Firebase.firestore]
 */
class ApiInterfaceFireStoreImpl(
    private val gson: Gson,
    private val currentUser: CurrentUser
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
    private val userCollectionRef = Firebase.firestore.collection("User")
    private val parentChildCollectionRef = Firebase.firestore.collection("ParentChild")
    private val messageCollectionRef = Firebase.firestore.collection("Message")
    private val localIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        val userWithEmailExits = userCollectionRef
            .whereEqualTo("Email", email)
            .get().await().documents.isNotEmpty()

        if (userWithEmailExits) {
            return Result.Failure(
                EmailAlreadyExistException("User with email $email already exits.")
            )
        }

        return try {
            userId = userCollectionRef.add(newUser).await().id
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
        val finalName = name ?: CurrentUser.user.name
        val finalPhone = phone ?: CurrentUser.user.phone

        return try {
            userCollectionRef.document(userId).update(
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
        "Name" to name,
        "Email" to email,
        "Phone" to phone,
        "DeviceId" to deviceId,
        "PushNotificationToken" to pushNotificationToken
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
            val queryByEmail = userCollectionRef.whereEqualTo("Email", parentEmailAddress)
            val querySnapshot = queryByEmail.get().await()
            if (querySnapshot.isEmpty) {
                return Result.Failure(EmailNotFoundException("Parent email id not found"))
            }
            val parentUserId = querySnapshot.documents.first().id

            // Register this pairing in the database
            parentChildCollectionRef.add(hashMapOf(
                "ChildUserId" to childUserId,
                "ParentUserId" to parentUserId
            )).await()

            Result.Success(parentUserId)
        } catch (ex: Exception) {
            Result.Failure(ex)
        }

    }

    override suspend fun fetchChildUsers(parentUserId: String): Result {
        return try {
            // Fetch the list of child user ids for passed parent
            val childUserIds = parentChildCollectionRef
                .whereEqualTo("ParentUserId", parentUserId)
                .get()
                .await()
                .documents.map { it.get("ChildUserId") as String }
                .distinct() // Filter out duplicates
                .filter { it.isNotEmpty() } // Filter out empty strings

            // Fetch the email of each child user
            val childList = childUserIds.map { childUserId ->
                val email = userCollectionRef
                    .whereEqualTo(FieldPath.documentId(), childUserId)
                    .get()
                    .await()
                    .documents.first()
                    .get("Email") as String
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
                val childSmsInfo = messageCollectionRef
                    .whereEqualTo("SenderUserId", childUserId2)
                    .get()
                    .await()
                    .documents
                    .map {
                        gson.fromJson(
                            it.get("PayLoad") as String,
                            ChildSmsInfo::class.java
                        ).apply { childUserId = childUserId2 }
                    }

                childSmsInfoList.addAll(childSmsInfo)
            }
            return Result.Success(childSmsInfoList)
        } catch (ex: Exception) {
            return Result.Failure(ex)
        }
    }

    /**
     * Pushes [message] to the cloud database.
     */
    override suspend fun pushMessage(message: Message): Result {

        if (CurrentUser.isUserSignedIn().not()) {
            return Result.Failure(UserSignedOutException("User is not signed in."))
        }

        val userId = CurrentUser.user.id
        // Write a message to the database
        val newMessage = hashMapOf(
            "PayLoad" to gson.toJson(message),
            "SenderUserId" to userId
        )

        return try {
            messageCollectionRef.add(newMessage).await().id
            Log.d(TAG, "pushMessageToServer: is successful")
            Result.Success()
        } catch (ex: Exception) {
            // TODO: Nikesh - Log this error in Firebase
            // TODO: Nikesh - Make a logger class
            Log.d(TAG, "pushMessageToServer: $ex")
            Result.Failure(ex)
        }
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