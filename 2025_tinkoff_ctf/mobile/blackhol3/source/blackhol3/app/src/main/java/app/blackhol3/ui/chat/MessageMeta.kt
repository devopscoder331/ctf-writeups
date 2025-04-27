package app.blackhol3.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.blackhol3.model.DeliveryStatus
import app.blackhol3.util.toMessengerFormattedTime

private val messageMetaIconSize = 12.dp

@Composable
fun MessageMeta(
    timestamp: Long,
    isIncoming: Boolean,
    deliveryStatus: DeliveryStatus,
    modifier: Modifier,
    onResendClick: () -> Unit,
) {
    Column(
        horizontalAlignment = if (isIncoming) Alignment.Start else Alignment.End,
        modifier = modifier,
    ) {
        when (deliveryStatus) {
            DeliveryStatus.SENT -> {
                Icon(
                    imageVector = Icons.Rounded.Pending,
                    contentDescription = "Отправлено",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(messageMetaIconSize),
                )
            }

            DeliveryStatus.DELIVERED -> {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Получено сервером",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(messageMetaIconSize),
                )
            }

            DeliveryStatus.FAILED -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onResendClick() },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Error,
                        contentDescription = "Ошибка",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(messageMetaIconSize),
                    )
                }
            }

            DeliveryStatus.INCOMING -> {}
        }

        Text(
            text = timestamp.toMessengerFormattedTime(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
