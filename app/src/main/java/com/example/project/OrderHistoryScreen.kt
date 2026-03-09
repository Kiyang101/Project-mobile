package com.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    userEmail: String?,
    historyViewModel: HistoryViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(ProductRepository()))
) {
    val historyList by historyViewModel.historyList.collectAsState()
    val allProductsState by productViewModel.allProducts.observeAsState()
    val cyanAccent = Color(0xFF00C2E0)

    LaunchedEffect(userEmail) {
        userEmail?.let { historyViewModel.loadHistory(it) }
        productViewModel.loadAllProducts()
    }

    val products = (allProductsState as? Resource.Success)?.data ?: emptyList()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf("HOME", "CATEGORIES", "ORDERS")
                val icons = listOf(Icons.Default.Home, Icons.Default.GridView, Icons.Default.Receipt)
                
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = index == 2, // Highlight ORDERS
                        onClick = {
                            when (index) {
                                0 -> navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                                1 -> navController.navigate("category") {
                                    popUpTo("home") { saveState = true }
                                }
                                2 -> { /* Already here */ }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = cyanAccent,
                            selectedTextColor = cyanAccent,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (historyList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(historyList.sortedByDescending { it.date }) { history ->
                        OrderCard(history, products)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(history: History, products: List<Product>) {
    val cyanAccent = Color(0xFF00C2E0)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon, Order ID, Date, and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0F7FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = cyanAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Order #${history.orderId}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF1A1C1E)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Placed on ${dateFormat.format(history.date)}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = Color(0xFFE0F7FA),
                    shape = RoundedCornerShape(50.dp) // Pill shape
                ) {
                    Text(
                        text = "PROCESSING",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = cyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // ส่วนแสดงรูปสินค้าแบบซ้อนกัน (Overlapping)
                val displayCount = 2 // จำนวนรูปที่จะแสดงก่อนเป็นกล่อง +
                val itemsToShow = history.cartItems.take(displayCount)
                val hasMore = history.cartItems.size > displayCount
                val totalItemsToDraw = itemsToShow.size + (if (hasMore) 1 else 0)

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .width((50 + (totalItemsToDraw - 1) * 28).dp) // คำนวณความกว้างตามจำนวนรูปที่ซ้อนกัน
                ) {
                    itemsToShow.forEachIndexed { index, item ->
                        val product = products.find { it.productId == item.productId }
                        Box(
                            modifier = Modifier
                                .offset(x = (index * 28).dp) // ระยะซ้อนกัน (เลื่อนภาพถัดไป 28dp จากขนาด 50dp)
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White) // พื้นหลังสีขาวสำหรับทำเป็นขอบ
                                .padding(2.dp) // ขอบหนา 2dp
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                        ) {
                            AsyncImage(
                                model = "${RetrofitInstance.BASE_URL}/api/products/image/view/${product?.imageIds?.firstOrNull()}",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // แสดงกล่อง + (จำนวนที่เหลือ) แบบซ้อนต่อท้าย
                    if (hasMore) {
                        Box(
                            modifier = Modifier
                                .offset(x = (displayCount * 28).dp)
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0F2F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${history.cartItems.size - displayCount}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // ส่วนแสดงราคารวม
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total amount",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        "฿${String.format(Locale.getDefault(), "%.2f", history.totalPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1C1E)
                    )
                }
            }
        }
    }
}
