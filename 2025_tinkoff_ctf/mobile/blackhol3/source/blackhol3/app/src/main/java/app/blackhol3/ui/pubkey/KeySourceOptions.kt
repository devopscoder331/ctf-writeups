package app.blackhol3.ui.pubkey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun KeySourceOptions(
    onFilePickClick: () -> Unit,
    onPasteClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Импортировать публичный ключ",
            style = MaterialTheme.typography.titleMedium,
        )

        Column(
            modifier = Modifier.Companion.fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier.Companion.fillMaxWidth(),
                onClick = onFilePickClick,
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = null,
                    modifier = Modifier.Companion.padding(end = 8.dp),
                )
                Text("Из файла")
            }

            Button(
                modifier = Modifier.Companion.fillMaxWidth(),
                onClick = onPasteClick,
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.Companion.padding(end = 8.dp),
                )
                Text("Вставить текст")
            }
        }
    }
}
