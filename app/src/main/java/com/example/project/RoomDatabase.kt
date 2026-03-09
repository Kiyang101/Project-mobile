package com.example.project

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val productId: Int = 0,
    val quantity: Int = 0,
    val size: String = ""
)

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val cartItems: List<CartItem>,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val email: String = "",
    val favoriteProducts: List<Int> = emptyList()
)

class Converters {
    @TypeConverter
    fun fromCartItemList(value: List<CartItem>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toCartItemList(value: String): List<CartItem> {
        val listType = object : TypeToken<List<CartItem>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cart: CartEntity)

    @Query("SELECT * FROM cart WHERE email = :email")
    suspend fun getCartByEmail(email: String): CartEntity?

    @Query("DELETE FROM cart WHERE email = :email")
    suspend fun deleteCartByEmail(email: String)
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE email = :email")
    suspend fun getFavoriteByEmail(email: String): FavoriteEntity?

    @Query("DELETE FROM favorites WHERE email = :email")
    suspend fun deleteFavoriteByEmail(email: String)
}

@Database(
    entities = [CartEntity::class, FavoriteEntity::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?:synchronized(lock=this){
                Room.databaseBuilder(
                    context.applicationContext,
                    klass = AppDatabase::class.java,
                    name="DB"
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}

class CartRepository(private val dao: CartDao) {
    suspend fun insert(cart: CartEntity) {
        dao.insert(cart)
    }

    suspend fun getCartByEmail(email: String) = dao.getCartByEmail(email)

    suspend fun deleteCartByEmail(email: String) = dao.deleteCartByEmail(email)
}

class FavoriteRepository(private val dao: FavoriteDao) {
    suspend fun insert(favorite: FavoriteEntity) {
        dao.insert(favorite)
    }

    suspend fun getFavoriteByEmail(email: String) = dao.getFavoriteByEmail(email)

    suspend fun deleteFavoriteByEmail(email: String) = dao.deleteFavoriteByEmail(email)
}

class CartViewModel(
    private val repository: CartRepository
): ViewModel() {

    private val _cart = MutableStateFlow<CartEntity?>(null)
    val cart: StateFlow<CartEntity?> = _cart.asStateFlow()

    val cartCount: StateFlow<Int> = _cart.map { cart ->
        cart?.cartItems?.sumOf { it.quantity } ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun insertCart(email: String, cartItems: List<CartItem>) {
        viewModelScope.launch {
            val existingCart = repository.getCartByEmail(email)
            val updatedCart = if (existingCart != null) {
                val updatedItems = existingCart.cartItems.toMutableList()
                cartItems.forEach { newItem ->
                    val index = updatedItems.indexOfFirst { it.productId == newItem.productId && it.size == newItem.size }
                    if (index != -1) {
                        val existingItem = updatedItems[index]
                        updatedItems[index] = existingItem.copy(quantity = existingItem.quantity + newItem.quantity)
                    } else {
                        updatedItems.add(newItem)
                    }
                }
                existingCart.copy(cartItems = updatedItems)
            } else {
                CartEntity(
                    email = email,
                    cartItems = cartItems
                )
            }
            repository.insert(updatedCart)
            Log.d("CartDebug", "Cart updated for $email: $updatedCart")
            loadCart(email)
        }
    }

    fun updateQuantity(email: String, productId: Int, size: String, newQuantity: Int) {
        viewModelScope.launch {
            val existingCart = repository.getCartByEmail(email)
            existingCart?.let { cart ->
                val updatedItems = cart.cartItems.map { item ->
                    if (item.productId == productId && item.size == size) {
                        item.copy(quantity = newQuantity)
                    } else {
                        item
                    }
                }
                repository.insert(cart.copy(cartItems = updatedItems))
                loadCart(email)
            }
        }
    }

    fun deleteItem(email: String, productId: Int, size: String) {
        viewModelScope.launch {
            val existingCart = repository.getCartByEmail(email)
            existingCart?.let { cart ->
                val updatedItems = cart.cartItems.filterNot { item ->
                    item.productId == productId && item.size == size
                }
                repository.insert(cart.copy(cartItems = updatedItems))
                loadCart(email)
            }
        }
    }

    fun loadCart(email: String) {
        viewModelScope.launch {
            val cart = repository.getCartByEmail(email)
            _cart.value = cart
            Log.d("CartDebug", "Cart loaded for $email: $cart")
        }
    }

    fun clearCart(email: String) {
        viewModelScope.launch {
            repository.deleteCartByEmail(email)
            _cart.value = null
            Log.d("CartDebug", "Cart cleared for $email")
        }
    }

    fun clearCartState() {
        _cart.value = null
    }

    suspend fun getCartByEmail(email: String) = repository.getCartByEmail(email)
}

class FavoriteViewModel(
    private val repository: FavoriteRepository
): ViewModel() {

    private val _favorites = MutableStateFlow<FavoriteEntity?>(null)
    val favorites: StateFlow<FavoriteEntity?> = _favorites.asStateFlow()

    fun toggleFavorite(email: String, productId: Int) {
        viewModelScope.launch {
            val currentFavorite = repository.getFavoriteByEmail(email)
            val updatedProducts = if (currentFavorite != null) {
                val list = currentFavorite.favoriteProducts.toMutableList()
                if (list.contains(productId)) {
                    list.remove(productId)
                } else {
                    list.add(productId)
                }
                list
            } else {
                listOf(productId)
            }
            val newFavorite = FavoriteEntity(email = email, favoriteProducts = updatedProducts)
            repository.insert(newFavorite)
            _favorites.value = newFavorite
        }
    }

    fun loadFavorites(email: String) {
        viewModelScope.launch {
            val favorite = repository.getFavoriteByEmail(email)
            _favorites.value = favorite
        }
    }

    fun isFavorite(productId: Int): Boolean {
        return _favorites.value?.favoriteProducts?.contains(productId) ?: false
    }
}

class AppViewModelFactory(
    context: Context
): ViewModelProvider.Factory{
    private val database = AppDatabase.getDatabase(context)
    private val cartDao = database.cartDao()
    private val favoriteDao = database.favoriteDao()
    
    private val cartRepository = CartRepository(cartDao)
    private val favoriteRepository = FavoriteRepository(favoriteDao)
    private val historyRepository = HistoryRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                CartViewModel(cartRepository) as T
            }
            modelClass.isAssignableFrom(FavoriteViewModel::class.java) -> {
                FavoriteViewModel(favoriteRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(historyRepository) as T
            }
            else -> throw IllegalArgumentException("ไม่พบ ViewModel ที่ต้องการ")
        }
    }
}
