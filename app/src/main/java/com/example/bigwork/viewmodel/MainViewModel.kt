package com.example.bigwork.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.model.RunRecord
import com.example.bigwork.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    // ==================== 当前登录用户 ====================
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // ==================== 地图选点临时状态 ====================
    private val _selectedMapLocation = MutableStateFlow<MapSelectedLocation?>(null)
    val selectedMapLocation: StateFlow<MapSelectedLocation?> = _selectedMapLocation.asStateFlow()

    // ==================== 登录状态 ====================
    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult.asStateFlow()

    // ==================== 注册状态 ====================
    private val _registerResult = MutableStateFlow<RegisterResult>(RegisterResult.Idle)
    val registerResult: StateFlow<RegisterResult> = _registerResult.asStateFlow()

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

    // ==================== 初始化 ====================
    init {
        loadData()
    }

    // ==================== 登录/注册/登出 ====================
    fun login(userId: String, password: String) {
        if (userId.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult.Error("请输入账号和密码")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = LoginResult.Loading
            try {
                val exists = repository.isUserExists(userId)
                if (!exists) {
                    _loginResult.value = LoginResult.Error("账号不存在")
                    return@launch
                }
                val user = repository.login(userId, password)
                if (user == null) {
                    _loginResult.value = LoginResult.Error("密码错误")
                } else {
                    _currentUser.value = user
                    _loginResult.value = LoginResult.Success(user)
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error("登录失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(userId: String, password: String, userName: String, userType: Int) {
        if (userId.isBlank() || password.isBlank() || userName.isBlank()) {
            _registerResult.value = RegisterResult.Error("请填写所有必填项")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _registerResult.value = RegisterResult.Loading
            try {
                val exists = repository.isUserExists(userId)
                if (exists) {
                    _registerResult.value = RegisterResult.Error("账号已存在")
                    return@launch
                }
                val newUser = User(
                    userId = userId,
                    password = password,
                    userName = userName,
                    userType = userType,
                    age = 0,
                    gender = "未设置",
                    phone = null
                )
                repository.register(newUser)
                _registerResult.value = RegisterResult.Success
            } catch (e: Exception) {
                _registerResult.value = RegisterResult.Error("注册失败：${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginResult.value = LoginResult.Idle
        _registerResult.value = RegisterResult.Idle
    }

    fun resetLoginResult() {
        _loginResult.value = LoginResult.Idle
    }

    fun resetRegisterResult() {
        _registerResult.value = RegisterResult.Idle
    }

    // ==================== 数据操作 ====================
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

    fun setSelectedMapLocation(address: String, latitude: Double, longitude: Double) {
        _selectedMapLocation.value = MapSelectedLocation(address, latitude, longitude)
    }

    fun clearSelectedLocation() {
        _selectedMapLocation.value = null
    }

    fun saveRunRecord(record: RunRecord) {
        viewModelScope.launch {
            try {
                repository.saveRunRecord(record)
            } catch (e: Exception) {
                _errorMessage.value = "保存跑步记录失败：${e.message}"
            }
        }
    }

    fun updateReserve(reserve: Reserve) {
        viewModelScope.launch {
            try {
                repository.createReserve(reserve) // REPLACE策略会更新已存在的记录
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "更新预约失败：${e.message}"
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

    fun getMyReservesAsBlind(userId: String): StateFlow<List<Reserve>> {
        return repository.getReservesByBlindUser(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun getMyReservesAsVolunteer(userId: String): StateFlow<List<Reserve>> {
        return repository.getReservesByVolunteer(userId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}

// 登录结果密封类
sealed class LoginResult {
    data object Idle : LoginResult()
    data object Loading : LoginResult()
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

// 注册结果密封类
sealed class RegisterResult {
    data object Idle : RegisterResult()
    data object Loading : RegisterResult()
    data object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

// 地图选点临时数据
data class MapSelectedLocation(
    val address: String,
    val latitude: Double,
    val longitude: Double
)
