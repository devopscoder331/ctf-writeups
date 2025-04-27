package whatis.love.agedate.profile.ui

import android.app.Activity
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import whatis.love.agedate.api.model.Microstatus
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.api.model.UserAccessLevel
import whatis.love.agedate.profile.viewmodel.ProfileScreenViewModel
import whatis.love.agedate.report.ReportDetails
import whatis.love.agedate.user.viewmodel.UserViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileScreenViewModel,
    userViewModel: UserViewModel,
    onBackClick: () -> Unit,
    onShareClick: (profileID: String) -> Unit,
    onMessageClick: (profileID: String) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile
    val adminVisibleProfileFields = uiState.adminFields
    val user by userViewModel.userProfile.collectAsStateWithLifecycle()
    val russianLocale = Locale("ru", "RU")
    val printErrorMessage by viewModel.printErrorMessage.collectAsStateWithLifecycle()
    var showReportSheet by remember { mutableStateOf(false) }
    var showAdminBanSheet by remember { mutableStateOf(false) }

    val banActionResult by viewModel.banActionSuccess.collectAsStateWithLifecycle()
    val banActionError by viewModel.banActionError.collectAsStateWithLifecycle()
    if (printErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearPrintError() },
            title = { Text("Ошибка печати") },
            text = { Text(printErrorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearPrintError() }) {
                    Text("OK")
                }
            },
        )
    }

    if (banActionResult != null) {
        if (banActionResult == true) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.clearBanActionResult()
                    showAdminBanSheet = false
                },
                title = { Text("Успешно") },
                text = { Text("Операция выполнена успешно") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearBanActionResult()
                        showAdminBanSheet = false
                    }) {
                        Text("OK")
                    }
                },
            )
        } else {
            AlertDialog(
                onDismissRequest = { viewModel.clearBanActionResult() },
                title = { Text("Ошибка") },
                text = { Text("$banActionError") },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearBanActionResult() }) {
                        Text("OK")
                    }
                },
            )
        }
    }
    if (showReportSheet && user != null) {
        ReportViolationBottomSheet(
            onDismiss = { showReportSheet = false },
            onPrintReport = { myProfile, profile, violationType ->
                val reportDetails = ReportDetails(myProfile, profile, violationType)
                if (activity != null) {
                    viewModel.printReport(activity, reportDetails)
                }
            },
            profile = profile,
            myProfile = user!!,
            token = userViewModel.userToken.value ?: "",
        )
    }

    if (showAdminBanSheet) {
        AdminBanBottomSheet(
            onDismiss = { showAdminBanSheet = false },
            onBanActionSubmit = { request ->
                viewModel.setBanStatus(profile.id, request)
            },
            adminProfile = uiState.adminFields,
        )
    }

    val loading = uiState.profileLoading
    val refreshing = uiState.profileRefreshing
    if (loading && !refreshing) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.Black,
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
        return
    }

    val error = uiState.profileErrorHeader
    if (error != null) {
        ProfileErrorMessage(error, uiState.profileErrorMessage, onBackClick)
        return
    }

    PullToRefreshBox(
        isRefreshing = uiState.profileRefreshing,
        onRefresh = { viewModel.refresh(false) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                ) {
                    AsyncImage(
                        model = profile.profilePictureUrl,
                        contentDescription = "Фото профиля ${profile.firstName} ${profile.lastName}",
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
                }
            }

            item {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${profile.firstName} ${profile.lastName}${profile.ageSuffix}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    if (profile.likeStatus == ProfileLikeStatus.LIKED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Симпатия",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
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
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        val day = profile.birthDay
                        val month = profile.birthMonth
                        if (day != null && month != null) {
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
                                profile.birthYear?.let { calendar.set(Calendar.YEAR, it) }

                                val birthdayText =
                                    if (profile.birthYear != null) {
                                        val yearFormat = SimpleDateFormat("yyyy", russianLocale)
                                        "${dateFormat.format(calendar.time)} ${
                                            yearFormat.format(
                                                calendar.time,
                                            )
                                        } г."
                                    } else {
                                        dateFormat.format(calendar.time)
                                    }

                                Text(
                                    text = birthdayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        if (profile.likeStatus == ProfileLikeStatus.LIKED) {
                            profile.decidedAt?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Вы лайкнули",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val date = Date(it)
                                    val dateFormat =
                                        DateFormat.getDateInstance(
                                            DateFormat.MEDIUM,
                                            russianLocale,
                                        )

                                    Text(
                                        text = "Вы лайкнули ${dateFormat.format(date)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        if (profile.likeStatus == ProfileLikeStatus.DISLIKED) {
                            profile.decidedAt?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Вы дислайкнули",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    val date = Date(it)
                                    val dateFormat =
                                        DateFormat.getDateInstance(
                                            DateFormat.MEDIUM,
                                            russianLocale,
                                        )

                                    Text(
                                        text = "Вы дислайкнули ${dateFormat.format(date)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

                        profile.likedYouAt?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = "Лайкнул(а) вас",
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                val date = Date(it)
                                val dateFormat =
                                    DateFormat.getDateInstance(
                                        DateFormat.MEDIUM,
                                        russianLocale,
                                    )

                                Text(
                                    text = "Лайкнул(а) вас ${dateFormat.format(date)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (profile.likedYouAt != null) {
                            Button(
                                onClick = { onMessageClick(profile.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Написать")
                            }
                        }

                        if (adminVisibleProfileFields != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Дата регистрации",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                val date = Date(adminVisibleProfileFields.registrationTimestamp)
                                val dateFormat =
                                    DateFormat.getDateTimeInstance(
                                        DateFormat.SHORT,
                                        DateFormat.SHORT,
                                        Locale("ru", "RU"),
                                    )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Дата регистрации: ${dateFormat.format(date)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Последняя активность",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                val date = Date(adminVisibleProfileFields.lastActivityTimestamp)
                                val dateFormat =
                                    DateFormat.getDateTimeInstance(
                                        DateFormat.SHORT,
                                        DateFormat.SHORT,
                                        Locale("ru", "RU"),
                                    )
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Последняя активность: ${dateFormat.format(date)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Дата рождения",
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Дата рождения: ${adminVisibleProfileFields.birthDay} ${adminVisibleProfileFields.birthMonth} ${adminVisibleProfileFields.birthYear}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Видимость даты рождения",
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Видимость ДР: ${adminVisibleProfileFields.birthDateVisibility.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }

                            if (adminVisibleProfileFields.isBanned) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Бан",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Забанен администратором ${adminVisibleProfileFields.bannedBy}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Причина бана",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Причина бана: ${adminVisibleProfileFields.banReason}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Окончание бана",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )

                                    val date = Date(adminVisibleProfileFields.banExpires ?: 0)
                                    val dateFormat =
                                        DateFormat.getDateTimeInstance(
                                            DateFormat.SHORT,
                                            DateFormat.SHORT,
                                            Locale("ru", "RU"),
                                        )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Окончание бана: ${dateFormat.format(date)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Отправленных лайков",
                                    tint = MaterialTheme.colorScheme.primary,
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Отправленных лайков: ${adminVisibleProfileFields.likesSentCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
            item {
                if (uiState.microstatusHistory.isNotEmpty()) {
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
            items(uiState.microstatusHistory) { microStatus ->
                ProfileMicrostatusHistoryItem(microStatus = microStatus)
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
                if (user?.accessLevel == UserAccessLevel.ADMIN) {
                    IconButton(onClick = { showAdminBanSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Ограничить",
                            tint = Color.White,
                        )
                    }
                }

                IconButton(onClick = { onShareClick(profile.id) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Поделиться",
                        tint = Color.White,
                    )
                }

                IconButton(onClick = { showReportSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Пожаловаться",
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
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileErrorMessage(
    heading: String,
    content: String?,
    onBackClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
        ) {
            Text(
                text = heading,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            content?.let {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }

        TopAppBar(
            title = { Text("Ошибка") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.Black,
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            modifier = Modifier.statusBarsPadding(),
        )
    }
}

@Composable
fun ProfileMicrostatusHistoryItem(microStatus: Microstatus) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Surface(
            shape =
                MicrostatusShape(
                    cornerRadius = 12f,
                    arrowPosition = MicrostatusShape.ArrowPosition.BOTTOM_START,
                    arrowWidth = 24f,
                    arrowHeight = 14f,
                ),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp,
            modifier = Modifier.padding(2.dp),
        ) {
            Text(
                text = microStatus.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier.padding(
                        start = 10.dp,
                        end = 12.dp,
                        top = 10.dp,
                        bottom = 10.dp,
                    ),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val date = Date(microStatus.createdAt)
        val dateFormat =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale("ru", "RU"))

        Text(
            text = dateFormat.format(date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}
