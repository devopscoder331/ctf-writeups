package app.blackhol3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.blackhol3.navigation.AppNavigation
import app.blackhol3.service.MessageUpdateManager
import app.blackhol3.ui.theme.Blackhol3Theme
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val updateManager = MessageUpdateManager(this)
        updateManager.initialize(enableBackground = true, intervalMinutes = 1)

        enableEdgeToEdge()
        setContent {
            KoinContext {
                Blackhol3Theme {
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .imePadding(),
                        ) {
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }
}
