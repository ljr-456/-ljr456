package com.example.server.routes

import com.example.server.model.Reserve
import com.example.server.repository.ReserveRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reserveRoutes(reserveRepo: ReserveRepository) {
    route("/posts") {
        get {
            call.respond(reserveRepo.getAllReserves())
        }
        post {
            val reserve = call.receive<Reserve>()
            call.respond(reserveRepo.createReserve(reserve))
        }
    }
}
