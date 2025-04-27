package whatis.love.agedate.chats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import kotlin.text.Typography.nbsp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListPaywallScreen(
    userViewModel: UserViewModel,
    navbarPaddingValues: PaddingValues,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                TopAppBar(
                    title = { Text(fontWeight = FontWeight.Bold, text = "Сообщения") },
                    windowInsets = WindowInsets.statusBars,
                    colors = TopAppBarDefaults.topAppBarColors(),
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        },
        modifier = Modifier.padding(bottom = navbarPaddingValues.calculateBottomPadding()),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "Эта${nbsp}функция${nbsp}доступна${nbsp}только пользователям${nbsp}с${nbsp}подпиской",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    openExternalRedirectWithToken(
                        context,
                        SUBSCRIBE_URL,
                        userViewModel.userToken.value!!,
                    )
                },
                modifier = Modifier.height(56.dp),
            ) {
                Text(
                    "Оформить подписку",
                    fontSize = 18.sp,
                )
            }
        }
    }
}
