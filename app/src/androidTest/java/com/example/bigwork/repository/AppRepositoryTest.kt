package com.example.bigwork.repository

import androidx.test.core.app.ApplicationProvider
import com.example.bigwork.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repository = AppRepository(context)

    @Test
    fun createUser_shouldPersistToDatabase() = runTest {
        val testUser = User(
            userId = "REPO_TEST_001",
            password = "test123",
            userName = "仓库测试用户",
            userType = 1,
            age = 28,
            gender = "女",
            phone = "13700001111"
        )

        repository.createUser(testUser)

        // 用getUsersByType间接验证数据写入
        val volunteerList = repository.getUsersByType(1).first()
        val containsTestUser = volunteerList.any { it.userId == "REPO_TEST_001" }
        assertTrue("用户未成功写入数据库", containsTestUser)
    }
}