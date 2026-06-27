package com.example.server.repository

import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

object DatabaseFactory {
    val dataSource: DataSource by lazy {
        JdbcDataSource().apply {
            val dbPath = System.getenv("DB_PATH") ?: "./data/bigwork_db"
            setURL("jdbc:h2:file:$dbPath;DB_CLOSE_DELAY=-1;MODE=MySQL")
            user = "sa"
            password = ""
        }
    }

    fun init() {
        val conn = dataSource.connection
        conn.createStatement().use { stmt ->
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    userId VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    userName VARCHAR(255) NOT NULL,
                    userType INT NOT NULL,
                    age INT NOT NULL,
                    gender VARCHAR(50) NOT NULL,
                    phone VARCHAR(50)
                )
            """.trimIndent())

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reserves (
                    reserveId VARCHAR(255) PRIMARY KEY,
                    blindUserId VARCHAR(255) NOT NULL,
                    volunteerUserId VARCHAR(255),
                    area VARCHAR(500) NOT NULL,
                    detailAddress VARCHAR(500) DEFAULT '',
                    latitude DOUBLE DEFAULT 0.0,
                    longitude DOUBLE DEFAULT 0.0,
                    remark VARCHAR(1000) NOT NULL,
                    status INT NOT NULL,
                    createTime VARCHAR(100) NOT NULL
                )
            """.trimIndent())

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS run_records (
                    recordId VARCHAR(255) PRIMARY KEY,
                    reserveId VARCHAR(255) NOT NULL,
                    blindUserId VARCHAR(255) NOT NULL,
                    volunteerUserId VARCHAR(255) NOT NULL,
                    area VARCHAR(500) NOT NULL,
                    duration REAL NOT NULL,
                    distance REAL DEFAULT 0.0,
                    createTime VARCHAR(100) NOT NULL
                )
            """.trimIndent())
        }
        conn.close()
    }
}
