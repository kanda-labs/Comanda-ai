package co.kandalabs.comandaai.platform

import android.content.Context
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.session.SessionManagerImpl

actual fun createSessionManager(): SessionManager {
    // This will be injected from the Android Application context
    // For now, we'll handle this in the DI configuration
    throw UnsupportedOperationException("Use DI to get SessionManager on Android")
}