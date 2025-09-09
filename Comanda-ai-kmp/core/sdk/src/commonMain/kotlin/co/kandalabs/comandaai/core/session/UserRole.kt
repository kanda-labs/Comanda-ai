package co.kandalabs.comandaai.core.session

/**
 * Enum representing different user roles in the system
 */
enum class UserRole {
    /**
     * Administrator with full system access
     */
    ADMIN,

    /**
     * Manager with elevated privileges
     */
    MANAGER,

    /**
     * Waiter with standard service privileges
     */
    WAITER,

    /**
     * Kitchen staff with order management privileges
     */
    KITCHEN
}