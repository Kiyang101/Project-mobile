package com.example.project

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

data class Cart(
    val email: String = "",
    val cartItems: List<CartItem> = emptyList()
)

data class CartItem(
    val product: Product = Product(),
    val quantity: Int = 0,
    val size: String = ""
)

data class History(
    val orderId: Int = 0,
    val email: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val date: Date = Date()
)

class FirestoreCartDataSource {
    private val collection = Firebase.firestore.collection("cart")

    /**
     * Adds an item to the cart. If the cart doesn't exist for the email, it creates one.
     * If the item (product + size) already exists, it increments the quantity.
     */
    suspend fun addItemToCart(email: String, newItem: CartItem) {
        val docRef = collection.document(email)
        val snapshot = docRef.get().await()
        
        if (snapshot.exists()) {
            val cart = snapshot.toObject(Cart::class.java)
            cart?.let {
                var itemExists = false
                val updatedItems = it.cartItems.map { item ->
                    if (item.product.productId == newItem.product.productId && item.size == newItem.size) {
                        itemExists = true
                        item.copy(quantity = item.quantity + newItem.quantity)
                    } else {
                        item
                    }
                }.toMutableList()

                if (!itemExists) {
                    updatedItems.add(newItem)
                }
                docRef.update("cartItems", updatedItems).await()
            }
        } else {
            // Create a new cart if it doesn't exist
            val newCart = Cart(email = email, cartItems = listOf(newItem))
            docRef.set(newCart).await()
        }
    }

    suspend fun updateProductQuantity(email: String, productId: Int, newQuantity: Int) {
        val docRef = collection.document(email)
        val snapshot = docRef.get().await()
        val cart = snapshot.toObject(Cart::class.java)

        cart?.let {
            val updatedItems = it.cartItems.map { item ->
                if (item.product.productId == productId) {
                    // Update only the specific product's quantity
                    item.copy(quantity = newQuantity)
                } else {
                    item
                }
            }
            docRef.update("cartItems", updatedItems).await()
        }
    }

    // If you want to update the entire cart object at once
    suspend fun update(cart: Cart) {
        collection.document(cart.email).set(cart).await()
    }

    suspend fun getCart(email: String): Cart? {
        return try {
            val snapshot = collection.document(email).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(Cart::class.java)
            } else {
                // Return an empty cart if document doesn't exist
                Cart(email = email, cartItems = emptyList())
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteItemInCart(email: String, productId: Int) {
        val docRef = collection.document(email)
        val snapshot = docRef.get().await()
        val cart = snapshot.toObject(Cart::class.java)

        cart?.let {
            val updatedItems = it.cartItems.filter { item ->
                item.product.productId != productId
            }
            docRef.update("cartItems", updatedItems).await()
        }
    }

    suspend fun clearCart(email: String) {
        collection.document(email).delete().await()
    }
}

class FirestoreHistoryDataSource {
    private val collection = Firebase.firestore.collection("history")

    suspend fun addHistory(history: History) {
        try {
            collection.add(history).await()
        } catch (e: Exception) {
            // Log or handle error
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
            emptyList()
        }
    }
}
