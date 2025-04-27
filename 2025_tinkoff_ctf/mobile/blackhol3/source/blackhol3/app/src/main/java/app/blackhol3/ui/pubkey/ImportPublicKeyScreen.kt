package app.blackhol3.ui.pubkey

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.blackhol3.service.KeyPicGenerationService
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPublicKeyScreen(
    viewModel: ImportPublicKeyViewModel = koinViewModel(),
    keyPicGenService: KeyPicGenerationService = koinInject(),
    onBackClick: () -> Unit,
    onAddKeySuccess: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    var showInvalidFileDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var keyName by remember { mutableStateOf("") }

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                val isValid = viewModel.processFileUri(context, it)
                if (!isValid) {
                    showInvalidFileDialog = true
                }
            }
        }

    Scaffold(
        topBar = {
            ImportPublicKeyTopBar(onBackClick = onBackClick)
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KeyPicDisplay(
                keyPicVizData = state.pubkey?.let { keyPicGenService.publicKeyVizData(it.rsaPublicKey) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!state.isKeyValid) {
                KeySourceOptions(
                    onFilePickClick = { filePickerLauncher.launch("*/*") },
                    onPasteClick = { showBottomSheet = true },
                )
            } else {
                val fp = state.pubkey?.fingerprint
                Text(
                    text =
                        fp
                            ?.chunked(fp.length / 2)
                            ?.joinToString("\n") { it.chunked(2).joinToString(":") } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                )
                KeyActionButtons(
                    onResetClick = { viewModel.resetKey() },
                    onAddClick = { showNameDialog = true },
                )
            }
        }
    }

    if (showBottomSheet) {
        PasteKeyBottomSheet(
            pastedText = state.pastedText,
            pastedTextError = state.pastedTextError,
            onTextChange = { viewModel.updatePastedText(it) },
            onApplyClick = {
                val isValid = viewModel.processPastedText()
                if (isValid) {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        showBottomSheet = false
                    }
                }
            },
            onDismiss = { showBottomSheet = false },
            sheetState = bottomSheetState,
        )
    }

    if (showInvalidFileDialog) {
        InvalidFileDialog(
            onDismiss = { showInvalidFileDialog = false },
            onSelectAnother = {
                showInvalidFileDialog = false
                filePickerLauncher.launch("*/*")
            },
        )
    }

    if (showNameDialog) {
        ImportPublicKeyNameDialog(
            keyName = keyName,
            onKeyNameChange = { keyName = it },
            onSaveClick = {
                viewModel.addKey(keyName)
                showNameDialog = false
                onAddKeySuccess(keyName)
            },
            onDismiss = { showNameDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPublicKeyTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Новый чат") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                )
            }
        },
    )
}
