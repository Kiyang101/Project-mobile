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

data class Product(
    val productId: String,
    val productName: String,
    val description: String,
    val price: Double,
    val category: String,
    val imageIds: List<Int>,
    val rating: Double,
)

interface ApiService {
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>
}

object RetrofitInstance {
    const val BASE_URL = "https://d8cd-2001-44c8-6571-d0f6-f40c-ec28-3243-39be.ngrok-free.app"
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
}

class ProductViewModel( private val repository: ProductRepository) : ViewModel() {
    private val _allProducts = MutableLiveData<Resource<List<Product>>>()
    val allProducts: LiveData<Resource<List<Product>>> = _allProducts

    fun loadAllProducts(){
        _allProducts.value = Resource.Loading()
        viewModelScope.launch {
            _allProducts.value = repository.fetchProduct()
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