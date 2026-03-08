package com.example.project

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

val CyanAccent = Color(0xFF00C2E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    isLoggedIn: Boolean = false,
    userEmail: String? = null,
    cartViewModel: CartViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val sizes = listOf("S", "M", "L", "XL")
    var selectedSize by remember { mutableStateOf("M") }
    var quantity by remember { mutableStateOf(1) }
    
    val favorites by favoriteViewModel.favorites.collectAsState()
    val isFavorited = favorites.any { it.productId == product.productId }

    LaunchedEffect(userEmail) {
        userEmail?.let {
            favoriteViewModel.observeFavorites(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Product Details",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Favorite button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .alpha(if (isLoggedIn) 1f else 0.5f)
                            .clickable(enabled = isLoggedIn) {
                                if (userEmail != null) {
                                    favoriteViewModel.toggleFavorite(userEmail, product)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorited) CyanAccent else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Add to Cart button
                    Button(
                        onClick = {
                            if (userEmail != null) {
                                cartViewModel.addToCart(userEmail, product, quantity, selectedSize)
                                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isLoggedIn,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add to Cart",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Product Image ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                AsyncImage(
                    model = "${RetrofitInstance.BASE_URL}/api/products/image/view/${product.images?.firstOrNull()?.imageId}",
                    contentDescription = product.productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // ── Product Info ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Category + Price row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                                append(product.category.uppercase())
                            }
                        },
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "฿${product.price}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Name + Rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.productName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${product.rating}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(16.dp))

                // ── Description ────────────────────────────────────────────
                Text(
                    text = "Description",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(20.dp))

                // ── Size Selector ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Size",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Size Guide",
                        fontSize = 13.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { /* Open size guide */ }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    sizes.forEach { size ->
                        val isSelected = selectedSize == size
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CyanAccent else Color.White)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) CyanAccent else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedSize = size },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = size,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(20.dp))

                // ── Quantity ───────────────────────────────────────────────
                Text(
                    text = "Quantity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minus button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { if (quantity > 1) quantity-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "$quantity",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    // Plus button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyanAccent, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { quantity++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
