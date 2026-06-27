package com.example.server.routes

import com.example.server.model.RunRecord
import com.example.server.repository.RunRecordRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.runRecordRoutes(recordRepo: RunRecordRepository) {
    route("/comments") {
        get {
            call.respond(recordRepo.getAllRunRecords())
        }
        post {
            val record = call.receive<RunRecord>()
            call.respond(recordRepo.createRunRecord(record))
        }
    }
}
