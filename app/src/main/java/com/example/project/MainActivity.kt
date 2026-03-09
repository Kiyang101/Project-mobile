package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.theme.ProjectTheme
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.firebaseapp.LoginScreen
import com.example.firebaseapp.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectTheme {
                Main()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val navController = rememberNavController()
    val cyanAccent = Color(0xFF00C2E0)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authVM = viewModel<AuthViewModel>()
    
    val appViewModelFactory = AppViewModelFactory(LocalContext.current)
    val cartVM: CartViewModel = viewModel(factory = appViewModelFactory)
    val favoriteVM: FavoriteViewModel = viewModel(factory = appViewModelFactory)

    LaunchedEffect(authVM.currentUser?.email) {
        authVM.currentUser?.email?.let { email ->
            favoriteVM.loadFavorites(email)
            cartVM.loadCart(email)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
        ) {
            composable("home") {
                HomeScreen(
                    navController = navController,
                    modifier = Modifier,
                    cartViewModel = cartVM
                )
            }
            composable("product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(ProductRepository()))

                LaunchedEffect(productId) {
                    productId?.toIntOrNull()?.let {
                        productViewModel.loadProduct(it)
                    }
                }

                val productState by productViewModel.product.observeAsState()

                when (val result = productState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = cyanAccent)
                        }
                    }
                    is Resource.Success -> {
                        result.data?.let { product ->
                            ProductDetailScreen(
                                product = product,
                                isLoggedIn = authVM.isLoggedIn,
                                userEmail = authVM.currentUser?.email,
                                cartViewModel = cartVM,
                                favoriteViewModel = favoriteVM,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                    is Resource.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = result.message ?: "Error loading product", color = Color.Red)
                        }
                    }
                    null -> {}
                }
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home"){
                            popUpTo("login"){inclusive = true}
                        }
                    },
                    onNavigateToRegister = {navController.navigate("register")},
                    onBack = { navController.popBackStack() }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("home"){
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {navController.popBackStack()},
                    onBack = { navController.popBackStack() }
                )
            }
            composable("category") {
                CategoryScreen(
                    navController = navController,
                    modifier = Modifier
                )
            }
            // ใน MainActivity.kt ส่วนของ NavHost
            composable("products/{categoryName}") { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(ProductRepository()))

                // โหลดข้อมูลสินค้าตามหมวดหมู่เมื่อเข้าหน้านี้
                LaunchedEffect(categoryName) {
                    viewModel.loadProductsByCategory(categoryName.lowercase())
                }

                val state by viewModel.allProducts.observeAsState()

                // เรียกใช้ CategoryProductListScreen ที่คุณเขียนไว้ใน Category.kt
                CategoryProductListScreen(
                    categoryName = categoryName,
                    state = state,
                    navController = navController
                )
            }
            // ใน MainActivity.kt
            composable("cart") {
                CartScreen(
                    navController = navController,
                    userEmail = authVM.currentUser?.email
                )
            }
            composable(
                "success/{orderId}/{totalPrice}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType },
                    navArgument("totalPrice") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                val totalPrice = backStackEntry.arguments?.getFloat("totalPrice")?.toDouble() ?: 0.0
                SuccessScreen(
                    navController = navController,
                    orderId = orderId,
                    totalPrice = totalPrice
                )
            }
            // ภายใน NavHost ใน MainActivity.kt
            composable("order") {
                OrderHistoryScreen(
                    navController = navController,
                    userEmail = authVM.currentUser?.email
                )
            }
        }
    }
}