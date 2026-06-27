package com.example.server.routes

import com.example.server.model.User
import com.example.server.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userRepo: UserRepository) {
    route("/users") {
        get {
            call.respond(userRepo.getAllUsers())
        }
        get("/{id}") {
            val id = call.parameters["id"] ?: ""
            val user = userRepo.getUserById(id)
            if (user != null) call.respond(user)
            else call.respondText("User not found", status = HttpStatusCode.NotFound)
        }
        post {
            val user = call.receive<User>()
            call.respond(userRepo.createUser(user))
        }
        post("/login") {
            val body = call.receive<Map<String, String>>()
            val userId = body["userId"] ?: ""
            val password = body["password"] ?: ""
            val user = userRepo.login(userId, password)
            if (user != null) call.respond(user)
            else call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }
}
