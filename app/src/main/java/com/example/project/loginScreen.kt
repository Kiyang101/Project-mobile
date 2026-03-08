package com.example.firebaseapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.AlertDialog
//import androidx.compose.material.Icon
//import androidx.compose.material.OutlinedButton
//import androidx.compose.material.OutlinedTextField
//import androidx.compose.material.Text
//import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showForgotDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val authVM = viewModel<AuthViewModel>()
    val authState by authVM.authState.collectAsState()
    LaunchedEffect(authState) {
        when(authState){
            is AuthViewModel.AuthState.Success -> {
                authVM.resetState()
                onLoginSuccess()
            }else -> {}
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotDialog = false
                resetEmail = ""
            },
            title = { Text("ลืมรหัสผ่าน") },
            text = {
                Column {
                    Text("กรอก Email ที่ใช้สมัครสมาชิก\nระบบจะส่งลิงก์รีเซ็ตรหัสผ่านให้")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authVM.resetPassword(resetEmail)
                        showForgotDialog = false
                        resetEmail = ""
                    },
                    enabled = resetEmail.isNotBlank(),
                ) { Text("ส่ง Email", color = Color(0xFF6D9E51)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    resetEmail = ""
                }) { Text("ยกเลิก", color = Color.Gray) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFE3DBBB)
        )
        Spacer(Modifier.height(16.dp))
        Text("Sign in", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))

        //------------------- TextField กรอก Email และ Password -------------------
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        imageVector = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Visibility"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        //------------------- ปุ่มลืมรหัสผ่าน -------------------
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                resetEmail = email
                showForgotDialog = true
            }) {
                Text("ลืมรหัสผ่าน?")
            }
        }
        Spacer(Modifier.height(12.dp))

        //------------------- ปุ่มเข้าสู่ระบบ -------------------
        Button(
            onClick = {
                authVM.loginWithEmail(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(6.dp),
            enabled = email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6D9E51),
                contentColor = Color.White
            )
        ) { Text("เข้าสู่ระบบ", color = Color.White) }

        Spacer(Modifier.height(12.dp))

        //------------------- ปุ่มไปหน้าลงทะเบียน -------------------
        TextButton(onClick = onNavigateToRegister) {
            Text("ยังไม่เป็นสมาชิก? สมัครสมาชิก")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.Gray,
                fontSize = 14.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        //------------------- Icon สำหรับล็อกอินผ่านโซเชียล -------------------
//        Row {
//            IconButton(
//                onClick = {  },
//                modifier = Modifier
//                    .size(56.dp)
//                    .border(1.dp, Color.LightGray, CircleShape)
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.search),
//                    contentDescription = "Sign in with Google",
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//            Spacer(Modifier.width(12.dp))
//
//            IconButton(
//                onClick = { },
//                modifier = Modifier
//                    .size(56.dp)
//                    .border(1.dp, Color.LightGray, CircleShape)
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.facebook),
//                    contentDescription = "Sign in with Google",
//                    modifier = Modifier.size(38.dp)
//                )
//            }
//        }
//        OutlinedButton(
//            onClick = {
//                authVM.loginWithGoogle(context)
//            },
//            modifier = Modifier.fillMaxWidth().height(50.dp),
//            shape = RoundedCornerShape(8.dp),
//            border = BorderStroke(1.dp, Color.LightGray)
//        ) {
//            Text("Sign in with Google", color = Color.Black)
//        }
    }
}
