package app.blackhol3.ui.pubkey

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun InvalidFileDialog(
    onDismiss: () -> Unit,
    onSelectAnother: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Недействительный файл") },
        text = { Text("Выбранный файл не содержит действительный RSA публичный ключ. Хотите выбрать другой файл?") },
        confirmButton = {
            TextButton(onClick = onSelectAnother) {
                Text("Выбрать другой")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
