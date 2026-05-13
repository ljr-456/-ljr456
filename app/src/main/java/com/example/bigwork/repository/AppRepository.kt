package com.example.bigwork.repository
import android.content.Context
import com.example.bigwork.database.AppDatabase
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.model.RunRecord
import com.example.bigwork.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    private val apiService = RetrofitClient.apiService
    private val db = AppDatabase.getInstance(context)

    // ==================== 核心：直接返回 Room 可观察 Flow（自动刷新UI） ====================
    fun getUsersByType(userType: Int) = db.userDao().getUsersByType(userType)
    fun getPendingReserves() = db.reserveDao().getPendingReserves()
    fun getReservesByBlindUser(blindUserId: String) = db.reserveDao().getReservesByBlindUser(blindUserId)
    fun getRecordsByBlindUser(blindUserId: String) = db.runRecordDao().getRecordsByBlindUser(blindUserId)

    // ==================== 网络刷新（异常捕获，不崩溃） ====================
    suspend fun refreshUsersByType(userType: Int) {
        withContext(Dispatchers.IO) {
            try {
                val remoteUsers = apiService.getAllUsers()
                val filteredUsers = remoteUsers.filter { it.userType == userType }
                db.userDao().insertUsers(filteredUsers)
            } catch (e: Exception) {
                e.printStackTrace() // 网络失败不崩溃
            }
        }
    }

    suspend fun refreshPendingReserves() {
        withContext(Dispatchers.IO) {
            try {
                val remoteReserves = apiService.getAllReserves()
                val pendingReserves = remoteReserves.filter { it.status == 0 }
                db.reserveDao().insertReserves(pendingReserves)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== 修复闪退：添加异常捕获 + 离线本地兜底 ====================
    suspend fun createUser(user: User): User {
        return withContext(Dispatchers.IO) {
            try {
                // 网络正常：走服务器
                val createdUser = apiService.createUser(user)
                db.userDao().insertUser(createdUser)
                createdUser
            } catch (e: Exception) {
                // 网络异常：直接存本地，绝不闪退！
                db.userDao().insertUser(user)
                user
            }
        }
    }

    suspend fun createReserve(reserve: Reserve): Reserve {
        return withContext(Dispatchers.IO) {
            try {
                val createdReserve = apiService.createReserve(reserve)
                db.reserveDao().insertReserve(createdReserve)
                createdReserve
            } catch (e: Exception) {
                // 离线兜底
                db.reserveDao().insertReserve(reserve)
                reserve
            }
        }
    }

    suspend fun createRunRecord(record: RunRecord): RunRecord {
        return withContext(Dispatchers.IO) {
            try {
                val createdRecord = apiService.createRunRecord(record)
                db.runRecordDao().insertRunRecord(createdRecord)
                createdRecord
            } catch (e: Exception) {
                // 离线兜底
                db.runRecordDao().insertRunRecord(record)
                record
            }
        }
    }

    // ==================== 本地操作 ====================
    suspend fun insertUserLocal(user: User) = db.userDao().insertUser(user)
    suspend fun insertReserveLocal(reserve: Reserve) = db.reserveDao().insertReserve(reserve)

    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            db.userDao().deleteAllUsers()
            db.reserveDao().deleteAllReserves()
            db.runRecordDao().deleteAllRunRecords()
        }
    }

    suspend fun getUserById(userId: String): User? {
        return db.userDao().getUserById(userId)
    }
}