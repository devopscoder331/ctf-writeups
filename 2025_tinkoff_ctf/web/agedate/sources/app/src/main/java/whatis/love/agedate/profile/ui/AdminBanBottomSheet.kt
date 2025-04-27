package whatis.love.agedate.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import whatis.love.agedate.api.model.AdminVisibleProfileFields
import whatis.love.agedate.api.model.BanAction
import whatis.love.agedate.api.requests.SetBanRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBanBottomSheet(
    onDismiss: () -> Unit,
    onBanActionSubmit: (SetBanRequest) -> Unit,
    adminProfile: AdminVisibleProfileFields? = null,
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    var selectedAction by remember {
        mutableStateOf(
            if (adminProfile?.isBanned == true) BanAction.BAN else BanAction.UNBAN,
        )
    }
    var banDuration by remember { mutableStateOf("1") }
    var banReason by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var processResult by remember { mutableStateOf<ProcessResult?>(null) }
    val durationInMs =
        try {
            banDuration.toInt() * 24 * 60 * 60 * 1000L
        } catch (_: NumberFormatException) {
            0L
        }
    val banDurationOptions = listOf("1", "3", "7", "14", "30", "90", "365")
    var expandedDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isProcessing) onDismiss()
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "Управление доступом пользователя",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (adminProfile != null) {
                Text(
                    text = "Пользователь: ${adminProfile.username}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = "Текущий статус: ${if (adminProfile.isBanned) "Заблокирован" else "Не заблокирован"}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                if (adminProfile.isBanned && adminProfile.banExpires != null) {
                    Text(
                        text = "Блокировка истекает: ${formatDate(adminProfile.banExpires!!)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (!adminProfile.banReason.isNullOrEmpty()) {
                    Text(
                        text = "Причина: ${adminProfile.banReason}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (!adminProfile.bannedBy.isNullOrEmpty()) {
                    Text(
                        text = "Кем заблокирован: ${adminProfile.bannedBy}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = "Выберите действие:",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.selectableGroup(),
            ) {
                listOf(
                    BanAction.BAN to "Заблокировать",
                    BanAction.UNBAN to "Разблокировать",
                ).forEach { (action, label) ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedAction == action,
                                    onClick = { selectedAction = action },
                                    role = Role.RadioButton,
                                ).padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedAction == action,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (selectedAction == BanAction.BAN) {
                Text(
                    text = "Срок блокировки (дней):",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                ) {
                    OutlinedTextField(
                        value = banDuration,
                        onValueChange = { banDuration = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Дней") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                    ) {
                        banDurationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("$option дней") },
                                onClick = {
                                    banDuration = option
                                    expandedDropdown = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = "Причина ${if (selectedAction == BanAction.BAN) "блокировки" else "разблокировки"}:",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = banReason,
                onValueChange = { banReason = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Введите причину") },
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { showConfirmationDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled =
                    banReason.isNotBlank() &&
                        !isProcessing &&
                        (selectedAction != BanAction.BAN || durationInMs > 0),
            ) {
                Text(
                    text =
                        when (selectedAction) {
                            BanAction.BAN -> "Заблокировать пользователя"
                            BanAction.UNBAN -> "Разблокировать пользователя"
                        },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    if (showConfirmationDialog) {
        ConfirmationDialog(
            onDismiss = { showConfirmationDialog = false },
            onConfirm = { regNo ->
                showConfirmationDialog = false
                isProcessing = true

                val request =
                    SetBanRequest(
                        reportRegNo = regNo,
                        state = selectedAction,
                        comment = banReason,
                        expires = if (selectedAction == BanAction.BAN) System.currentTimeMillis() + durationInMs else null,
                    )
                onBanActionSubmit(request)
            },
        )
    }
    processResult?.let { result ->
        ResultDialog(
            result = result,
            onDismiss = {
                processResult = null
                onDismiss()
            },
        )
    }
}

@Composable
private fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var regNumber by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Подтверждение действия") },
        text = {
            Column {
                Text("Для подтверждения введите регистрационный номер документа")

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text("Рег. номер") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(regNumber) },
                enabled = regNumber.isNotBlank(),
            ) {
                Text("Подтвердить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}

@Composable
fun ResultDialog(
    result: ProcessResult,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    when (result) {
                        is ProcessResult.Success -> "Операция выполнена"
                        is ProcessResult.Error -> "Ошибка"
                    },
            )
        },
        text = {
            Text(
                text =
                    when (result) {
                        is ProcessResult.Success -> "Действие выполнено успешно."
                        is ProcessResult.Error -> "Произошла ошибка: ${result.message}"
                    },
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}

sealed class ProcessResult {
    object Success : ProcessResult()

    data class Error(
        val message: String,
    ) : ProcessResult()
}

private fun formatDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale("ru", "RU"))
    return dateFormat.format(date)
}
