package com.example.project

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

//data class CartItem(
//    val product: Product,
//    val quantity: Int,
//    val size: String,
//)
//
//data class Cart(
//   val items: List<CartItem>,
//   val totalPrice: Double,
//)

interface ApiService {
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>
}

object RetrofitInstance {
    private const val BASE_URL = "https://d8cd-2001-44c8-6571-d0f6-f40c-ec28-3243-39be.ngrok-free.app"
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