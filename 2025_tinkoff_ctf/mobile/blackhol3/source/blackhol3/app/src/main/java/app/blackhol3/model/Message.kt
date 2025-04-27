package app.blackhol3.model

data class Message(
    val id: String,
    val chatId: String,
    val seq: Long = -1,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
    val content: String,
    val timestamp: Long,
    val media: Media? = null,
    val mediaRef: String? = null,
)
