package whatis.love.agedate.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import whatis.love.agedate.api.model.MyProfile
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.util.REPORT_URL
import whatis.love.agedate.util.openExternalRedirectWithToken

@Composable
fun ReportPrintConfirmationDialog(
    onDismiss: () -> Unit,
    onPrintClick: (myProfile: MyProfile, profile: Profile, violationType: String) -> Unit,
    violationType: String,
    profile: Profile,
    myProfile: MyProfile,
    token: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Отправка жалобы",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            val context = LocalContext.current

            Column {
                Text(
                    text = "Распечатайте созданное заявление и отправьте по указанному в нём почтовому адресу заказным письмом.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Для ускорения процесса рассмотрения жалобы, вы можете загрузить фотографию вашего заявления ",
                )
                Text(
                    text = "на нашем сайте.",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                        ),
                    modifier =
                        Modifier.clickable {
                            openExternalRedirectWithToken(context, REPORT_URL, token)
                        },
                    textDecoration = TextDecoration.Underline,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ВАЖНО: при предварительной подаче, анкета может быть заблокирована только после получения оригинала.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Функция отправки фотографий заявления доступна в тестовом режиме.",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPrintClick(myProfile, profile, violationType)
                    onDismiss()
                },
            ) {
                Text("Печать")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("Отмена")
            }
        },
    )
}
