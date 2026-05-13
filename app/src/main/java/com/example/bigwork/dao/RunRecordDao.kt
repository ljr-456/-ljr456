package com.example.bigwork.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bigwork.model.RunRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RunRecordDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertRunRecord(record: RunRecord)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertRunRecords(records: List<RunRecord>)

    @Update
    suspend fun updateRunRecord(record: RunRecord)

    @Delete
    suspend fun deleteRunRecord(record: RunRecord)

    @Query("DELETE FROM run_records")
    suspend fun deleteAllRunRecords()

    // 根据ID查询跑步记录
    @Query("SELECT * FROM run_records WHERE recordId = :recordId")
    suspend fun getRunRecordById(recordId: String): RunRecord?

    // 查询某个盲人的所有跑步记录
    @Query("SELECT * FROM run_records WHERE blindUserId = :blindUserId ORDER BY createTime DESC")
    fun getRecordsByBlindUser(blindUserId: String): Flow<List<RunRecord>>

    // 查询某个志愿者的所有跑步记录
    @Query("SELECT * FROM run_records WHERE volunteerUserId = :volunteerUserId ORDER BY createTime DESC")
    fun getRecordsByVolunteer(volunteerUserId: String): Flow<List<RunRecord>>

    // 查询某次预约对应的跑步记录
    @Query("SELECT * FROM run_records WHERE reserveId = :reserveId")
    suspend fun getRecordByReserveId(reserveId: String): RunRecord?
}