package com.example.bigwork.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bigwork.model.Reserve
import kotlinx.coroutines.flow.Flow

@Dao
interface ReserveDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertReserve(reserve: Reserve)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertReserves(reserves: List<Reserve>)

    @Update
    suspend fun updateReserve(reserve: Reserve)

    @Delete
    suspend fun deleteReserve(reserve: Reserve)

    @Query("DELETE FROM reserves")
    suspend fun deleteAllReserves()

    // 根据ID查询预约
    @Query("SELECT * FROM reserves WHERE reserveId = :reserveId")
    suspend fun getReserveById(reserveId: String): Reserve?

    // 查询所有待接单的预约（志愿者端用）
    @Query("SELECT * FROM reserves WHERE status = 0 ORDER BY createTime DESC")
    fun getPendingReserves(): Flow<List<Reserve>>

    // 查询某个盲人发起的所有预约
    @Query("SELECT * FROM reserves WHERE blindUserId = :blindUserId ORDER BY createTime DESC")
    fun getReservesByBlindUser(blindUserId: String): Flow<List<Reserve>>

    // 查询某个志愿者接的所有预约
    @Query("SELECT * FROM reserves WHERE volunteerUserId = :volunteerUserId ORDER BY createTime DESC")
    fun getReservesByVolunteer(volunteerUserId: String): Flow<List<Reserve>>

    @Query("SELECT * FROM reserves WHERE reserveId = :reserveId")
    fun getReserveByIdFlow(reserveId: String): Flow<Reserve?>
}