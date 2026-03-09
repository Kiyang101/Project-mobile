package com.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
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
    
    var selectedItem by remember { mutableStateOf(0) }
    val cartCount by cartViewModel.cartCount.collectAsState()

    // Re-fetch products and update cart when user authentication state changes
    LaunchedEffect(authVM.currentUser) {
        viewModel.loadAllProducts()
        val email = authVM.currentUser?.email
        if (email != null) {
            cartViewModel.loadCart(email)
        } else {
            cartViewModel.clearCartState()
        }
    }

    val items = listOf("HOME", "CATEGORIES", "ORDERS")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.GridView,
        Icons.Default.Receipt,
    )

    val cyanAccent = Color(0xFF00C2E0)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavDrawerContent(
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
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
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "SHEOUT",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Open Cart */ }) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(containerColor = cyanAccent) {
                                            Text("$cartCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.ShoppingBag, contentDescription = "Cart")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFF8F9FA)
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                when (index) {
                                    0 -> navController.navigate("home")
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = cyanAccent,
                                selectedTextColor = cyanAccent,
                                unselectedIconColor = Color.LightGray,
                                unselectedTextColor = Color.LightGray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            },
        ) { innerPadding ->
            val isRefreshing = state.value is Resource.Loading

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.loadAllProducts() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Replaced LazyColumn with LazyVerticalGrid for 2 columns
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    when (val result = state.value) {
                        is Resource.Loading -> {
                            // Only show full-screen loader if not already showing pull-to-refresh indicator
                            if (!isRefreshing) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF00C2E0))
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

                        null -> item { }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: Product, navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("product/${product.productId}") }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // Adjusts the height proportionally to the width
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF4F4F4))
        ) {
            AsyncImage(
                model = "${RetrofitInstance.BASE_URL}/api/products/image/view/${product.imageIds?.firstOrNull()}",
                contentDescription = product.productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text Content
        Text(
            text = product.productName,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = product.category,
            color = Color.Gray,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "฿${product.price}",
            color = Color(0xFF00C2E0),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}
