package whatis.love.agedate.questionnaire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NameScreen(
    firstName: String,
    lastName: String,
    firstNameError: String? = null,
    lastNameError: String? = null,
    onNext: (String, String) -> Unit,
) {
    var firstNameValue by remember { mutableStateOf(firstName) }
    var lastNameValue by remember { mutableStateOf(lastName) }
    var localFirstNameError by remember { mutableStateOf<String?>(null) }
    var localLastNameError by remember { mutableStateOf<String?>(null) }
    val nameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(firstNameError, lastNameError) {
        firstNameError?.let { localFirstNameError = it }
        lastNameError?.let { localLastNameError = it }
    }

    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    val enterTransition =
        remember { expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn() }
    val exitTransition =
        remember {
            shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeOut()
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Как вас зовут?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = firstNameValue,
            onValueChange = {
                firstNameValue = it
                localFirstNameError = null
            },
            label = { Text("Имя") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester),
            isError = localFirstNameError != null,
            supportingText = {
                if (localFirstNameError != null) {
                    Text(
                        text = localFirstNameError!!,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            singleLine = true,
        )

        OutlinedTextField(
            value = lastNameValue,
            onValueChange = {
                lastNameValue = it
                localLastNameError = null
            },
            label = { Text("Фамилия") },
            modifier = Modifier.fillMaxWidth(),
            isError = localLastNameError != null,
            supportingText = {
                if (localLastNameError != null) {
                    Text(
                        text = localLastNameError!!,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Box {
            this@Column.AnimatedVisibility(
                visible = !imeVisible,
                enter = enterTransition,
                exit = exitTransition,
            ) {
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
                        text = "Ваше имя и фамилия будут видны другим пользователям. Пожалуйста, используйте настоящие данные.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                when {
                    firstNameValue.isBlank() -> {
                        localFirstNameError = "Пожалуйста, укажите имя"
                    }

                    lastNameValue.isBlank() -> {
                        localLastNameError = "Пожалуйста, укажите фамилию"
                    }

                    else -> {
                        onNext(firstNameValue, lastNameValue)
                    }
                }
            },
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

        Spacer(modifier = Modifier.height(48.dp))
    }
    LaunchedEffect(firstNameError, lastNameError) {
        if (firstNameError != null || lastNameError != null) {
            nameFocusRequester.requestFocus()
        }
    }
}
