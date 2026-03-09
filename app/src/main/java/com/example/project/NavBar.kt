package com.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NavDrawerContent(
    onCloseDrawer: () -> Unit,
    onSignOut: () -> Unit,
    onSignIn: () -> Unit
) {
    val cyanAccent = Color(0xFF00C2E0)
    val lightCyan = Color(0xFFE0F7FA)
    val authVM = viewModel<AuthViewModel>()

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxHeight().width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(lightCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = cyanAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = authVM.currentUser?.email ?: "Guest",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Items
            DrawerMenuItem(
                icon = Icons.Default.Person,
                label = "User Profile",
                onClick = { /* Handle navigation */ }
            )
            DrawerMenuItem(
                icon = Icons.Default.FavoriteBorder,
                label = "Favorites",
                onClick = { /* Handle navigation */ }
            )
            DrawerMenuItem(
                icon = Icons.Default.Payments,
                label = "Payment Methods",
                onClick = { /* Handle navigation */ }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Sign In and Sign Out Button
            if(authVM.isLoggedIn){
                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }else{
                Button(
                    onClick = onSignIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val lightCyan = Color(0xFFE0F7FA)
    val cyanAccent = Color(0xFF00C2E0)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (label == "User Profile") lightCyan else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (label == "User Profile") cyanAccent else Color.DarkGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (label == "User Profile") cyanAccent else Color.DarkGray
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (label == "User Profile") cyanAccent else Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
