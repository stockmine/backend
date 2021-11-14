package com.github.anivanovic.stockmine

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

const val REDDIT_AUTORIZE = "https://www.reddit.com/api/v1/authorize"
const val REDDIT_AUTH = "https://www.reddit.com/api/v1/access_token"
val client = HttpClient(CIO)

private val redirect_uri = "https://backend-lf3ty7mjqq-ew.a.run.app/reddit"

private val clientId = "tiL3UaYvQqnzkiex7pRciQ"

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
            val ub = URLBuilder(REDDIT_AUTORIZE)
            ub.parameters.append("client_id", clientId)
            ub.parameters.append("response_type", "code")
            ub.parameters.append("state", UUID.randomUUID().toString())
            ub.parameters.append("redirect_uri", redirect_uri)
            ub.parameters.append("duration", "permanent")
            ub.parameters.append("scope", "read")
            call.respondRedirect(ub.buildString(), false)
        }
        get("/reddit") {
            val cred = (clientId + ":" + System.getenv("REDDIT_SECRET")).toByteArray()
            val auth = Base64.getEncoder().encode(cred).toString(Charsets.UTF_8)
            val res: HttpResponse = client.post(REDDIT_AUTH) {
                parameter("grant_type", "authorization_code")
                parameter("code", call.request.queryParameters["code"])
                parameter("redirect_uri", redirect_uri)
                headers.append("Authorization", "Basic $auth")
            }
            val body: String = res.receive()
            call.respondText(body, ContentType.Application.Json)
        }
    }
}