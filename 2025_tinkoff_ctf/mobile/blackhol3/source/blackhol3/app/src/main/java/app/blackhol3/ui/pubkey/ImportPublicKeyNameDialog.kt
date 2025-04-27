package app.blackhol3.ui.pubkey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun ImportPublicKeyNameDialog(
    keyName: String,
    onKeyNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Назовите чат") },
        text = {
            Column {
                OutlinedTextField(
                    value = keyName,
                    onValueChange = onKeyNameChange,
                    modifier =
                        Modifier.Companion
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSaveClick) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
