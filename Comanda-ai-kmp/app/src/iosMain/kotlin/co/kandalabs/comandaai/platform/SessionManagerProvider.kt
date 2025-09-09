package co.kandalabs.comandaai.platform

import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.sdk.session.SessionManagerImpl

actual fun createSessionManager(): SessionManager {
    return SessionManagerImpl()
}