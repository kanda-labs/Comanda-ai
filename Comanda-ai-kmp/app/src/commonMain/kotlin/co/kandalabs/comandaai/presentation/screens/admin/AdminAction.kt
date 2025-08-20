package co.kandalabs.comandaai.presentation.screens.admin

/**
 * Actions available in the Admin screen
 */
sealed class AdminAction {
    /**
     * Navigate to Categories Management screen
     */
    data object NavigateToCategoriesManagement : AdminAction()
    
    /**
     * Navigate to Items Management screen
     */
    data object NavigateToItemsManagement : AdminAction()
    
    /**
     * Navigate to Users Management screen
     */
    data object NavigateToUsersManagement : AdminAction()
    
    /**
     * Navigate back to previous screen
     */
    data object NavigateBack : AdminAction()
    
    /**
     * Retry loading admin data
     */
    data object Retry : AdminAction()
}