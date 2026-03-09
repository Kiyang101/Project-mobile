package com.example.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class History(
    val orderId: String = "",
    val email: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val date: Date = Date()
)

fun generateOrderId(): String {
    // 1. Get current date in YYYYMMDD format
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val dateString = dateFormat.format(Date())

    // 2. Generate a 4-digit random number (1000 to 9999)
    val randomNum = (1000..9999).random()

    // 3. Combine them
    return "ORD-$dateString-$randomNum"
}

class FirestoreHistoryDataSource {
    private val collection = Firebase.firestore.collection("history")

    suspend fun addHistory(history: History) {
        try {
            collection.add(history).await()
        } catch (e: Exception) {
            Log.e("FirestoreHistoryDataSource", "Error adding history to Firestore", e)
        }
    }

    suspend fun getHistory(email: String): List<History> {
        return try {
            val snapshot = collection
                .whereEqualTo("email", email)
                .get()
                .await()
            snapshot.toObjects(History::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreHistoryDataSource", "Error getting history", e)
            emptyList()
        }
    }
}

class HistoryRepository(
    private val dataSource: FirestoreHistoryDataSource = FirestoreHistoryDataSource()
) {
    suspend fun addHistory(history: History) = dataSource.addHistory(history)
    suspend fun getHistory(email: String) = dataSource.getHistory(email)
}

class HistoryViewModel(
    private val repository: HistoryRepository = HistoryRepository()
) : ViewModel() {
    private val _historyList = MutableStateFlow<List<History>>(emptyList())
    val historyList: StateFlow<List<History>> = _historyList.asStateFlow()

    fun loadHistory(email: String) {
        viewModelScope.launch {
            val history = repository.getHistory(email)
            _historyList.value = history
        }
    }

    fun addHistory(history: History) {
        viewModelScope.launch {
            repository.addHistory(history)
            loadHistory(history.email)
        }
    }
}