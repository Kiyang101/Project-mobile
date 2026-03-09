package com.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.ui.theme.CyanAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(ProductRepository())),
    cartViewModel: CartViewModel = viewModel(factory = AppViewModelFactory(LocalContext.current))
) {
    val state = viewModel.allProducts.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authVM = viewModel<AuthViewModel>()
    val cartCount by cartViewModel.cartCount.collectAsState()

    LaunchedEffect(authVM.currentUser) {
        viewModel.loadAllProducts()
        val email = authVM.currentUser?.email
        if (email != null) {
            cartViewModel.loadCart(email)
        } else {
            cartViewModel.clearCartState()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawerContent(
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onSignOut = {
                    scope.launch {
                        authVM.logout()
                        drawerState.close()
                    }
                },
                onSignIn = {
                    scope.launch {
                        drawerState.close()
                        navController.navigate("login")
                    }
                },
                onNavigateToFavorites = { navController.navigate("favorite") },
                onNavigateToPayment = { navController.navigate("payment") }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("SHEOUT", fontWeight = FontWeight.Bold, color = Color.Black) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("cart") }) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = CyanAccent) {
                                            Text("$cartCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.ShoppingBag, contentDescription = "Cart")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF8F9FA))
                )
            },
            bottomBar = {
                AppBottomBar(navController = navController, currentScreenIndex = 0)
            },
        ) { innerPadding ->
            val isRefreshing = state.value is Resource.Loading

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadAllProducts() },
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (val result = state.value) {
                        is Resource.Loading -> {
                            if (!isRefreshing) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = CyanAccent)
                                    }
                                }
                            }
                        }
                        is Resource.Success -> {
                            items(result.data ?: emptyList()) { product ->
                                ProductItemCard(product, navController)
                            }
                        }
                        is Resource.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = result.message ?: "Error loading products",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        null -> {}
                    }
                }
            }
        }
    }
}
