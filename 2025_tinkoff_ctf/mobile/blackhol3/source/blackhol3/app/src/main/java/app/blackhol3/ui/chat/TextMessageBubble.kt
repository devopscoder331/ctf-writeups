package app.blackhol3.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.blackhol3.model.Message

@Composable
fun TextMessageBubble(
    message: Message,
    isIncoming: Boolean,
    onResendClick: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isIncoming) {
            Box(modifier = Modifier.weight(1f, fill = false)) {
                MessageBubble(message = message, isIncoming = isIncoming)
            }

            MessageMeta(
                timestamp = message.timestamp,
                isIncoming = isIncoming,
                deliveryStatus = message.deliveryStatus,
                modifier = Modifier.padding(horizontal = 6.dp),
                onResendClick = { },
            )
        } else {
            MessageMeta(
                timestamp = message.timestamp,
                isIncoming = isIncoming,
                deliveryStatus = message.deliveryStatus,
                modifier = Modifier.padding(horizontal = 6.dp),
                onResendClick = { onResendClick(message.id) },
            )

            Box(modifier = Modifier.weight(1f, fill = false)) {
                MessageBubble(message = message, isIncoming = isIncoming)
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isIncoming: Boolean,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (!isIncoming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
        )
    }
}
