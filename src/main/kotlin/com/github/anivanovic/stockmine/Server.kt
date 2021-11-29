package com.github.anivanovic.stockmine

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.netty.*
import io.ktor.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

private val redditAuth = RedditAuth("tiL3UaYvQqnzkiex7pRciQ", System.getenv("REDDIT_SECRET"))

@JvmOverloads
fun Application.module(testing: Boolean = false, development: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/") {
            call.respond("Hello from me")
        }
        static("/static") {
            resources("files")
        }
        get("/test") {
            val res = RedditAccessTokenResponse(
                "token", "grant", 12, "ref_token", "scope"
            )
            call.respond(res)
        }
        get("/reddit-auth") {
            redditAuth.auth(call)
        }
        get("/reddit") {
            redditAuth.getAccessToken(call)
        }
        get("/reddit-refresh") {
            redditAuth.refreshToken(call)
        }
        get("/reddit-list") {
            val redditApi = RedditApi(redditAuth)
            val response = redditApi.listSubredditNew(
                call.parameters["subreddit"] ?: "stocks",
                call.parameters["after"],
                call.parameters["limit"]?.toInt() ?: 100,
                call.parameters["count"]?.toInt() ?: 0,
            )
            call.respond(response)
        }
        get("/reddit-comments") {
            val redditApi = RedditApi(redditAuth)
            val response = redditApi.listPostComments(
                call.parameters["subreddit"] ?: "stocks",
                call.parameters.getOrFail("id"),
            )
            call.respond(response)
        }
    }
}