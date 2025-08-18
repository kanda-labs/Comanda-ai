package co.kandalabs.comandaai.auth.navigation

object AuthNavigationCallback {
    private var loginSuccessCallback: (() -> Unit)? = null
    
    fun setOnLoginSuccess(callback: () -> Unit) {
        loginSuccessCallback = callback
    }
    
    fun onLoginSuccess() {
        loginSuccessCallback?.invoke()
    }
}