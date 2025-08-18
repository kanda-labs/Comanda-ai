package co.kandalabs.comandaai.platform

import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.session.SessionManagerImpl

actual fun createSessionManager(): SessionManager {
    return SessionManagerImpl()
}