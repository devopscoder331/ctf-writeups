package app.blackhol3.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ServerMessage(
    val sender: String,
    val message: String,
)

@Serializable
data class ServerMessages(
    val messages: List<ServerMessage>,
)
