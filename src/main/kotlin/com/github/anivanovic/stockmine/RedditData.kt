package com.github.anivanovic.stockmine


import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable(with = KindSerializer::class)
enum class Kind(val type: String) {
    NULL(""),
    LISTING("Listing"),
    COMMENT("t1"),
    USER("t2"),
    POST("t3"),
    SUBREDDIT("t5");

    companion object {
        fun byKind(type: String): Kind {
            return values().first { it.type == type }
        }
    }
}

@Serializer(forClass = Kind::class)
object KindSerializer {
    override fun serialize(encoder: Encoder, value: Kind) {
        if (value == Kind.NULL) {
            encoder.encodeNull()
            return
        }

        encoder.encodeString(value.type)
    }

    override fun deserialize(decoder: Decoder): Kind {
        return Kind.byKind(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("kind", PrimitiveKind.STRING)

}

@Serializable
data class RedditData<T>(
    val kind: Kind,
    val data: T?,
)

class RedditDataSerializer<T>(dataSerializer: KSerializer<T>) : KSerializer<RedditData<T>> {
    private val serializer = RedditData.serializer(dataSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: RedditData<T>) {
        if (value.kind == Kind.NULL) {
            encoder.encodeNull()
            return
        }

        serializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): RedditData<T> {
        return try {
            serializer.deserialize(decoder)
        } catch (e: Exception) {
            RedditData(Kind.NULL, null)
        }
    }
}

@Serializable
data class RedditListing<T>(
    val after: String?,
    val before: String?,
    val dist: Int?,
    val modhash: String?,
    @SerialName("geo_filter") val geoFilter: String,
    val children: List<RedditData<T>>,
)

@Serializable
data class RedditPost(
    val subreddit: String,
    val selftext: String,
    val title: String,
    @SerialName("subreddit_id") val subredditId: String,
    val id: String,
    @SerialName("author_fullname") val authorFullName: String,
    val author: String,
    @SerialName("num_comments") val numComments: Int,
    @SerialName("created_utc") val createdAt: Double,
)

@Serializable
data class RedditComment(
    val id: String?,
    @SerialName("total_awards_received") val awardsReceived: Int?,
    @SerialName("author") val authorName: String,
    @SerialName("parent_id") val parentId: String?,
    @SerialName("author_fullname") val authorId: String,
    @SerialName("is_submitter") val isSubmitter: Boolean,
    val score: Int,
    val likes: Int?,
    val downs: Int,
    val body: String,
    val permalink: String,
    @SerialName("created_utc") val created: Double,
    @Serializable(with = RedditDataSerializer::class) val replies: RedditData<RedditListing<RedditComment>>?
)