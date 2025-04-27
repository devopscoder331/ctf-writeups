package app.blackhol3.ui.pubkey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KeyActionButtons(
    onResetClick: () -> Unit,
    onAddClick: () -> Unit,
) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Button(onClick = onResetClick, modifier = Modifier.Companion.weight(0.7f)) {
            Text("Сбросить")
        }

        Spacer(modifier = Modifier.Companion.weight(0.1f))

        Button(onClick = onAddClick, modifier = Modifier.Companion.weight(1f)) {
            Text("Добавить ключ")
        }
    }
}
