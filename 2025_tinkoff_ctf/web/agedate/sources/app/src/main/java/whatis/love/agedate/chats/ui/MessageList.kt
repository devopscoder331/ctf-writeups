package whatis.love.agedate.chats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import whatis.love.agedate.api.model.Message
import java.util.Calendar

@Composable
fun MessageList(
    messages: List<Message>,
    currentUserId: String,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    val sortedMessages =
        remember(messages) {
            messages.sortedBy { it.timestamp }
        }

    var shouldScrollToBottom by remember { mutableStateOf(false) }
    val firstLoadComplete = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (sortedMessages.isNotEmpty() && !firstLoadComplete.value) {
            listState.scrollToItem(index = sortedMessages.size - 1)
            firstLoadComplete.value = true
        }
    }

    LaunchedEffect(sortedMessages) {
        if (sortedMessages.isNotEmpty() && !firstLoadComplete.value) {
            listState.scrollToItem(index = sortedMessages.size - 1)
            firstLoadComplete.value = true
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (!isLoading && index == 0 && sortedMessages.isNotEmpty()) {
                    onLoadMore()
                }
            }
    }

    LaunchedEffect(sortedMessages.size) {
        if (sortedMessages.isNotEmpty() && firstLoadComplete.value) {
            val lastMessage = sortedMessages.last()
            if (lastMessage.senderId == currentUserId) {
                shouldScrollToBottom = true
            }
        }
    }

    LaunchedEffect(shouldScrollToBottom) {
        if (shouldScrollToBottom && sortedMessages.isNotEmpty()) {
            listState.animateScrollToItem(sortedMessages.size - 1)
            shouldScrollToBottom = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isLoading) {
                item(key = "loading-indicator") {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }

            var currentDate: Long? = null

            items(
                count = sortedMessages.size,
                key = { sortedMessages[it].id },
            ) { index ->
                val message = sortedMessages[index]

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = message.timestamp
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val messageDate = calendar.timeInMillis

                if (currentDate == null || messageDate != currentDate) {
                    DateDivider(timestamp = messageDate)
                    currentDate = messageDate
                }

                MessageItem(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId,
                )
            }
        }
    }
}
