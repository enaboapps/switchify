package com.enaboapps.switchify.backend.data

import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirestoreManager provides a simplified interface for interacting with Firestore.
 */
class FirestoreManager {
    private val firestoreDb = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreManager"

        @Volatile
        private var instance: FirestoreManager? = null

        fun getInstance(): FirestoreManager {
            return instance ?: synchronized(this) {
                instance ?: FirestoreManager().also { instance = it }
            }
        }
    }

    /**
     * Saves data to a Firestore document.
     */
    suspend fun saveDocument(
        path: String,
        data: Map<String, Any>
    ) = withContext(Dispatchers.IO) {
        try {
            val docRef = firestoreDb.document(path)
            val options = SetOptions.merge()
            docRef.set(data, options).await()
            Log.i(TAG, "Document saved successfully: $path")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving document: $path", e)
            throw e
        }
    }

    /**
     * Retrieves data from a Firestore document.
     */
    suspend fun getDocument(
        path: String
    ): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val docRef = firestoreDb.document(path)
            val snapshot = docRef.get().await()
            return@withContext if (snapshot.exists()) snapshot.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving document: $path", e)
            throw e
        }
    }

    /**
     * Updates specific fields in a Firestore document.
     */
    suspend fun updateDocument(
        path: String,
        updates: Map<String, Any>
    ) = withContext(Dispatchers.IO) {
        try {
            val docRef = firestoreDb.document(path)
            docRef.update(updates).await()
            Log.i(TAG, "Document updated successfully: $path")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating document: $path", e)
            throw e
        }
    }

    /**
     * Deletes a Firestore document.
     */
    suspend fun deleteDocument(
        path: String
    ) = withContext(Dispatchers.IO) {
        try {
            val docRef = firestoreDb.document(path)
            docRef.delete().await()
            Log.i(TAG, "Document deleted successfully: $path")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting document: $path", e)
            throw e
        }
    }

    /**
     * Sets up a listener for changes to a Firestore document.
     */
    fun listenToDocument(
        path: String,
        onDocument: (Map<String, Any>?) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): ListenerRegistration {
        val docRef = firestoreDb.document(path)
        return docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error listening to document: $path", e)
                onError?.invoke(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                onDocument(snapshot.data)
            } else {
                onDocument(null)
            }
        }
    }

    /**
     * Queries documents in a Firestore collection.
     */
    suspend fun queryDocuments(
        collectionPath: String,
        queries: List<QueryFilter> = emptyList(),
        orderBy: List<OrderBy> = emptyList(),
        limit: Int? = null
    ): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            var query: Query = firestoreDb.collection(collectionPath)

            // Apply all query filters
            queries.forEach { filter ->
                query = when (filter) {
                    is QueryFilter.WhereEqual -> query.whereEqualTo(filter.field, filter.value)
                    is QueryFilter.WhereGreaterThan -> query.whereGreaterThan(
                        filter.field,
                        filter.value
                    )

                    is QueryFilter.WhereLessThan -> query.whereLessThan(filter.field, filter.value)
                    is QueryFilter.WhereGreaterThanOrEqual -> query.whereGreaterThanOrEqualTo(
                        filter.field,
                        filter.value
                    )

                    is QueryFilter.WhereLessThanOrEqual -> query.whereLessThanOrEqualTo(
                        filter.field,
                        filter.value
                    )

                    is QueryFilter.WhereArrayContains -> query.whereArrayContains(
                        filter.field,
                        filter.value
                    )

                    is QueryFilter.WhereIn -> query.whereIn(filter.field, filter.values)
                    is QueryFilter.WhereArrayContainsAny -> query.whereArrayContainsAny(
                        filter.field,
                        filter.values
                    )
                }
            }

            // Apply ordering
            orderBy.forEach { order ->
                query = when (order) {
                    is OrderBy.Ascending -> query.orderBy(order.field)
                    is OrderBy.Descending -> query.orderBy(order.field, Query.Direction.DESCENDING)
                }
            }

            // Apply limit if specified
            if (limit != null) {
                query = query.limit(limit.toLong())
            }

            val snapshot = query.get().await()
            return@withContext snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying documents in: $collectionPath", e)
            throw e
        }
    }
}

sealed class QueryFilter {
    data class WhereEqual(val field: String, val value: Any) : QueryFilter()
    data class WhereGreaterThan(val field: String, val value: Any) : QueryFilter()
    data class WhereLessThan(val field: String, val value: Any) : QueryFilter()
    data class WhereGreaterThanOrEqual(val field: String, val value: Any) : QueryFilter()
    data class WhereLessThanOrEqual(val field: String, val value: Any) : QueryFilter()
    data class WhereArrayContains(val field: String, val value: Any) : QueryFilter()
    data class WhereIn(val field: String, val values: List<Any>) : QueryFilter()
    data class WhereArrayContainsAny(val field: String, val values: List<Any>) : QueryFilter()
}

sealed class OrderBy {
    data class Ascending(val field: String) : OrderBy()
    data class Descending(val field: String) : OrderBy()
}