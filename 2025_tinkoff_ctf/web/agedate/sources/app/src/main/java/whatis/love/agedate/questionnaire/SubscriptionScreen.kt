package whatis.love.agedate.questionnaire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import whatis.love.agedate.user.viewmodel.UserViewModel
import whatis.love.agedate.util.SUBSCRIBE_URL
import whatis.love.agedate.util.openExternalRedirectWithToken

@Composable
fun SubscriptionScreen(
    userViewModel: UserViewModel,
    onNext: () -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Подписка AgeDate",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Подписка",
            tint = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .size(80.dp)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ).padding(16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Доступ к сервису знакомств",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))
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
                    text = "AgeDate — не бесплатная программа. Подписка необходима для доступа к основным функциям приложения.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Без подписки вы сможете только просматривать профили и отмечать понравившиеся, но не сможете отправлять сообщения или взаимодействовать с другими пользователями.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                openExternalRedirectWithToken(
                    context,
                    SUBSCRIBE_URL,
                    userViewModel.userToken.value!!,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
        ) {
            Text(
                "Оформить подписку",
                fontSize = 18.sp,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
        ) {
            Text(
                text = "ВНИМАНИЕ: При нажатии на кнопку \"Оформить подписку\" откроется внешнее окно для совершения платежа. Убедитесь, что вы находитесь в безопасном месте перед вводом платежных данных.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 16.sp,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Вы всегда можете оформить подписку позже в разделе \"Настройки\".",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier =
                Modifier
                    .padding(top = 8.dp),
        ) {
            TextButton(
                onClick = { onNext() },
            ) {
                Text(
                    "Продолжить без подписки",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
