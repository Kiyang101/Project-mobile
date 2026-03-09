package com.example.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth

    var currentUser by mutableStateOf(auth.currentUser)
        private set

    val isLoggedIn: Boolean
        get() = currentUser != null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        object ResetPasswordSent : AuthState()
        data class Error(val message: String) : AuthState()
    }

    init {
        // Listen for auth state changes to keep currentUser state in sync
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
    }

    //------------------ ลงทะเบียนใช้งาน ------------------
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "อีเมลนี้ถูกใช้งานแล้ว"
                    "ERROR_INVALID_EMAIL" -> "รูปแบบอีเมลไม่ถูกต้อง"
                    "ERROR_WEAK_PASSWORD" -> "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร"
                    else -> e.message ?: "เกิดข้อผิดพลาดในการลงทะเบียน"
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    //------------------ รีเซตรหัสผ่าน ------------------
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.ResetPasswordSent
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "ไม่พบบัญชีนี้ในระบบ"
                    "ERROR_INVALID_EMAIL"  -> "รูปแบบ Email ไม่ถูกต้อง"
                    else -> "เกิดข้อผิดพลาด กรุณาลองใหม่"
                }
                _authState.value = AuthState.Error(message)
            }
        }
    }

    //------------------ ล็อกอินด้วยอีเมล์ ------------------
    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: FirebaseAuthException) {
                val message = when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "ไม่พบบัญชีนี้ในระบบ"
                    "ERROR_WRONG_PASSWORD" -> "รหัสผ่านไม่ถูกต้อง"
                    "ERROR_INVALID_EMAIL" -> "รูปแบบอีเมลไม่ถูกต้อง"
                    "ERROR_USER_DISABLED" -> "บัญชีนี้ถูกระงับการใช้งาน"
                    else -> e.message ?: "เกิดข้อผิดพลาดในการเข้าสู่ระบบ"
                }
                _authState.value = AuthState.Error(message)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "เกิดข้อผิดพลาด")
            }
        }
    }

    //------------------ ออกจากระบบ ------------------
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
