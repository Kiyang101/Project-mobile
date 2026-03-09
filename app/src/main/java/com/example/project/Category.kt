package com.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DryCleaning
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

data class CategoryItem(
    val name: String,
    val itemCount: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController, modifier: Modifier = Modifier) {
    val categories = remember {
        listOf(
            CategoryItem("Blazer", "1.2k+ Items", Icons.Default.BusinessCenter, Color(0xFF1976D2), Color(0xFFE3F2FD)),
            CategoryItem("Coat", "1.2k+ Items", Icons.Default.Checkroom, Color(0xFF00796B), Color(0xFFE0F2F1)),
            CategoryItem("Dress", "1.2k+ Items", Icons.Default.DryCleaning, Color(0xFFC2185B), Color(0xFFFCE4EC)),
            CategoryItem("Pants", "1.2k+ Items", Icons.Default.AccessibilityNew, Color(0xFFE65100), Color(0xFFFFF3E0)),
            CategoryItem("Skirt", "1.2k+ Items", Icons.Default.Woman, Color(0xFF512DA8), Color(0xFFEDE7F6)),
            CategoryItem("Sweater", "1.2k+ Items", Icons.Default.AcUnit, Color(0xFF880E4F), Color(0xFFFCE4EC)),
            CategoryItem("View All", "1.2k+ Items", Icons.Default.GridView, Color(0xFF455A64), Color(0xFFF5F5F5))
        )
    }

    var selectedItem by remember { mutableStateOf(1) } // หน้า Category คือ index 1
    val items = listOf("HOME", "CATEGORIES", "ORDERS")
    val icons = listOf(Icons.Default.Home, Icons.Default.GridView, Icons.Default.Receipt)
    val cyanAccent = Color(0xFF00C2E0)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = {
                            when (index) {
                                0 -> navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                                1 -> { /* Already here */ }
                                2 -> navController.navigate("order") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
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
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { item ->
                CategoryCard(item) {
                    // เชื่อมไปยังหน้าแสดงสินค้าตามหมวดหมู่
                    navController.navigate("products/${item.name}")
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: CategoryItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f) // กำหนดให้เป็นทรงสี่เหลี่ยมแนวตั้งนิดๆ
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ส่วนของไอคอนที่มีวงกลมซ้อนหลัง
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(category.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = category.iconColor,
                    modifier = Modifier.size(35.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ชื่อหมวดหมู่
            Text(
                text = category.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            // จำนวนสินค้า
            Text(
                text = category.itemCount,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductListScreen(
    categoryName: String,
    state: Resource<List<Product>>?,
    navController: NavController
) {
    val cyanAccent = Color(0xFF00C2E0)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        categoryName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state) {
                is Resource.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = cyanAccent
                    )
                }
                is Resource.Success -> {
                    val products = state.data ?: emptyList()

                    if (products.isEmpty()) {
                        // กรณีไม่มีสินค้าในหมวดหมู่นี้
                        Text(
                            text = "No items found in $categoryName",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        // แสดงรายการสินค้าแบบ Grid 2 คอลัมน์ (เหมือนหน้า Home)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(products) { product ->
                                ProductItemCard(product, navController)
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Button(
                            onClick = { /* เพิ่ม logic ให้กดโหลดใหม่ที่นี่ได้ */ },
                            colors = ButtonDefaults.buttonColors(containerColor = cyanAccent)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                null -> {}
            }
        }
    }
}