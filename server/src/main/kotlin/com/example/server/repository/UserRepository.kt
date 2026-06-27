package com.example.server.repository

import com.example.server.model.User
import javax.sql.DataSource

class UserRepository(private val ds: DataSource) {

    fun getAllUsers(): List<User> {
        val conn = ds.connection
        val rs = conn.prepareStatement("SELECT * FROM users").executeQuery()
        val list = mutableListOf<User>()
        while (rs.next()) {
            list.add(User(
                userId = rs.getString("userId"),
                password = rs.getString("password"),
                userName = rs.getString("userName"),
                userType = rs.getInt("userType"),
                age = rs.getInt("age"),
                gender = rs.getString("gender"),
                phone = rs.getString("phone")
            ))
        }
        rs.close()
        conn.close()
        return list
    }

    fun getUserById(userId: String): User? {
        val conn = ds.connection
        val ps = conn.prepareStatement("SELECT * FROM users WHERE userId = ?")
        ps.setString(1, userId)
        val rs = ps.executeQuery()
        val user = if (rs.next()) User(
            userId = rs.getString("userId"),
            password = rs.getString("password"),
            userName = rs.getString("userName"),
            userType = rs.getInt("userType"),
            age = rs.getInt("age"),
            gender = rs.getString("gender"),
            phone = rs.getString("phone")
        ) else null
        rs.close()
        ps.close()
        conn.close()
        return user
    }

    fun createUser(user: User): User {
        val conn = ds.connection
        val ps = conn.prepareStatement(
            "MERGE INTO users (userId, password, userName, userType, age, gender, phone) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )
        ps.setString(1, user.userId)
        ps.setString(2, user.password)
        ps.setString(3, user.userName)
        ps.setInt(4, user.userType)
        ps.setInt(5, user.age)
        ps.setString(6, user.gender)
        ps.setString(7, user.phone)
        ps.executeUpdate()
        ps.close()
        conn.close()
        return user
    }

    fun login(userId: String, password: String): User? {
        val conn = ds.connection
        val ps = conn.prepareStatement(
            "SELECT * FROM users WHERE userId = ? AND password = ?"
        )
        ps.setString(1, userId)
        ps.setString(2, password)
        val rs = ps.executeQuery()
        val user = if (rs.next()) User(
            userId = rs.getString("userId"),
            password = rs.getString("password"),
            userName = rs.getString("userName"),
            userType = rs.getInt("userType"),
            age = rs.getInt("age"),
            gender = rs.getString("gender"),
            phone = rs.getString("phone")
        ) else null
        rs.close()
        ps.close()
        conn.close()
        return user
    }
}
