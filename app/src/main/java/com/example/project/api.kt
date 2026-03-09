package com.example.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ProductImage(
    val imageId: Int = 0,
    val orientation: String = ""
)

data class Product(
    val productId: Int = 0,
    val productName: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val sold: Int = 0,
    val rating: Double = 0.0,
    val active: Boolean = true,
    val quantity: Int = 0,
    val size: String = "",
    val category: String = "",
    val imageIds: List<Int>? = null,
    val images: List<ProductImage>? = null
)

interface ApiService {
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int
    ): Response<List<Product>>

    @GET("api/products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String
    ): Response<List<Product>>
}

object RetrofitInstance {
    const val BASE_URL = "https://broderick-cognoscible-bulgingly.ngrok-free.dev"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T): Resource<T>(data)
    class Error<T>(message: String?, data: T? = null): Resource<T>(data, message)
    class Loading<T>: Resource<T>()
}

class ProductRepository {
    suspend fun fetchProduct(): Resource<List<Product>> {
        return try {
            val response = RetrofitInstance.api.getAllProducts()
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty body")
            } else { Resource.Error("Error ${response.code()}") }
        } catch (e: Exception) { Resource.Error(e.message) }
    }

    suspend fun fetchProductById(id: Int): Resource<Product> {
        return try {
            val response = RetrofitInstance.api.getProductById(id)
            if (response.isSuccessful) {
                response.body()?.firstOrNull()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Product not found")
            } else { Resource.Error("Error ${response.code()}") }
        } catch (e: Exception) { Resource.Error(e.message) }
    }

    suspend fun fetchProductsByCategory(category: String): Resource<List<Product>> {
        return try {
            val response = RetrofitInstance.api.getProductsByCategory(category)
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty body")
            } else { Resource.Error("Error ${response.code()}") }
        } catch (e: Exception) { Resource.Error(e.message) }
    }
}

class ProductViewModel( private val repository: ProductRepository) : ViewModel() {
    private val _product = MutableLiveData<Resource<Product>>()
    private val _allProducts = MutableLiveData<Resource<List<Product>>>()
    val allProducts: LiveData<Resource<List<Product>>> = _allProducts
    val product: LiveData<Resource<Product>> = _product

    fun loadProduct(id: Int) {
        _product.value = Resource.Loading()
        viewModelScope.launch {
            _product.value = repository.fetchProductById(id)
        }
    }

    fun loadAllProducts(){
        _allProducts.value = Resource.Loading()
        viewModelScope.launch {
            _allProducts.value = repository.fetchProduct()
        }
    }

    fun clearProducts() {
        _allProducts.value = null
    }

    fun loadProductsByCategory(category: String) {
        _allProducts.value = Resource.Loading()
        viewModelScope.launch {
            _allProducts.value = repository.fetchProductsByCategory(category)
        }
    }
}

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
