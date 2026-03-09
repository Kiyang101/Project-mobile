package com.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.util.Locale

data class CartItemUI(
    val product: Product,
    val quantity: Int,
    val size: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    userEmail: String?,
    // Use AppViewModelFactory to create Room's CartViewModel
    cartViewModel: CartViewModel = viewModel(factory = AppViewModelFactory(LocalContext.current)),
    // Use ProductViewModel to fetch product details from API
    productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(ProductRepository()))
) {
    val cartEntity by cartViewModel.cart.collectAsState()
    val allProductsState by productViewModel.allProducts.observeAsState()
    val cyanAccent = Color(0xFF00C2E0)
    val historyViewModel: HistoryViewModel = viewModel()

    // Load all products to find names/prices/images by productId
    LaunchedEffect(Unit) {
        productViewModel.loadAllProducts()
    }

    LaunchedEffect(userEmail) {
        userEmail?.let { cartViewModel.loadCart(it) }
    }

    // Combine Room data with API data
    val products = (allProductsState as? Resource.Success)?.data ?: emptyList()
    val cartItemsUI = remember(cartEntity, products) {
        cartEntity?.cartItems?.mapNotNull { roomItem ->
            products.find { it.productId == roomItem.productId }?.let { product ->
                CartItemUI(product, roomItem.quantity, roomItem.size)
            }
        } ?: emptyList()
    }

    // Selection state: Map of (productId, size) to Boolean
    val selectedItems = remember { mutableStateMapOf<Pair<Int, String>, Boolean>() }

    // Update selection state when cart items change
    LaunchedEffect(cartItemsUI) {
        cartItemsUI.forEach { item ->
            val key = Pair(item.product.productId, item.size)
            if (key !in selectedItems) {
                selectedItems[key] = true // Default new items to selected
            }
        }
        val currentKeys = cartItemsUI.map { Pair(it.product.productId, it.size) }.toSet()
        val keysToRemove = selectedItems.keys.filter { it !in currentKeys }
        keysToRemove.forEach { selectedItems.remove(it) }
    }

    val selectedCartItems by remember(cartItemsUI, selectedItems) {
        derivedStateOf { cartItemsUI.filter { selectedItems[Pair(it.product.productId, it.size)] == true } }
    }
    
    val totalPrice by remember(selectedCartItems) {
        derivedStateOf { selectedCartItems.sumOf { it.product.price * it.quantity } }
    }
    
    val isAllSelected = cartItemsUI.isNotEmpty() && cartItemsUI.all { selectedItems[Pair(it.product.productId, it.size)] == true }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Cart", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (cartItemsUI.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Payment", fontSize = 16.sp, color = Color.Gray)
                            Text(
                                "฿${String.format(Locale.getDefault(), "%.2f", totalPrice)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = cyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Select All beside the button
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isAllSelected,
                                    onCheckedChange = { checked ->
                                        cartItemsUI.forEach {
                                            selectedItems[Pair(it.product.productId, it.size)] = checked
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = cyanAccent)
                                )
                                Text("All", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            }

                            Button(
                                onClick = {
                                    if (userEmail != null && selectedCartItems.isNotEmpty()) {
                                        val orderId = generateOrderId()
                                        val history = History(
                                            orderId = orderId,
                                            email = userEmail,
                                            cartItems = selectedCartItems.map { CartItem(it.product.productId, it.quantity, it.size) },
                                            totalPrice = totalPrice,
                                            date = java.util.Date()
                                        )
                                        // 2. บันทึกประวัติลง Firebase
                                        historyViewModel.addHistory(history)
                                        // Process selected items and clear them from cart
                                        selectedCartItems.forEach { item ->
                                            cartViewModel.deleteItem(userEmail, item.product.productId, item.size)
                                        }

                                        navController.navigate("success/$orderId/${totalPrice.toFloat()}") {
                                            popUpTo("home") { inclusive = false }
                                        }
                                    }
                                },
                                enabled = selectedCartItems.isNotEmpty(),
                                modifier = Modifier
                                    .weight(2f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = cyanAccent)
                            ) {
                                Text(
                                    if (selectedCartItems.isEmpty()) "Select items"
                                    else "Checkout (${selectedCartItems.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        if (cartItemsUI.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (allProductsState is Resource.Loading) {
                    CircularProgressIndicator(color = cyanAccent)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your cart is empty", color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { navController.navigate("home") }) {
                            Text("Start Shopping", color = cyanAccent)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItemsUI) { itemUI ->
                    val key = Pair(itemUI.product.productId, itemUI.size)
                    CartItemRow(
                        item = itemUI,
                        isSelected = selectedItems[key] ?: false,
                        onToggleSelection = { selectedItems[key] = it },
                        onUpdateQuantity = { newQty ->
                            userEmail?.let { cartViewModel.updateQuantity(it, itemUI.product.productId, itemUI.size, newQty) }
                        },
                        onDelete = {
                            userEmail?.let { cartViewModel.deleteItem(it, itemUI.product.productId, itemUI.size) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemUI,
    isSelected: Boolean,
    onToggleSelection: (Boolean) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val cyanAccent = Color(0xFF00C2E0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onToggleSelection,
            colors = CheckboxDefaults.colors(checkedColor = cyanAccent)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .size(85.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            AsyncImage(
                model = "${RetrofitInstance.BASE_URL}/api/products/image/view/${item.product.images?.firstOrNull()?.imageId ?: item.product.imageIds?.firstOrNull()}",
                contentDescription = item.product.productName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.productName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            Text("Size: ${item.size}", color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("฿${String.format(Locale.getDefault(), "%.2f", item.product.price)}", fontWeight = FontWeight.Bold, color = cyanAccent, fontSize = 15.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (item.quantity > 1) onUpdateQuantity(item.quantity - 1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                }
                Text("${item.quantity}", modifier = Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                IconButton(
                    onClick = { onUpdateQuantity(item.quantity + 1) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = cyanAccent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}