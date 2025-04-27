package app.blackhol3.ui.chatlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.blackhol3.data.local.model.Chat

@Composable
internal fun Chats(
    chats: List<Chat>,
    viewModel: ChatListViewModel,
    onChatSelected: (id: String) -> Unit,
) {
    var selectedChatId by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        itemsIndexed(
            items = chats,
        ) { index, chat ->
            ChatItem(
                chat = chat,
                viewModel = viewModel,
                onClick = { onChatSelected(chat.id) },
                isSelected = selectedChatId == chat.id,
                onLongPress = { chatId ->
                    selectedChatId = if (selectedChatId == chatId) "" else chatId
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatItem(
    viewModel: ChatListViewModel,
    chat: Chat,
    onClick: () -> Unit,
    isSelected: Boolean,
    onLongPress: (String) -> Unit,
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val actionsVisibilityState = remember { MutableTransitionState(false) }
    val message by viewModel.latestMessageFeed(chat.id).collectAsStateWithLifecycle(null)

    LaunchedEffect(isSelected) {
        actionsVisibilityState.targetState = isSelected
    }

    val actionsSlideInSpec = tween<IntOffset>(durationMillis = 200)
    val backgroundColorSpec = tween<Color>(durationMillis = 300)

    val backgroundColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = backgroundColorSpec,
        label = "backgroundColorAnimation",
    )

    val msg = message
    val (lastMsg, lastMsgSvc) =
        if (msg == null) {
            "Нет сообщений" to true
        } else if (msg.media != null) {
            "Медиа: ${msg.media.mimeType}" to true
        } else {
            msg.content to false
        }

    Column {
        Box(
            modifier =
                Modifier.Companion
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { onLongPress(chat.id) },
                    ).background(color = backgroundColor),
        ) {
            ListItem(
                colors =
                    ListItemDefaults.colors(
                        containerColor = Color.Companion.Transparent,
                    ),
                headlineContent = {
                    Text(
                        text =
                            chat.name ?: chat.pubKey.fingerprint
                                .chunked(2)
                                .joinToString(":"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Companion.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                    )
                },
                supportingContent = {
                    Text(
                        text = lastMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                        fontStyle = if (lastMsgSvc) FontStyle.Italic else FontStyle.Normal,
                    )
                },
                leadingContent = {
                    Box(
                        modifier =
                            Modifier.Companion
                                .size(48.dp),
                    ) {
                        Image(
                            bitmap = chat.pubKey.keyPic.asImageBitmap(),
                            contentDescription = "Аватар",
                            modifier = Modifier.Companion.fillMaxSize(),
                        )
                    }
                },
                trailingContent = {
                    this@Column.AnimatedVisibility(
                        visibleState = actionsVisibilityState,
                        enter =
                            slideIn(
                                initialOffset = { IntOffset(it.width, 0) },
                                animationSpec = actionsSlideInSpec,
                            ),
                        exit =
                            slideOut(
                                targetOffset = { IntOffset(it.width, 0) },
                                animationSpec = actionsSlideInSpec,
                            ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.Companion.CenterVertically,
                            modifier =
                                Modifier.Companion
                                    .height(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(28.dp),
                                    ),
                        ) {
                            HorizontalDivider(
                                modifier =
                                    Modifier.Companion
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .padding(vertical = 8.dp),
                                color = Color.Companion.White.copy(alpha = 0.3f),
                            )

                            IconButton(
                                onClick = {
                                    showRenameDialog = true
                                    onLongPress("")
                                },
                                modifier = Modifier.Companion.fillMaxHeight(),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Переименовать",
                                    tint = Color.Companion.White,
                                )
                            }

                            HorizontalDivider(
                                modifier =
                                    Modifier.Companion
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            )

                            IconButton(
                                onClick = {
                                    showDeleteDialog = true
                                    onLongPress("")
                                },
                                modifier = Modifier.Companion.fillMaxHeight(),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Удалить",
                                    tint = Color.Companion.White,
                                )
                            }
                        }
                    }
                },
            )
        }

        Box(
            modifier = Modifier.Companion.fillMaxWidth(),
            contentAlignment = Alignment.Companion.Center,
        ) {
            HorizontalDivider(
                modifier =
                    Modifier.Companion
                        .fillMaxWidth(0.9f)
                        .padding(start = 24.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }

    if (showRenameDialog) {
        RenameDialog(
            initialName = chat.name ?: "",
            onConfirm = { newName ->
                viewModel.renameChat(chat.id, newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            chatName = chat.name ?: "Без имени",
            onConfirm = {
                viewModel.deleteChat(chat.id)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
