package app.blackhol3.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.blackhol3.ui.common.PrivateKeyViewModel
import app.blackhol3.ui.common.PrivateKeyViewModelState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation(privateKeyViewModel: PrivateKeyViewModel = koinViewModel()) {
    val privateKey by privateKeyViewModel.currentPrivateKey.collectAsState()
    val privateKeyState by privateKeyViewModel.currentState.collectAsState()
    val state =
        when (privateKeyState) {
            is PrivateKeyViewModelState.Done ->
                if (privateKey == null) {
                    NavigationState.NoKey
                } else {
                    NavigationState.WithKey
                }

            else -> NavigationState.Loading
        }

    Surface(
        modifier =
            Modifier
                .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (fadeIn()) togetherWith
                    (slideOutHorizontally { width -> -width } + fadeOut())
            },
            modifier = Modifier.fillMaxSize(),
        ) { state ->
            when (state) {
                is NavigationState.Loading -> {
                }

                is NavigationState.WithKey -> WithKeyNavHost()

                is NavigationState.NoKey -> NoKeyNavHost()
            }
        }
    }
}
