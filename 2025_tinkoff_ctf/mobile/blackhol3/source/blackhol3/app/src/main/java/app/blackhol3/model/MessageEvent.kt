package app.blackhol3.model

sealed class MessageEvent {
    data class Added(
        val message: Message,
    ) : MessageEvent()

    data class UpdatedDeliveryStatus(
        val messageId: String,
        val newStatus: DeliveryStatus,
    ) : MessageEvent()
}
