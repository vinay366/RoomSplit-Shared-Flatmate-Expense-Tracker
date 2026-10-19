package com.example.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

object FirestoreErrorHandler {
    private const val TAG = "FirestoreErrorHandler"

    fun handle(e: Throwable, userFriendlyAction: String): String {
        Log.e(TAG, "Error during $userFriendlyAction", e)
        return when (e) {
            is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        "Permission denied. Make sure you are signed in and a member of this room."
                    FirebaseFirestoreException.Code.NOT_FOUND ->
                        "Requested room or data was not found."
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        "Service temporarily unavailable. Please check your internet connection."
                    else -> e.localizedMessage ?: "Firestore error: ${e.code}"
                }
            }
            else -> e.localizedMessage ?: "An unexpected error occurred."
        }
    }
}
