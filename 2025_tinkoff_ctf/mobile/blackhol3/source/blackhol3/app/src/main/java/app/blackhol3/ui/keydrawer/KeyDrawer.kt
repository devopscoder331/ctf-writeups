package app.blackhol3.ui.keydrawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.blackhol3.R
import app.blackhol3.model.PrivateKey
import app.blackhol3.ui.common.PrivateKeyViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun KeyDrawer(
    viewModel: PrivateKeyViewModel = koinViewModel(),
    onKeyClicked: (id: String) -> Unit,
    onNewKeyClicked: () -> Unit,
    onKeyRegenerateClicked: (id: String) -> Unit,
) {
    val keys by viewModel.privateKeys.collectAsState()
    val currentKey by viewModel.currentPrivateKey.collectAsState()

    val context = LocalContext.current

    var showMenuForKey by remember { mutableStateOf<PrivateKey?>(null) }
    var keyToDelete by remember { mutableStateOf<PrivateKey?>(null) }
    var keyToShowFingerprint by remember { mutableStateOf<PrivateKey?>(null) }

    ModalDrawerSheet {
        Column {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.logo_name),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .height(36.dp),
            )

            HorizontalDivider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(
                        text = "Ключи",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                            ),
                    )
                }

                items(keys) { item ->
                    KeyListItem(
                        privateKey = item,
                        isSelected = item.id == currentKey?.id,
                        isActiveKey = item.id == currentKey?.id,
                        onClick = { onKeyClicked(item.id) },
                        onMoreClick = { showMenuForKey = item },
                        showMenu = showMenuForKey?.id == item.id,
                        onDismissMenu = { showMenuForKey = null },
                        onDeleteClick = {
                            keyToDelete = item
                            showMenuForKey = null
                        },
                        onRegenerateClick = {
                            onKeyRegenerateClicked(item.id)
                            showMenuForKey = null
                        },
                        onShowFingerprintClick = {
                            keyToShowFingerprint = item
                            showMenuForKey = null
                        },
                        onShareClick = {
                            viewModel.sharePublicKey(context, item)
                            showMenuForKey = null
                        },
                    )
                }

                item {
                    NavigationDrawerItem(
                        label = {
                            Text(
                                "Добавить новый",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        selected = false,
                        onClick = onNewKeyClicked,
                        icon = {
                            Box(
                                modifier = Modifier.size(52.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Добавить новый",
                                    modifier = Modifier.padding(4.dp),
                                )
                            }
                        },
                        shape = RectangleShape,
                    )
                }
            }
        }
    }

    keyToDelete?.let { key ->
        KeyDeleteConfirmationDialog(
            keyName = key.id,
            onConfirm = {
                viewModel.deletePrivateKey(key.id)
                keyToDelete = null
            },
            onDismiss = { keyToDelete = null },
        )
    }

    keyToShowFingerprint?.let { key ->
        KeyFingerprintDialog(
            key = key,
            onDismiss = { keyToShowFingerprint = null },
        )
    }
}

@Composable
private fun KeyListItem(
    privateKey: PrivateKey,
    isSelected: Boolean,
    isActiveKey: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onRegenerateClick: () -> Unit,
    onShowFingerprintClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationDrawerItem(
            modifier = Modifier.weight(1f),
            label = {
                Text(
                    privateKey.id,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            selected = false,
            onClick = onClick,
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(36.dp),
                ) {
                    Image(
                        privateKey.keyPic.asImageBitmap(),
                        contentDescription = "Отпечаток ключа",
                        modifier = Modifier.size(36.dp),
                    )

                    this@Row.AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn() + scaleIn(spring(Spring.DampingRatioMediumBouncy)),
                        exit = fadeOut() + scaleOut(tween()),
                        modifier =
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(14.dp)
                                .offset(x = 6.dp, y = 6.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = CircleShape,
                                    ).clip(CircleShape),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .size(12.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                        ).clip(CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(10.dp),
                                )
                            }
                        }
                    }
                }
            },
            shape = RectangleShape,
        )

        Box {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Больше опций",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
            ) {
                DropdownMenuItem(
                    text = { Text("Показать отпечаток") },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                    onClick = onShowFingerprintClick,
                )

                DropdownMenuItem(
                    text = { Text("Отправить пуб. ключ") },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    onClick = onShareClick,
                )

                if (isActiveKey) {
                    DropdownMenuItem(
                        text = { Text("Перегенерировать") },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        onClick = onRegenerateClick,
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text("Удалить") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = onDeleteClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyDeleteConfirmationDialog(
    keyName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить ключ") },
        text = { Text("Вы уверены, что хотите удалить ключ \"$keyName\"?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Удалить")
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
private fun KeyFingerprintDialog(
    key: PrivateKey,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отпечаток ключа") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Image(
                    bitmap = key.keyPic.asImageBitmap(),
                    contentDescription = "Отпечаток ключа",
                    modifier =
                        Modifier
                            .size(200.dp)
                            .padding(16.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Этот отпечаток должен совпадать у всех собеседников.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Если изображения отличаются, ваш чат мог быть скомпрометирован.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        },
    )
}
