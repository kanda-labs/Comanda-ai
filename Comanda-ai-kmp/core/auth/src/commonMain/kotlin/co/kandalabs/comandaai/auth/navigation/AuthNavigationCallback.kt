package co.kandalabs.comandaai.auth.navigation

object AuthNavigationCallback {
    private var loginSuccessCallback: (() -> Unit)? = null
    private var logoutSuccessCallback: (() -> Unit)? = null
    
    fun setOnLoginSuccess(callback: () -> Unit) {
        loginSuccessCallback = callback
    }
    
    fun setOnLogoutSuccess(callback: () -> Unit) {
        logoutSuccessCallback = callback
    }
    
    fun onLoginSuccess() {
        loginSuccessCallback?.invoke()
    }
    
    fun onLogoutSuccess() {
        logoutSuccessCallback?.invoke()
    }
}