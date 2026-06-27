package com.example.server.repository

import com.example.server.model.Reserve
import javax.sql.DataSource

class ReserveRepository(private val ds: DataSource) {

    fun getAllReserves(): List<Reserve> {
        val conn = ds.connection
        val rs = conn.prepareStatement("SELECT * FROM reserves").executeQuery()
        val list = mutableListOf<Reserve>()
        while (rs.next()) {
            list.add(Reserve(
                reserveId = rs.getString("reserveId"),
                blindUserId = rs.getString("blindUserId"),
                volunteerUserId = rs.getString("volunteerUserId"),
                area = rs.getString("area"),
                detailAddress = rs.getString("detailAddress") ?: "",
                latitude = rs.getDouble("latitude"),
                longitude = rs.getDouble("longitude"),
                remark = rs.getString("remark"),
                status = rs.getInt("status"),
                createTime = rs.getString("createTime")
            ))
        }
        rs.close()
        conn.close()
        return list
    }

    fun createReserve(reserve: Reserve): Reserve {
        val conn = ds.connection
        val ps = conn.prepareStatement(
            "MERGE INTO reserves (reserveId, blindUserId, volunteerUserId, area, detailAddress, latitude, longitude, remark, status, createTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )
        ps.setString(1, reserve.reserveId)
        ps.setString(2, reserve.blindUserId)
        ps.setString(3, reserve.volunteerUserId)
        ps.setString(4, reserve.area)
        ps.setString(5, reserve.detailAddress)
        ps.setDouble(6, reserve.latitude)
        ps.setDouble(7, reserve.longitude)
        ps.setString(8, reserve.remark)
        ps.setInt(9, reserve.status)
        ps.setString(10, reserve.createTime)
        ps.executeUpdate()
        ps.close()
        conn.close()
        return reserve
    }
}
