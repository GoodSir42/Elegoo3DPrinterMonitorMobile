package eu.kutscheid.elegoomonitor

import android.app.Application
import co.touchlab.kermit.LogcatWriter
import co.touchlab.kermit.Logger
import eu.kutscheid.elegoomonitor.di.appModule
import org.koin.core.context.startKoin

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule())
        }

        // Initialize Kermit with a LogcatWriter for Android
        // This will output logs to Logcat.
        Logger.setLogWriters(LogcatWriter())

        // You can also set a default tag for all logs from Kermit
        // If not set, it often defaults to the class name where the log is called.
        // Logger.setTag("MyAppTag") // Optional: set a global tag

        // Example: Log a message when the application starts
        Logger.i { "Kermit logging initialized in Application class." }
    }
}