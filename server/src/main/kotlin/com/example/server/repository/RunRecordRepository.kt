package com.example.server.repository

import com.example.server.model.RunRecord
import javax.sql.DataSource

class RunRecordRepository(private val ds: DataSource) {

    fun getAllRunRecords(): List<RunRecord> {
        val conn = ds.connection
        val rs = conn.prepareStatement("SELECT * FROM run_records").executeQuery()
        val list = mutableListOf<RunRecord>()
        while (rs.next()) {
            list.add(RunRecord(
                recordId = rs.getString("recordId"),
                reserveId = rs.getString("reserveId"),
                blindUserId = rs.getString("blindUserId"),
                volunteerUserId = rs.getString("volunteerUserId"),
                area = rs.getString("area"),
                duration = rs.getFloat("duration"),
                distance = rs.getFloat("distance"),
                createTime = rs.getString("createTime")
            ))
        }
        rs.close()
        conn.close()
        return list
    }

    fun createRunRecord(record: RunRecord): RunRecord {
        val conn = ds.connection
        val ps = conn.prepareStatement(
            "MERGE INTO run_records (recordId, reserveId, blindUserId, volunteerUserId, area, duration, distance, createTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )
        ps.setString(1, record.recordId)
        ps.setString(2, record.reserveId)
        ps.setString(3, record.blindUserId)
        ps.setString(4, record.volunteerUserId)
        ps.setString(5, record.area)
        ps.setFloat(6, record.duration)
        ps.setFloat(7, record.distance)
        ps.setString(8, record.createTime)
        ps.executeUpdate()
        ps.close()
        conn.close()
        return record
    }
}
