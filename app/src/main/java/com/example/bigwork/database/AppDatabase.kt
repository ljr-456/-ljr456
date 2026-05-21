package com.example.bigwork.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.bigwork.dao.UserDao
import com.example.bigwork.dao.ReserveDao
import com.example.bigwork.dao.RunRecordDao
import com.example.bigwork.model.User
import com.example.bigwork.model.Reserve
import com.example.bigwork.model.RunRecord

@Database(
    entities = [User::class, Reserve::class, RunRecord::class], // 包含三个表
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // 对外提供三个Dao实例
    abstract fun userDao(): UserDao
    abstract fun reserveDao(): ReserveDao
    abstract fun runRecordDao(): RunRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blind_run_database"
                ).fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}