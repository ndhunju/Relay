package com.ndhunju.relay.service

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts the API that allows interaction with some kind of BE server
 */
interface ApiInterface {

    /**
     * Makes API request to create user.
     */
    fun createUser(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        deviceId: String? = null,
        pushNotificationToken: String? = null,
    ): StateFlow<Result>

    /**
     * Makes API requests to update the user. If null is passed, don't update that value.
     * Only the non-null value must be updated in the server
     */
    fun updateUser(name: String? = null, phone: String? = null, ): StateFlow<Result>


    /**
     * Makes API request to pair [childUserId] with a
     * parent user whose email is [parentEmailAddress]
     */
    fun pairWithParent(childUserId: String, parentEmailAddress: String): Flow<Result>

    /**
     * Pushes [message] to the server.
     */
    fun pushMessage(message: Message): StateFlow<Result>

}

private val TAG = ApiInterfaceFireStoreImpl::class.simpleName

/**
 * Implements [ApiInterface] using [Firebase.firestore]
 */
class ApiInterfaceFireStoreImpl(
    private val gson: Gson,
    private val currentUser: CurrentUser
) : ApiInterface {

    private var userId: String?
        get() {
           return currentUser.user.id
        }
        set(value) {
            currentUser.user = currentUser.user.copy(id = value)
        }

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
        getUserCollection().add(newUser)
            .addOnSuccessListener { documentRef ->
                val docId = documentRef.id
                userId = docId //
                flow.value = Result.Success(userId)
            }
            .addOnFailureListener {ex ->
                Log.d(TAG, "createUser: Failed with $ex")
                flow.value = Result.Failure(ex.message)
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
        val userId = currentUser.user.id ?: return stateFlow.apply {
            value = Result.Failure("User Id is null")
        }

        // If null is passed, use existing values
        val finalName = name ?: currentUser.user.name
        val finalPhone = phone ?: currentUser.user.phone

        getUserCollection().document(userId).update(
            makeMapForUserCollection(name = finalName, phone = finalPhone).toMap()
        ).addOnSuccessListener {
            stateFlow.value = Result.Success()
        }.addOnFailureListener {
            Log.d(TAG, "updateUser: Failed to update user")
            stateFlow.value = Result.Failure(it.message)
        }

        return stateFlow
    }

    /**
     * Returns [CollectionReference] for "User" collection
     */
    private fun getUserCollection(): CollectionReference {
        val database = Firebase.firestore
        return database.collection("User")
    }

    private fun getParentChildCollection(): CollectionReference {
        return Firebase.firestore.collection("ParentChild")
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
        return flow
    }

    /**
     * Pushes [message] to the cloud database.
     */
    override fun pushMessage(
        message: Message
    ): StateFlow<Result> {

        val stateFlow = MutableStateFlow<Result>(Result.Pending)

        if (currentUser.isUserSignedIn().not()) {
            stateFlow.value = Result.Failure("User is not signed in.")
            return stateFlow
        }

        val userId = currentUser.user.id ?: return stateFlow.apply {
            value = Result.Failure("User Id is null")
        }

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
                stateFlow.value = Result.Failure(exception.localizedMessage ?: "")
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
 * Encapsulates different results that an async fun can have.
 */
sealed class Result {
    data object Pending: Result()
    data class Success(val data: Any? = null): Result()
    data class Failure(val message: String? = null): Result()
}