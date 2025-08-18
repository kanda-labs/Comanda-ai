package kandalabs.commander.domain.enums

import kotlinx.serialization.Serializable

/**
 * Enum representing different user roles in the system
 */
@Serializable
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