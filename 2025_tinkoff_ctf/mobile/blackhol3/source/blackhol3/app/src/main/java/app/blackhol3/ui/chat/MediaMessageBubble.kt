package app.blackhol3.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Slideshow
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.blackhol3.model.Media
import app.blackhol3.model.Message
import app.blackhol3.util.toBitmap
import java.util.Locale

@Composable
fun MediaMessageBubble(
    message: Message,
    isIncoming: Boolean,
    onImageClick: (Media) -> Unit,
    onFileClick: (Media) -> Unit,
    onResendClick: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isIncoming) {
            Box(modifier = Modifier.weight(1f, fill = false)) {
                MessageBubble(
                    message = message,
                    isIncoming = isIncoming,
                    onImageClick = onImageClick,
                    onFileClick = onFileClick,
                )
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
                MessageBubble(
                    message = message,
                    isIncoming = isIncoming,
                    onImageClick = onImageClick,
                    onFileClick = onFileClick,
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isIncoming: Boolean,
    modifier: Modifier = Modifier,
    onImageClick: (Media) -> Unit,
    onFileClick: (Media) -> Unit,
) {
    val media = message.media!!
    val isImage = media.mimeType.startsWith("image/") && media.content != null
    Card(
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = if (!isIncoming) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column {
            if (isImage) {
                Box(
                    modifier =
                        Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            .clickable { onImageClick(media) },
                ) {
                    Image(
                        bitmap = media.content.toBitmap().asImageBitmap(),
                        contentDescription = "Изображение",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .size(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onFileClick(media) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getMediaTypeIcon(media.mimeType),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = media.mimeType,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = formatFileSizeRussian(media.sizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getMediaTypeIcon(mimeType: String): ImageVector =
    when {
        mimeType.startsWith("image/") -> Icons.Outlined.Image
        mimeType.startsWith("audio/") -> Icons.Outlined.AudioFile
        mimeType.startsWith("video/") -> Icons.Outlined.VideoFile
        mimeType.startsWith("text/") -> Icons.AutoMirrored.Outlined.TextSnippet
        mimeType.startsWith("application/pdf") -> Icons.Outlined.PictureAsPdf
        mimeType.startsWith("application/vnd.ms-excel") ||
            mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml") ->
            Icons.Outlined.TableChart

        mimeType.startsWith("application/vnd.ms-powerpoint") ||
            mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml") ->
            Icons.Outlined.Slideshow

        mimeType.startsWith("application/msword") ||
            mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml") ->
            Icons.Outlined.Description

        mimeType.startsWith("application/zip") ||
            mimeType.startsWith("application/x-rar-compressed") ||
            mimeType.startsWith("application/x-7z-compressed") ->
            Icons.Outlined.FolderZip

        else -> Icons.AutoMirrored.Outlined.InsertDriveFile
    }

private fun formatFileSizeRussian(sizeBytes: Long): String {
    val locale = Locale("ru")

    return when {
        sizeBytes < 1024 ->
            buildString {
                append(sizeBytes)
                append(" ")
                append(pluralizeBytes(sizeBytes))
            }

        sizeBytes < 1_048_576 -> {
            val kb = sizeBytes / 1024.0
            String.format(locale, "%.1f КБ", kb)
        }

        sizeBytes < 1_073_741_824 -> {
            val mb = sizeBytes / 1_048_576.0
            String.format(locale, "%.1f МБ", mb)
        }

        else -> {
            val gb = sizeBytes / 1_073_741_824.0
            String.format(locale, "%.2f ГБ", gb)
        }
    }
}

private fun pluralizeBytes(count: Long): String =
    when {
        count % 10 == 1L && count % 100 != 11L -> "байт"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "байта"
        else -> "байт"
    }
