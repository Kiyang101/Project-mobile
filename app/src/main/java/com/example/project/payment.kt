package com.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(onBack: () -> Unit,onSelectMethod: (String) -> Unit) {
    var selectedId by remember { mutableStateOf("visa") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods", fontWeight = FontWeight.Bold) },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // ... (Order Total) ...
                    Button(
                        onClick = {
                            // เมื่อกดปุ่ม ให้ส่งชื่อบัตรที่เลือกกลับไป และย้อนกลับหน้าเดิม
                            val methodName = if (selectedId == "visa") "Visa" else "Mastercard"
                            onSelectMethod(methodName)
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CCE5))
                    ) {
                        Text("Confirm", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            PaymentItemCard(
                title = "Visa",
                subtitle = "Expires 12/26",
                imageRes = R.drawable.visa_logo,
                isSelected = selectedId == "visa",
                onSelect = { selectedId = "visa" } // เมื่อกด ให้เซตค่าเป็น visa
            )
            Spacer(modifier = Modifier.height(8.dp))
            PaymentItemCard(
                title = "Mastercard",
                subtitle = "Expires 09/25",
                imageRes = R.drawable.mastercard_logo,
                isSelected = selectedId == "master",
                onSelect = { selectedId = "master" } // เมื่อกด ให้เซตค่าเป็น master
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PaymentItemCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
    ) {
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
            Box(modifier = Modifier
                .size(width = 50.dp, height = 32.dp)
                .background(Color(0xFFF1F3F4), RoundedCornerShape(8.dp))
            ){
                Icon(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.Unspecified // ใช้สีดั้งเดิมของรูปภาพ
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}
