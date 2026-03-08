package com.example.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException

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

data class Favorite(
    val email: String = "",
    val favoriteProducts: List<Product> = emptyList()
)

class FirestoreCartDataSource {
    private val collection = Firebase.firestore.collection("cart")

    /**
     * Adds an item to the cart. If the cart doesn't exist for the email, it creates one.
     * If the item (product + size) already exists, it increments the quantity.
     */
    suspend fun addItemToCart(email: String, newItem: CartItem) {
        try {
            val docRef = collection.document(email)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                val cart = snapshot.toObject(Cart::class.java)

                if (cart != null) {
                    var itemExists = false
                    val updatedItems = cart.cartItems.map { item ->
                        // Use productId for comparison to avoid object equality issues
                        if (item.product.productId == newItem.product.productId && item.size == newItem.size) {
                            itemExists = true
                            item.copy(quantity = item.quantity + newItem.quantity)
//                            updateProductQuantity(email, newItem.product.productId, newItem.size, item.quantity + newItem.quantity)
                        } else {
                            item
                        }
                    }.toMutableList()

                    if (!itemExists) {
                        updatedItems.add(newItem)
                    }

                    Log.d("FirestoreCart", "Updating cart for $email with ${updatedItems.size} items")
                    docRef.set(cart.copy(cartItems = updatedItems)).await()
                    Log.d("FirestoreCart", "Successfully updated existing cart!")
                } else {
                    Log.e("FirestoreCart", "Failed to deserialize Cart object for $email even though document exists")
                    // Fallback: overwrite with new cart if deserialization fails to prevent getting stuck
                    val newCart = Cart(email = email, cartItems = listOf(newItem))
                    docRef.set(newCart).await()
                }
            } else {
                val newCart = Cart(email = email, cartItems = listOf(newItem))
                docRef.set(newCart).await()
                Log.d("FirestoreCart", "Successfully created new cart!")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreCart", "Error adding item: ${e.message}", e)
        }
    }

    suspend fun updateProductQuantity(email: String, productId: Int, size: String, newQuantity: Int) {
        try {
            val docRef = collection.document(email)
            val snapshot = docRef.get().await()
            val cart = snapshot.toObject(Cart::class.java)

            cart?.let {
                val updatedItems = it.cartItems.map { item ->
                    if (item.product.productId == productId && item.size == size) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
                docRef.set(it.copy(cartItems = updatedItems)).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreCart", "Error updating quantity", e)
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
                Cart(email = email, cartItems = emptyList())
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getCartFlow(email: String): Flow<Cart?> = callbackFlow {
        val docRef = collection.document(email)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreCart", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val cart = snapshot.toObject(Cart::class.java)
                    trySend(cart)
                } catch (e: Exception) {
                    Log.e("FirestoreCart", "Error in getCartFlow: ${e.message}", e)
                }
            } else {
                trySend(Cart(email = email, cartItems = emptyList()))
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteItemInCart(email: String, productId: Int, size: String) {
        try {
            val docRef = collection.document(email)
            val snapshot = docRef.get().await()
            val cart = snapshot.toObject(Cart::class.java)

            cart?.let {
                val updatedItems = it.cartItems.filter { item ->
                    !(item.product.productId == productId && item.size == size)
                }
                docRef.set(it.copy(cartItems = updatedItems)).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreCart", "Error deleting item", e)
        }
    }

    suspend fun clearCart(email: String) {
        collection.document(email).delete().await()
    }
}

class CartViewModel : ViewModel() {
    private val repository = FirestoreCartDataSource()
    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    fun observeCart(email: String) {
        viewModelScope.launch {
            repository.getCartFlow(email).collect { cart ->
                _cartCount.value = cart?.cartItems?.sumOf { it.quantity } ?: 0
            }
        }
    }

    fun addToCart(email: String, product: Product, quantity: Int, size: String) {
        viewModelScope.launch {
            repository.addItemToCart(email, CartItem(product, quantity, size))
        }
    }

    fun updateQuantity(email: String, productId: Int, size: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateProductQuantity(email, productId, size, newQuantity)
        }
    }

    fun deleteItem(email: String, productId: Int, size: String) {
        viewModelScope.launch {
            repository.deleteItemInCart(email, productId, size)
        }
    }
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
            emptyList()
        }
    }
}

class FirestoreFavoriteDataSource {
    private val collection = Firebase.firestore.collection("favorites")

    suspend fun getFavorite(email: String): Favorite? {
        return try {
            val snapshot = collection.document(email).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(Favorite::class.java)
            } else {
                Favorite(email = email, favoriteProducts = emptyList())
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addFavorite(email: String, product: Product) {
        val docRef = collection.document(email)
        val snapshot = docRef.get().await()

        if (snapshot.exists()) {
            val favorite = snapshot.toObject(Favorite::class.java)
            favorite?.let {
                val alreadyExists = it.favoriteProducts.any { p -> p.productId == product.productId }
                if (!alreadyExists) {
                    val updatedProducts = it.favoriteProducts.toMutableList()
                    updatedProducts.add(product)
                    docRef.update("favoriteProducts", updatedProducts).await()
                }
            }
        } else {
            val newFavorite = Favorite(email = email, favoriteProducts = listOf(product))
            docRef.set(newFavorite).await()
        }
    }

    suspend fun removeFavorite(email: String, productId: Int) {
        val docRef = collection.document(email)
        val snapshot = docRef.get().await()
        val favorite = snapshot.toObject(Favorite::class.java)

        favorite?.let {
            val updatedProducts = it.favoriteProducts.filter { p ->
                p.productId != productId
            }
            docRef.update("favoriteProducts", updatedProducts).await()
        }
    }
}
