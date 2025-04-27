package app.blackhol3

import android.app.Application
import android.content.Intent
import app.blackhol3.di.appModule
import app.blackhol3.service.MessageWebSocketBackgroundService
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class Blackhol3App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@Blackhol3App)
            modules(appModule)

            try {
                val serviceIntent =
                    Intent(applicationContext, MessageWebSocketBackgroundService::class.java)
                startService(serviceIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
