package com.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.project.RetrofitInstance


@Composable
fun HomeScreen(modifier: Modifier,
               viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory (ProductRepository()))
){
    val state = viewModel.allProducts.observeAsState()
    LaunchedEffect(Unit) { viewModel.loadAllProducts() }
    when (val result = state.value) {
        is Resource.Loading -> { CircularProgressIndicator() }
        is Resource.Success -> {
            LazyColumn(modifier.fillMaxSize().padding(vertical = 60.dp)) {
                items(result.data?: emptyList()){product -> ProductItem(product)}
            }
        }
        is Resource.Error -> {
            Text(text = result.message ?: "Error")  }
        null -> Unit
    }
}

@Composable
fun ProductItem(product: Product) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(model = "${RetrofitInstance.BASE_URL}/api/products/image/view/${product.imageIds[0]}",
                contentDescription = product.productName,
                modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.height(20.dp))
            Text("ProductId: ${product.productId}")
            Text("Title: ${product.productName}")
            Text("Price: ${product.price}")
        }
    }
}