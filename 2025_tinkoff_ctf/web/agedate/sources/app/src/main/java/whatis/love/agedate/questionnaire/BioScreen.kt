package whatis.love.agedate.questionnaire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BioScreen(
    bio: String,
    bioError: String? = null,
    onNext: (String) -> Unit,
) {
    var bioText by remember { mutableStateOf(bio) }
    var localBioError by remember { mutableStateOf<String?>(null) }

    val characterLimit = 1000
    val remainingChars = characterLimit - bioText.length
    LaunchedEffect(bioError) {
        bioError?.let { localBioError = it }
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
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "О себе",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Расскажите немного о себе, чтобы другие пользователи могли вас лучше узнать",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(
            visible = localBioError != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Text(
                    text = localBioError ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        OutlinedTextField(
            value = bioText,
            onValueChange = {
                if (it.length <= characterLimit) {
                    bioText = it
                    localBioError = null
                }
            },
            label = { Text("Ваша история") },
            placeholder = { Text("Напишите что-нибудь о себе...") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            supportingText = {
                Text(
                    text = "Осталось символов: $remainingChars",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            isError = localBioError != null,
        )

        Spacer(modifier = Modifier.height(16.dp))
        AnimatedVisibility(
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
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Советы по заполнению:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BioTipItem(
                        text = "Расскажите о своих увлечениях и хобби",
                    )

                    BioTipItem(
                        text = "Поделитесь своим жизненным опытом",
                    )

                    BioTipItem(
                        text = "Упомяните любимые места для прогулок",
                    )

                    BioTipItem(
                        text = "Расскажите о своей семье или домашних животных",
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onNext(bioText) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
        ) {
            Text(
                "Далее",
                fontSize = 18.sp,
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun BioTipItem(text: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
        )
    }
}
