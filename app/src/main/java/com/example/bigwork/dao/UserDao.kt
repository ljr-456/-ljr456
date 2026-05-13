package com.example.bigwork.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bigwork.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // 插入单个用户
    // 插入单个用户
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // 批量插入用户
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    // 更新用户信息
    @Update
    suspend fun updateUser(user: User)

    // 删除单个用户
    @Delete
    suspend fun deleteUser(user: User)

    // 删除所有用户
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // 根据ID查询用户
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): User?

    // 根据用户类型查询所有用户（Flow自动监听变化）
    @Query("SELECT * FROM users WHERE userType = :userType")
    fun getUsersByType(userType: Int): Flow<List<User>>

    // 登录验证
    @Query("SELECT * FROM users WHERE userId = :userId AND password = :password")
    suspend fun login(userId: String, password: String): User?
}