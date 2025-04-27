package whatis.love.agedate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.AndroidEntryPoint
import whatis.love.agedate.api.client.AgeDateOkHttp
import whatis.love.agedate.api.client.pinning.PinningParametersRepo
import whatis.love.agedate.api.client.session.SessionPersistence
import whatis.love.agedate.navigation.AppNavigation
import whatis.love.agedate.ui.theme.AgeDateTheme
import whatis.love.agedate.user.viewmodel.UserViewModel
import javax.inject.Inject

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var pinningParamsRepo: PinningParametersRepo

    @Inject
    lateinit var sessionPersistence: SessionPersistence

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgeDateTheme {
                val userViewModel: UserViewModel = hiltViewModel()

                setSingletonImageLoaderFactory { context ->
                    ImageLoader
                        .Builder(context)
                        .components {
                            add(
                                OkHttpNetworkFetcherFactory(
                                    callFactory = {
                                        AgeDateOkHttp(pinningParamsRepo, sessionPersistence)
                                    },
                                ),
                            )
                        }.crossfade(true)
                        .build()
                }

                AppNavigation(
                    userViewModel = userViewModel,
                )
            }
        }
    }
}
