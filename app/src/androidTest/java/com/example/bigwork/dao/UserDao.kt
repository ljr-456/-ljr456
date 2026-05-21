package com.example.bigwork.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.bigwork.database.AppDatabase
import com.example.bigwork.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = database.userDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertUser_shouldReturnUserById() = runTest {
        val testUser = User("TEST01", "123", "测试用户", 0, 20, "男", null)
        userDao.insertUser(testUser)
        val result = userDao.getUserById("TEST01")
        assertEquals(testUser, result)
    }

    @Test
    fun getUsersByType_shouldReturnFilteredList() = runTest {
        val blindUser = User("B01", "123", "盲人用户", 0, 26, "女", null)
        val volunteerUser = User("V01", "123", "志愿者用户", 1, 30, "男", null)
        userDao.insertUsers(listOf(blindUser, volunteerUser))

        val result = userDao.getUsersByType(0).first()
        assertEquals(1, result.size)
        assertEquals("盲人用户", result[0].userName)
    }

    @Test
    fun deleteUser_shouldReturnNull() = runTest {
        val testUser = User("DEL01", "123", "待删除用户", 0, 22, "男", null)
        userDao.insertUser(testUser)
        userDao.deleteUser(testUser)
        val result = userDao.getUserById("DEL01")
        assertEquals(null, result)
    }
}