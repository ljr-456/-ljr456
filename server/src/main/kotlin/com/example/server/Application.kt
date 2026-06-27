package com.example.server

import com.example.server.repository.DatabaseFactory
import com.example.server.repository.ReserveRepository
import com.example.server.repository.RunRecordRepository
import com.example.server.repository.UserRepository
import com.example.server.routes.*
import com.google.gson.FieldNamingPolicy
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    DatabaseFactory.init()

    val ds = DatabaseFactory.dataSource
    val userRepo = UserRepository(ds)
    val reserveRepo = ReserveRepository(ds)
    val recordRepo = RunRecordRepository(ds)

    val port = (System.getenv("SERVER_PORT") ?: "8080").toInt()

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(ContentNegotiation) {
            gson {
                setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            }
        }
        install(CORS) {
            anyHost()
            allowHeader(io.ktor.http.HttpHeaders.ContentType)
            allowMethod(io.ktor.http.HttpMethod.Get)
            allowMethod(io.ktor.http.HttpMethod.Post)
        }
        routing {
            get("/") {
                call.respondText("BigWork Server is running")
            }
            userRoutes(userRepo)
            reserveRoutes(reserveRepo)
            runRecordRoutes(recordRepo)
        }
    }.start(wait = true)
}
