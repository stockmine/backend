package com.github.anivanovic.stockmine

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

var REDDIT_AUTH = "https://www.reddit.com/api/v1/access_token"
val client = HttpClient(CIO)

@JvmOverloads
fun Application.module(testing: Boolean = false, development: Boolean = false) {
    routing {
        get("/") {
            call.respond("Hello from me")
        }
        static("/static") {
            resources("files")
        }

        get("/reddit-auth") {
            client.request(REDDIT_AUTH) {
                method = HttpMethod.Get
                parameter("client_id", "tiL3UaYvQqnzkiex7pRciQ")
                parameter("response_type", "code")
                parameter("state", UUID.randomUUID())
                parameter("redirect_uri", "https://backend-lf3ty7mjqq-ew.a.run.app/reddit")
                parameter("duration", "permanent")
                parameter("scope", "read")
            }
        }
        get("/reddit") {
            call.respond("got reddit code " + call.request.queryParameters["code"])
        }
    }
}