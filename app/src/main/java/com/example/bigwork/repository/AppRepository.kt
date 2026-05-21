// AppRepository.kt（补充完整，保留原有离线逻辑）
package com.example.bigwork.repository

import android.content.Context
import com.example.bigwork.database.AppDatabase
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.model.RunRecord
import com.example.bigwork.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    private val apiService = RetrofitClient.apiService
    private val db = AppDatabase.getInstance(context)

    // ==================== 只读数据流（UI唯一数据来源）====================
    fun getBlindUsers(): Flow<List<User>> = db.userDao().getUsersByType(0)
    fun getVolunteers(): Flow<List<User>> = db.userDao().getUsersByType(1)
    fun getPendingReserves(): Flow<List<Reserve>> = db.reserveDao().getPendingReserves()
    fun getUserById(userId: String): Flow<User?> = db.userDao().getUserByIdFlow(userId)
    fun getReserveById(reserveId: String): Flow<Reserve?> = db.reserveDao().getReserveByIdFlow(reserveId)
    fun getReservesByBlindUser(blindUserId: String): Flow<List<Reserve>> =
        db.reserveDao().getReservesByBlindUser(blindUserId)
    fun getReservesByVolunteer(volunteerUserId: String): Flow<List<Reserve>> =
        db.reserveDao().getReservesByVolunteer(volunteerUserId)

    // ==================== 数据操作（写入后自动触发上面的Flow更新）====================
    suspend fun refreshAllData() {
        withContext(Dispatchers.IO) {
            try {
                val users = apiService.getAllUsers()
                db.userDao().insertUsers(users)

                val reserves = apiService.getAllReserves()
                db.reserveDao().insertReserves(reserves)
            } catch (e: Exception) {
                // 网络API（jsonplaceholder）字段与本地模型不匹配，反序列化后主键为null
                // Room插入会失败，这里忽略网络错误，UI展示本地数据即可
            }
        }
    }

    suspend fun createUser(user: User) {
        withContext(Dispatchers.IO) {
            try {
                val createdUser = apiService.createUser(user)
                db.userDao().insertUser(createdUser)
            } catch (e: Exception) {
                db.userDao().insertUser(user) // 离线兜底
            }
        }
    }

    suspend fun createReserve(reserve: Reserve) {
        withContext(Dispatchers.IO) {
            try {
                val createdReserve = apiService.createReserve(reserve)
                db.reserveDao().insertReserve(createdReserve)
            } catch (e: Exception) {
                db.reserveDao().insertReserve(reserve) // 离线兜底
            }
        }
    }

    suspend fun login(userId: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            db.userDao().login(userId, password)
        }
    }

    suspend fun isUserExists(userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            db.userDao().getUserById(userId) != null
        }
    }

    suspend fun register(user: User) {
        withContext(Dispatchers.IO) {
            db.userDao().insertUser(user)
        }
    }

    suspend fun saveRunRecord(record: RunRecord) {
        withContext(Dispatchers.IO) {
            db.runRecordDao().insertRunRecord(record)
        }
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            db.userDao().deleteAllUsers()
            db.reserveDao().deleteAllReserves()
        }
    }
}