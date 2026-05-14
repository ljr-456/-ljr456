// MainViewModel.kt（完整数据绑定版）
package com.example.bigwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    // ==================== UI状态：数据状态（自动更新）====================
    val blindUsers: StateFlow<List<User>> = repository.getBlindUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val volunteers: StateFlow<List<User>> = repository.getVolunteers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingReserves: StateFlow<List<Reserve>> = repository.getPendingReserves()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ==================== UI状态：加载状态 + 错误状态 ====================
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ==================== 初始化 + 数据操作 ====================
    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.refreshAllData()
            } catch (e: Exception) {
                _errorMessage.value = "加载失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createUser(user: User) {
        viewModelScope.launch {
            try {
                repository.createUser(user)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "创建用户失败：${e.message}"
            }
        }
    }

    fun createReserve(reserve: Reserve) {
        viewModelScope.launch {
            try {
                repository.createReserve(reserve)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "创建预约失败：${e.message}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "清空数据失败：${e.message}"
            }
        }
    }

    // ==================== 详情页数据绑定 ====================
    fun getUserById(userId: String): StateFlow<User?> {
        return repository.getUserById(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }

    fun getReserveById(reserveId: String): StateFlow<Reserve?> {
        return repository.getReserveById(reserveId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }
}