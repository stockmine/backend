package com.github.anivanovic.stockmine

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

class RedditAuth(private val clientId: String, private val secret: String) {
    private val REDDIT_AUTHORIZE = "https://www.reddit.com/api/v1/authorize"
    private val REDDIT_ACCESS_TOKEN = "https://www.reddit.com/api/v1/access_token"

    private val client = HttpClient(CIO)
    private val redirectUri = "https://backend-lf3ty7mjqq-ew.a.run.app/reddit"

    private var refreshToken: String? = null
    var accessToken: String? = null
    private var tokenExpireAt: Instant? = null


    suspend fun auth(call: ApplicationCall) {
        val ub = URLBuilder(REDDIT_AUTHORIZE)
        ub.parameters.append("client_id", clientId)
        ub.parameters.append("response_type", "code")
        ub.parameters.append("state", UUID.randomUUID().toString())
        ub.parameters.append("redirect_uri", redirectUri)
        ub.parameters.append("duration", "permanent")
        ub.parameters.append("scope", "read")
        call.respondRedirect(ub.buildString(), false)
    }

    suspend fun getAccessToken(call: ApplicationCall) {
        val res: HttpResponse = client.post(REDDIT_ACCESS_TOKEN) {
            parameter("grant_type", "authorization_code")
            parameter("code", call.request.queryParameters["code"])
            parameter("redirect_uri", redirectUri)
            appendAuthHeader(headers)
        }
        val now = Instant.now()
        val response: RedditAccessTokenResponse = res.receive()
        accessToken = response.accessToken
        refreshToken = response.refreshToken
        tokenExpireAt = now.plusSeconds(response.expiresIn)
        call.respond(response)
    }

    suspend fun refreshToken(call: ApplicationCall) {
        val res: HttpResponse = client.post(REDDIT_ACCESS_TOKEN) {
            body = "grant_type=refresh_token&refresh_token=$refreshToken"
            appendAuthHeader(headers)
        }
        val now = Instant.now()
        val response: RedditRefreshTokenResponse = res.receive()
        accessToken = response.accessToken
        tokenExpireAt = now.plusSeconds(response.expiresIn)
        call.respond(response)
    }

    private fun appendAuthHeader(headers: HeadersBuilder) {
        val cred = ("$clientId:$secret").toByteArray()
        val auth = Base64.getEncoder().encode(cred).toString(Charsets.UTF_8)
        headers.append("Authorization", "Basic $auth")
    }
}

@Serializable
data class RedditAccessTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class RedditRefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    val scope: String,
)

class RedditApi(
    private val redditAuth: RedditAuth
) {
    private val client = HttpClient(CIO)
    private val redditHost = "www.reddit.com"

    suspend fun listSubredditNew(subreddit: String, offset: Int): String {
        val call: HttpResponse = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = redditHost
                path("api", subreddit, "new")
                if (offset != 0) {
                    parameters.append("count", offset.toString())
                }
                parameters.append("limit", "100")
                parameters.append("show", "all")

                appendAuthHeader(headers)
            }
            build()
        }
        return call.receive()
    }

    private fun appendAuthHeader(headers: HeadersBuilder) {
       headers.append("Authorization", "Bearer ${redditAuth.accessToken}")
    }

}