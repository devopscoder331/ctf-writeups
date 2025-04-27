package app.blackhol3.ui.chat

import androidx.compose.runtime.Composable
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.model.Media
import app.blackhol3.model.Message

@Composable
fun MessageItem(
    message: Message,
    onImageClick: (Media) -> Unit,
    onFileClick: (Media) -> Unit,
    onResendClick: (String) -> Unit,
) {
    val isIncoming = message.deliveryStatus == DeliveryStatus.INCOMING
    if (message.media != null) {
        MediaMessageBubble(
            message = message,
            isIncoming = isIncoming,
            onImageClick = onImageClick,
            onFileClick = onFileClick,
            onResendClick = onResendClick,
        )
    } else {
        TextMessageBubble(
            message = message,
            isIncoming = isIncoming,
            onResendClick = onResendClick,
        )
    }
}
