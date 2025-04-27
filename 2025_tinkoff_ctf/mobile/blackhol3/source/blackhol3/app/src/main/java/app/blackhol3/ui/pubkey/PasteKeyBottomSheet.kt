package app.blackhol3.ui.pubkey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasteKeyBottomSheet(
    pastedText: String,
    pastedTextError: String?,
    onTextChange: (String) -> Unit,
    onApplyClick: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Вставьте публичный ключ RSA в PEM-формате",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = pastedText,
                onValueChange = onTextChange,
                label = { Text("Публичный ключ") },
                modifier = Modifier.Companion.fillMaxWidth(),
                isError = pastedTextError != null,
                supportingText =
                    pastedTextError?.let {
                        { Text(it) }
                    },
                minLines = 5,
                maxLines = 10,
            )

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }

                TextButton(onClick = onApplyClick) {
                    Text("Применить")
                }
            }

            Spacer(modifier = Modifier.Companion.height(16.dp))
        }
    }
}
