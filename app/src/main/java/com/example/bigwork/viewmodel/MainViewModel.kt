package com.example.bigwork.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.repository.AppRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    // ==================== 修复：StateFlow 直接观察 Room Flow，自动更新 ====================
    val blindUsers: StateFlow<List<User>> = repository.getUsersByType(0)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val volunteers: StateFlow<List<User>> = repository.getUsersByType(1)
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

    // 初始化时拉取一次网络数据
    init {
        refreshAllData()
    }

    // 主动刷新全部数据
    fun refreshAllData() {
        viewModelScope.launch {
            repository.refreshUsersByType(0)
            repository.refreshUsersByType(1)
            repository.refreshPendingReserves()
        }
    }

    // ==================== 写入：调用后UI自动刷新 ====================
    fun createUser(user: User) {
        viewModelScope.launch {
            repository.createUser(user)
        }
    }

    fun createReserve(reserve: Reserve) {
        viewModelScope.launch {
            repository.createReserve(reserve)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }
}