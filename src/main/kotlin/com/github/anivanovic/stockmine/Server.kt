package com.github.anivanovic.stockmine

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import java.util.function.Function

fun main(args: Array<String>): Unit = EngineMain.main(args)

@JvmOverloads
fun Application.module(testing: Boolean = false, development: Boolean = false) {
//    install(Sessions) {
//        cookie<UserIdPrincipal>("auth", SessionStorageMemory()) {
//            cookie.path = "/"
//            cookie.extensions["SameSite"] = "lax"
//        }
//    }
//    install(Authentication) {
//        form("form") {
//            userParamName = "uname"
//            passwordParamName = "psw"
//            validate { cred ->
//                if (cred.name == "antonije" && cred.password == "admin123") {
//                    UserIdPrincipal(cred.name)
//                } else {
//                    null
//                }
//            }
//            challenge {
//                val errors = call.authentication.allFailures;
//                when (errors.firstOrNull()) {
//                    AuthenticationFailedCause.InvalidCredentials -> call.respondRedirect("/login?invalid")
//                    AuthenticationFailedCause.NoCredentials -> call.respondRedirect("/login?no")
//                    else -> call.respondRedirect("/login")
//                }
//            }
//        }
//        session<UserIdPrincipal>("session") {
//            validate { session -> session }
//            challenge {
//                call.respondRedirect("/login?no")
//            }
//        }s
//    }
    routing {
//        authenticate("session") {
            get("/") {
//                val principal = call.principal<UserIdPrincipal>()
//                if (principal == null) {
//                    call.respondRedirect("/login");
//                } else {
                    call.respondText("Hello from kako ovo volim brajo moj!")
//                }
            }
//            static("/static") {
//                resources("files")
//            }
            static("/files") {
                resources("dist");
            }
//        }
//        authenticate("form") {
//            post("/login") {
//                val principal = call.principal<UserIdPrincipal>()!!
//                call.sessions.set(principal)
//                call.respondRedirect("/")
//            }
//        }
    }
}