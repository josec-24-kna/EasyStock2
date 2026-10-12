package com.example.easystock2.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easystock2.auth.data.UserDao
import com.example.easystock2.auth.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _loggedInUserId = MutableStateFlow<Int?>(null)
    val loggedInUserId: StateFlow<Int?> = _loggedInUserId.asStateFlow()

    fun onUsernameChange(newUsername: String) { _username.value = newUsername }

    fun onPinChange(newPin: String) { _pin.value = newPin }

    fun register() {
        viewModelScope.launch {
            if (_username.value.isBlank() || _pin.value.isBlank()) {
                _message.value = "Llena todos los campos"
                return@launch
            }
            val exists = userDao.checkUserExists(_username.value) > 0
            if (exists) {
                _message.value = "El usuario ya existe"
            } else {
                userDao.insertUser(UserEntity(username = _username.value, pin = _pin.value))
                _message.value = "Registro exitoso. Ahora inicia sesión."
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            val user = userDao.login(_username.value, _pin.value)
            if (user != null) {
                _loggedInUserId.value = user.id
            } else {
                _message.value = "Credenciales incorrectas"
            }
        }
    }
}

// Factory necesario para inyectar el UserDao en el ViewModel sin usar Dagger/Hilt
class AuthViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userDao) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}