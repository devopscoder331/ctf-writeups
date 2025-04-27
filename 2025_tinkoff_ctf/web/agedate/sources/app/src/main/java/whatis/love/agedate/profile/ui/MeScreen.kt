package whatis.love.agedate.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import whatis.love.agedate.api.model.UserAccessLevel
import whatis.love.agedate.user.viewmodel.UserViewModel
import whatis.love.agedate.util.SUBSCRIBE_URL
import whatis.love.agedate.util.openExternalRedirectWithToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: (profileID: String) -> Unit,
) {
    val myProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val russianLocale = Locale("ru", "RU")
    val context = LocalContext.current

    var showPaywallDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNewMicrostatusSheet by remember { mutableStateOf(false) }
    var microstatusText by remember { mutableStateOf("") }
    val microstatusError by viewModel.microstatusError.collectAsStateWithLifecycle()
    val microstatusUpdating by viewModel.microstatusUpdating.collectAsStateWithLifecycle()
    LaunchedEffect(microstatusUpdating) {
        if (microstatusUpdating == false && microstatusError == null) {
            microstatusText = ""
            showNewMicrostatusSheet = false
        }
    }

    if (showNewMicrostatusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewMicrostatusSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text(
                    text = "Новый статус",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                OutlinedTextField(
                    value = microstatusText,
                    onValueChange = {
                        if (it.length <= 200) microstatusText = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Что у вас нового?") },
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (microstatusError != null) {
                                Text(
                                    text = microstatusError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = "${microstatusText.length}/200",
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    },
                    isError = microstatusError != null,
                    maxLines = 5,
                    minLines = 3,
                    singleLine = false,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.newMicrostatus(microstatusText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = microstatusText.isNotBlank() && microstatusUpdating != true,
                ) {
                    if (microstatusUpdating == true) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Опубликовать")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel.refreshUserProfile() },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                    ) {
                        AsyncImage(
                            model = myProfile!!.profilePictureUrl,
                            contentDescription = "Фото вашего профиля",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    Color.Black.copy(alpha = 0.05f),
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.5f),
                                                ),
                                            startY = 200f,
                                        ),
                                    ),
                        )
                        FloatingActionButton(
                            onClick = { showNewMicrostatusSheet = true },
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .size(56.dp)
                                    .align(Alignment.BottomEnd),
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Добавить статус",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${myProfile!!.firstName} ${myProfile!!.lastName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                item {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = "О пользователе",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = myProfile!!.bio,
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val day = myProfile!!.birthDay
                            val month = myProfile!!.birthMonth

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cake,
                                    contentDescription = "День рождения",
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                val dateFormat = SimpleDateFormat("d MMMM", russianLocale)

                                val calendar = Calendar.getInstance()
                                calendar.set(Calendar.DAY_OF_MONTH, day)
                                calendar.set(Calendar.MONTH, month - 1)
                                myProfile!!.birthYear.let { calendar.set(Calendar.YEAR, it) }

                                val yearFormat = SimpleDateFormat("yyyy", russianLocale)
                                val birthdayText = "${dateFormat.format(calendar.time)} ${
                                    yearFormat.format(
                                        calendar.time,
                                    )
                                } г."

                                Text(
                                    text = birthdayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
                item {
                    if (myProfile!!.microstatusHistory.isNotEmpty()) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                        ) {
                            Text(
                                text = "История статусов",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }
                }

                items(myProfile!!.microstatusHistory) {
                    ProfileMicrostatusHistoryItem(microStatus = it)
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = {
                        if (myProfile!!.accessLevel == UserAccessLevel.TRIAL) {
                            showPaywallDialog = true
                        } else {
                            onShareClick(myProfile!!.id)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = Color.White,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        titleContentColor = Color.White,
                    ),
                modifier = Modifier.statusBarsPadding(),
            )
        }

        if (showPaywallDialog) {
            AlertDialog(
                onDismissRequest = { showPaywallDialog = false },
                title = { Text("Требуется подписка") },
                text = {
                    Column {
                        Text("Для того, чтобы ваш профиль могли увидеть другие пользователи, нужна платная подписка.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            openExternalRedirectWithToken(
                                context,
                                SUBSCRIBE_URL,
                                viewModel.userToken.value!!,
                            )
                            showPaywallDialog = false
                        },
                    ) {
                        Text("Оформить подписку")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPaywallDialog = false }) {
                        Text("Отмена")
                    }
                },
            )
        }
    }
}
