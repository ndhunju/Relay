package com.ndhunju.relay.service

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.ndhunju.relay.ui.messages.Message
import com.ndhunju.relay.util.CurrentUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides API to store and read data from Cloud Database.
 * We could extract Interface out of this class if need be.
 */
@Singleton
class CloudDatabaseService @Inject constructor(
    private val gson: Gson,
    private val currentUser: CurrentUser
) {

    private val TAG = CloudDatabaseService::class.simpleName

    private var userId: String?
        get() {
           return currentUser.userId
        }
        set(value) {
            currentUser.userId = value
        }

    /**
     * Creates user in the cloud database.
     * TODO: Nikesh - Use third party Identity Provider to authenticate and create user
     */
    fun createUser(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        deviceId: String? = null,
        pushNotificationToken: String? = null,
        onUserCreateCallback: (() -> Unit)? = null
    ) {
        val database = Firebase.firestore
        val userCollection = database.collection("User")
        val newUser = hashMapOf(
            "Name" to name,
            "Email" to email,
            "Phone" to phone,
            "DeviceId" to deviceId,
            "PushNotificationToken" to pushNotificationToken
        )

        userCollection.add(newUser)
            .addOnSuccessListener { documentRef ->
                val docId = documentRef.id
                userId = docId
                onUserCreateCallback?.invoke()
            }
            .addOnFailureListener {ex ->
                Log.d(TAG, "createUser: Failed with $ex")
            }

    }

    /**
     * Pushes [message] to the cloud database
     */
    fun pushMessage(message: Message): StateFlow<Result> {

        val result = MutableStateFlow<Result>(Result.Pending)

        if (userId == null) {
            createUser {
                pushMessage(message)
            }
        }

        val userId = userId

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
                result.value = Result.Success(true)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "pushMessageToServer: $exception")
                // TODO: Nikesh - Log this error in Firebase
                result.value = Result.Failure(exception.localizedMessage ?: "")
            }

        return result
    }

    /**
     * WIP
     * Fetches message for meant for [currentUser]
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
    data class Success(val data: Any): Result()
    data class Failure(val message: String): Result()
}