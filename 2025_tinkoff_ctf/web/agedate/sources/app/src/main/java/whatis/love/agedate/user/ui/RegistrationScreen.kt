package whatis.love.agedate.user.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import whatis.love.agedate.user.viewmodel.UserState
import whatis.love.agedate.user.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by userViewModel.userState.collectAsState()

    val usernameError =
        when (authState) {
            is UserState.Error -> (authState as UserState.Error).usernameError
            else -> null
        }

    val passwordError =
        when (authState) {
            is UserState.Error -> (authState as UserState.Error).passwordError
            else -> null
        }

    val generalError =
        when (authState) {
            is UserState.Error -> (authState as UserState.Error).message
            else -> null
        }

    var showPassword by remember { mutableStateOf(false) }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    fun register(
        username: String,
        password: String,
    ) {
        userViewModel.register(username, password)
    }

    val enterTransition =
        remember { expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn() }
    val exitTransition =
        remember {
            shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeOut()
        }

    LaunchedEffect(Unit) {
        userViewModel.reset()
    }

    LaunchedEffect(key1 = authState) {
        if (authState is UserState.Authenticated) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
        LaunchedEffect(imeVisible) {
            isKeyboardVisible = imeVisible
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                this@Column.AnimatedVisibility(
                    visible = !isKeyboardVisible,
                    enter = enterTransition,
                    exit = exitTransition,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Добро пожаловать!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Создайте учетную запись",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                label = { Text("Имя пользователя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = usernameError != null || generalError != null,
                supportingText = {
                    if (usernameError != null) {
                        Text(
                            text = usernameError,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text("Минимум 9 символов")
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                },
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = passwordError != null || generalError != null,
                supportingText = {
                    if (passwordError != null) {
                        Text(
                            text = passwordError,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text("Минимум 9 символов")
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            register(username, password)
                        },
                    ),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль",
                        )
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            )

            Box {
                this@Column.AnimatedVisibility(
                    visible = !isKeyboardVisible,
                    enter = enterTransition,
                    exit = exitTransition,
                ) {
                    Column {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Text(
                                text = "ВАЖНО: Пожалуйста, запишите ваше имя пользователя и пароль и храните их в безопасном месте. Эта информация необходима для входа в приложение.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            Button(
                onClick = { register(username, password) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
            ) {
                Text(
                    "Далее",
                    fontSize = 18.sp,
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
            ) {
                Text(
                    text = generalError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
