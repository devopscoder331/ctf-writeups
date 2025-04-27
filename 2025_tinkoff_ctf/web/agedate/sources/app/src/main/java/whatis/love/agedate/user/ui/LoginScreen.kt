package whatis.love.agedate.user.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import whatis.love.agedate.R
import whatis.love.agedate.user.viewmodel.UserState
import whatis.love.agedate.user.viewmodel.UserViewModel

@Composable
fun LoginScreen(
    userViewModel: UserViewModel,
    onRegisterClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

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
    val isLoading = authState is UserState.Loading

    var showLogo by remember { mutableStateOf(false) }
    var showFields by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        delay(100)
        showLogo = true
        delay(300)
        showFields = true
        delay(300)
        showButtons = true
    }

    LaunchedEffect(key1 = Unit) {
        userViewModel.reset()
    }

    val focusManager = LocalFocusManager.current

    fun login(
        username: String,
        password: String,
    ) {
        userViewModel.login(username, password)
    }
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .imePadding()
                .navigationBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(bottom = 32.dp)
                        .padding(horizontal = 64.dp)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .graphicsLayer(
                                alpha =
                                    animateFloatAsState(
                                        targetValue = if (showLogo) 1f else 0f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "logoAlpha",
                                    ).value,
                                translationY =
                                    animateFloatAsState(
                                        targetValue = if (showLogo) 0f else -200f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "logoTranslation",
                                    ).value,
                            ).clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.agedate_logo_hollow),
                        contentDescription = "Логотип",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    )
                }
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    label = { Text("Имя пользователя") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                alpha =
                                    animateFloatAsState(
                                        targetValue = if (showFields) 1f else 0f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "usernameAlpha",
                                    ).value,
                                translationY =
                                    animateFloatAsState(
                                        targetValue = if (showFields) 0f else 200f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "usernameTranslation",
                                    ).value,
                            ),
                    enabled = !isLoading,
                    isError = usernameError != null || generalError != null,
                    supportingText =
                        if (usernameError != null) {
                            { Text(usernameError) }
                        } else {
                            null
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            imeAction = ImeAction.Next,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    singleLine = true,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = { Text("Пароль") },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                alpha =
                                    animateFloatAsState(
                                        targetValue = if (showFields) 1f else 0f,
                                        animationSpec =
                                            tween(
                                                500,
                                                delayMillis = 150,
                                                easing = FastOutSlowInEasing,
                                            ),
                                        label = "passwordAlpha",
                                    ).value,
                                translationY =
                                    animateFloatAsState(
                                        targetValue = if (showFields) 0f else 200f,
                                        animationSpec =
                                            tween(
                                                500,
                                                delayMillis = 150,
                                                easing = FastOutSlowInEasing,
                                            ),
                                        label = "passwordTranslation",
                                    ).value,
                            ),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль",
                            )
                        }
                    },
                    enabled = !isLoading,
                    isError = passwordError != null || generalError != null,
                    supportingText =
                        if (passwordError != null) {
                            { Text(passwordError) }
                        } else {
                            null
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                login(
                                    username,
                                    password,
                                )
                            },
                        ),
                    singleLine = true,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
            ) {
                Button(
                    onClick = {
                        login(username, password)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer(
                                alpha =
                                    animateFloatAsState(
                                        targetValue = if (showButtons) 1f else 0f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "buttonAlpha",
                                    ).value,
                                translationY =
                                    animateFloatAsState(
                                        targetValue = if (showButtons) 0f else 200f,
                                        animationSpec = tween(500, easing = FastOutSlowInEasing),
                                        label = "buttonTranslation",
                                    ).value,
                            ),
                ) {
                    Text("Войти")
                }
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
            Box(
                modifier =
                    Modifier
                        .padding(top = 8.dp),
            ) {
                TextButton(
                    onClick = onRegisterClick,
                    modifier =
                        Modifier
                            .graphicsLayer(
                                alpha =
                                    animateFloatAsState(
                                        targetValue = if (showButtons) 1f else 0f,
                                        animationSpec =
                                            tween(
                                                500,
                                                delayMillis = 150,
                                                easing = FastOutSlowInEasing,
                                            ),
                                        label = "registerAlpha",
                                    ).value,
                                translationY =
                                    animateFloatAsState(
                                        targetValue = if (showButtons) 0f else 200f,
                                        animationSpec =
                                            tween(
                                                500,
                                                delayMillis = 150,
                                                easing = FastOutSlowInEasing,
                                            ),
                                        label = "registerTranslation",
                                    ).value,
                            ),
                ) {
                    Text(
                        "Ещё нет аккаунта? Зарегистрируйтесь!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
