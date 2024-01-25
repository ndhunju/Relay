package com.ndhunju.relay.api

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.ui.parent.Child
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
           return CurrentUser.user.id
        }
        set(value) {
            CurrentUser.user = CurrentUser.user.copy(id = value)
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
    override fun createUser(
        name: String?,
        email: String?,
        phone: String?,
        deviceId: String?,
        pushNotificationToken: String?,
    ): StateFlow<Result> {
        val flow = MutableStateFlow<Result>(Result.Pending)
        val newUser = makeMapForUserCollection(name, email, phone, deviceId, pushNotificationToken)

        // TODO: Nikesh - Check for duplicate user email before creating new account
        userCollectionRef.add(newUser)
            .addOnSuccessListener { documentRef ->
                val docId = documentRef.id
                userId = docId //
                flow.value = Result.Success(userId)
            }
            .addOnFailureListener {ex ->
                Log.d(TAG, "createUser: Failed with $ex")
                flow.value = Result.Failure(ex)
            }

        return flow
    }

    /**
     * Updates user in cloud database
     */
    override fun updateUser(
        name: String?,
        phone: String?,
    ): StateFlow<Result> {
        val stateFlow = MutableStateFlow<Result>(Result.Pending)
        val userId = CurrentUser.user.id

        // If null is passed, use existing values
        val finalName = name ?: CurrentUser.user.name
        val finalPhone = phone ?: CurrentUser.user.phone

        userCollectionRef.document(userId).update(
            makeMapForUserCollection(name = finalName, phone = finalPhone).toMap()
        ).addOnSuccessListener {
            stateFlow.value = Result.Success()
        }.addOnFailureListener {
            Log.d(TAG, "updateUser: Failed to update user")
            stateFlow.value = Result.Failure(it)
        }

        return stateFlow
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

    override fun pairWithParent(childUserId: String, parentEmailAddress: String): Flow<Result> {
        val flow = MutableStateFlow<Result>(Result.Pending)
        // TODO: Nikesh - check if user has already paired with 3 parents
        // Check that such parent email address already exists
        val queryByEmail = userCollectionRef.whereEqualTo("Email", parentEmailAddress)
        queryByEmail.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                flow.value = Result.Failure(EmailNotFoundException("Parent email id not found"))
                return@addOnSuccessListener
            }

            val parentUserId = querySnapshot.documents.first().id

            // Register this pairing in the database
            parentChildCollectionRef.add(hashMapOf(
                "ChildUserId" to childUserId,
                "ParentUserId" to parentUserId
            )).addOnSuccessListener {
                // Pass parent user's id back
                flow.value = Result.Success(parentUserId)
            }.addOnFailureListener { ex ->
                //Log.d(TAG, "pairWithParent: $ex")
                flow.value = Result.Failure(ex)
            }
        }

        return flow
    }

    override fun fetchChildUsers(parentUserId: String): Flow<Result> {
        val flow = MutableStateFlow<Result>(Result.Pending)
        localIoScope.launch {
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

            flow.value = Result.Success(childList)
        }

        return flow
    }

    override fun fetchMessagesFromChildUsers(childUserIds: List<String>): Flow<Result> {
        val flow = MutableStateFlow<Result>(Result.Pending)
        localIoScope.launch {
            val childUserIdToMessages = mutableMapOf<String, List<Message>>()
            try {
                for (childUserId in childUserIds) {
                    val messages = messageCollectionRef
                        .whereEqualTo("SenderUserId", childUserId)
                        .get()
                        .await()
                        .documents
                        .map { gson.fromJson(it.get("PayLoad") as String, Message::class.java) }

                    childUserIdToMessages[childUserId] = messages
                }
                flow.value = Result.Success(childUserIdToMessages)
            } catch (ex: Exception) {
                flow.value = Result.Failure(ex)
            }
        }

        return flow
    }

    /**
     * Pushes [message] to the cloud database.
     */
    override fun pushMessage(
        message: Message
    ): StateFlow<Result> {

        val stateFlow = MutableStateFlow<Result>(Result.Pending)

        if (CurrentUser.isUserSignedIn().not()) {
            stateFlow.value = Result.Failure(UserSignedOutException("User is not signed in."))
            return stateFlow
        }

        val userId = CurrentUser.user.id

        // Write a message to the database
        val database = Firebase.firestore
        val messageCollection = database.collection("Message")

        val newMessage = hashMapOf(
            "PayLoad" to gson.toJson(message),
            "SenderUserId" to userId
        )

        messageCollection.add(newMessage)
            .addOnSuccessListener {
                Log.d(TAG, "pushMessageToServer: is successful")
                stateFlow.value = Result.Success()
            }
            .addOnFailureListener { exception ->
                // TODO: Nikesh - Log this error in Firebase
                // TODO: Nikesh - Make a logger class
                Log.d(TAG, "pushMessageToServer: $exception")
                stateFlow.value = Result.Failure(exception)
            }

        return stateFlow
    }

    /**
     * WIP
     * Fetches messages meant for [currentUser]
     */
//    fun fetchMessages() {
//        val database = Firebase.firestore
//        val messageCollection = database.collection("Message")
//
//        val exitingEntry = database.collection("Message ")
//        exitingEntry.get().addOnSuccessListener { result ->
//            for (document in result) {
//                Log.d("888", "pushMessageToServer: $document")
//            }
//        }.addOnFailureListener { exception ->
//            Log.d("888", "pushMessageToServer: $exception")
//        }
//    }
}

/**
 * Exception thrown when email address is not registered already
 */
class EmailNotFoundException(message: String): Exception(message)

/**
 * Exception thrown when user is not signed in
 */
class UserSignedOutException(message: String): Exception(message)