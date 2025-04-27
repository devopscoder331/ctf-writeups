package app.blackhol3.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.blackhol3.model.Media
import app.blackhol3.model.Message

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessageList(
    messagesByDate: List<Pair<String, List<Message>>>,
    listState: LazyListState,
    onImageClick: (Media) -> Unit,
    onFileClick: (Media) -> Unit,
    onResendClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
    isRefreshing: Boolean = false,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            messagesByDate.reversed().forEach { (date, messagesForDate) ->
                items(
                    items = messagesForDate.reversed(),
                    key = { it.id },
                ) { message ->
                    MessageItem(
                        message = message,
                        onImageClick = onImageClick,
                        onFileClick = onFileClick,
                        onResendClick = onResendClick,
                    )
                }

                item(key = "date-$date") {
                    Modifier.Companion
                        .padding(vertical = 16.dp)
                    DateSeparator(
                        date = date,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}
