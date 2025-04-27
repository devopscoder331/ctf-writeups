package whatis.love.agedate.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.SecurityUpdateWarning
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.MyProfile
import whatis.love.agedate.api.model.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportViolationBottomSheet(
    onDismiss: () -> Unit,
    onPrintReport: (myProfile: MyProfile, profile: Profile, violationType: String) -> Unit,
    profile: Profile,
    myProfile: MyProfile,
    token: String,
) {
    val modalBottomSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    val scope = rememberCoroutineScope()
    var selectedViolationType by remember { mutableStateOf<String?>(null) }
    var isDialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        modalBottomSheetState.expand()
    }
    if (isDialogVisible && selectedViolationType != null) {
        ReportPrintConfirmationDialog(
            onDismiss = {
                isDialogVisible = false
                selectedViolationType = null
                onDismiss()
            },
            onPrintClick = { myProfileArg, profileArg, violationTypeArg ->
                onPrintReport(myProfileArg, profileArg, violationTypeArg)
                isDialogVisible = false
                selectedViolationType = null
                onDismiss()
            },
            violationType = selectedViolationType!!,
            profile = profile,
            myProfile = myProfile,
            token = token,
        )
    }
    val violationTypes =
        listOf(
            "reason-1" to "Ненормативная лексика",
            "reason-2" to "Нарушение авторских прав",
            "reason-3" to "Ложная информация",
            "reason-4" to "Оскорбление пользователей",
            "reason-5" to "Спам и реклама",
            "reason-6" to "Мошенничество",
            "reason-7" to "Угрозы и запугивание",
            "reason-8" to "Пропаганда насилия",
            "reason-9" to "Нарушение конфиденциальности",
        )
    if (!isDialogVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = modalBottomSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.8f),
            ) {
                Text(
                    text = "Сообщить о нарушении",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )

                Divider()

                Text(
                    text = "Выберите причину жалобы:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    items(violationTypes.size) { index ->
                        val (type, description) = violationTypes[index]
                        val icon =
                            when (type) {
                                "reason-1" -> Icons.Default.Block
                                "reason-2" -> Icons.Default.CopyAll
                                "reason-3" -> Icons.Default.Info
                                "reason-4" -> Icons.Default.SentimentVeryDissatisfied
                                "reason-5" -> Icons.Default.MarkEmailUnread
                                "reason-6" -> Icons.Default.SecurityUpdateWarning
                                "reason-7" -> Icons.Default.Dangerous
                                "reason-8" -> Icons.Default.Visibility
                                "reason-9" -> Icons.Default.Lock
                                else -> Icons.Default.Report
                            }

                        ReportViolationItem(
                            description = description,
                            icon = icon,
                            onClick = {
                                selectedViolationType = type
                                scope.launch {
                                    modalBottomSheetState.hide()
                                    isDialogVisible = true
                                }
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        scope.launch {
                            modalBottomSheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}

@Composable
private fun ReportViolationItem(
    description: String,
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Report,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
