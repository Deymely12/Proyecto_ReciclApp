package com.example.reciclapp.screens.ranking

import com.example.reciclapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.jvm.java

class RankingRepository(
    private val bd: FirebaseFirestore
) {
    fun getRankingFlow(): Flow<List<User>> = callbackFlow {

        val subscription = bd.collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (snapshot != null) {
                    val list = snapshot.toObjects(User::class.java)
                    trySend(list)
                }
            }

        awaitClose {
            subscription.remove()
        }
    }


}