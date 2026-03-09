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
import androidx.navigation.compose.currentBackStackEntryAsState
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

    //Login&User model
    val authVM = viewModel<AuthViewModel>()

    //ดึงข้อมูลจากฐานข้อมูล
    val appViewModelFactory = AppViewModelFactory(LocalContext.current)
    val cartVM: CartViewModel = viewModel(factory = appViewModelFactory)
    val favoriteVM: FavoriteViewModel = viewModel(factory = appViewModelFactory)

    LaunchedEffect(authVM.currentUser?.email) {
        //เมื่อมีการlogin ให้นำข้อมูลfav+cartของuserนั้นมาแสดงผล
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

            //ple
            composable("payment") {
                PaymentScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
