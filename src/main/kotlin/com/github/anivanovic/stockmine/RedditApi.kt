package com.github.anivanovic.stockmine;

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement


class RedditApi(
    private val redditAuth: RedditAuth
) {
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private val oauthURI = "https://oauth.reddit.com"
    private val wwwURI = "https://www.reddit.com"

    suspend fun listSubredditNew(subreddit: String, after: String?, limit: Int, count: Int?): RedditData<RedditListing<RedditPost>> {
        val newPostsUrl = "$oauthURI/r/$subreddit/new"
        return client.get(newPostsUrl) {
            if (count != 0) {
                parameter("count", count.toString())
            }
            if (after != null) {
                parameter("after", after)
            }
            parameter("limit", limit.toString())
            parameter("show", "all")

            appendAuthHeader(headers)
        }
    }

    suspend fun listPostComments(subreddit: String, postId: String): RedditData<RedditListing<RedditComment>> {
        val postCommentsUrl = "$wwwURI/r/$subreddit/comments/$postId.json?raw_json=1"
        val listing: JsonArray = client.get(postCommentsUrl) {
            parameter("sort", "new")
        }
        val comment = listing[1]
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
        return json.decodeFromJsonElement(comment)
    }

    private fun appendAuthHeader(headers: HeadersBuilder) {
        headers.append("Authorization", "bearer ${redditAuth.accessToken}")
    }
}
