package com.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onBack: () -> Unit) {
    var selectedId by remember { mutableStateOf("visa") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomCheckoutSection()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            // ส่วน Saved Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Saved Cards", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Edit", color = Color(0xFF00BCD4), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            PaymentItemCard(
                title = "Visa ending in 4242",
                subtitle = "Expires 12/26",
                isSelected = selectedId == "visa",
                onSelect = { selectedId = "visa" } // เมื่อกด ให้เซตค่าเป็น visa
            )
            Spacer(modifier = Modifier.height(8.dp))
            PaymentItemCard(
                title = "Mastercard ending in 8888",
                subtitle = "Expires 09/25",
                isSelected = selectedId == "master",
                onSelect = { selectedId = "master" } // เมื่อกด ให้เซตค่าเป็น master
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ส่วน E-Wallets & More
            Text("E-Wallets & More", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))) {
                WalletItem("Apple Pay", "Fast and secure checkout")
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                WalletItem("Bank Transfer", "Secure bank to bank wire")
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                WalletItem("PayPal", "Linked to user@email.com")
            }
        }
    }
}

@Composable
fun PaymentItemCard(title: String, subtitle: String, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect()},
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF00C2E0)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFF1F3F4), RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}

@Composable
fun WalletItem(title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFFE1F5FE), RoundedCornerShape(8.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
    }
}

@Composable
fun BottomCheckoutSection() {
    Surface(shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order Total", fontSize = 16.sp, color = Color.Gray)
                Text("$249.00", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CCE5))
            ) {
                Text("Continue to Review", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}