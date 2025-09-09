package co.kandalabs.comandaai.core.session

import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.sdk.session.UserSession

class SessionManagerImpl : SessionManager {
    
    private var _userSession: UserSession? = null

    override suspend fun saveSession(session: UserSession) {
        _userSession = session
        // TODO: Implement persistent storage for desktop if needed
    }

    override suspend fun getSession(): UserSession? {
        return _userSession
    }

    override suspend fun clearSession() {
        _userSession = null
        // TODO: Clear persistent storage for desktop if needed
    }

    override suspend fun logout() {
        clearSession()
        // TODO: Clear additional cached data if needed
    }

    override suspend fun hasActiveSession(): Boolean {
        return _userSession != null
    }
}