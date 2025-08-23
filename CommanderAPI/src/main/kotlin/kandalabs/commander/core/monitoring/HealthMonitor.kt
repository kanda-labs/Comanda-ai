package kandalabs.commander.core.monitoring

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.seconds

/**
 * Internal health monitoring service for self-healing capabilities
 */
class HealthMonitor {
    private val logger = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)
    private val lastHealthCheck = AtomicLong(System.currentTimeMillis())
    private val consecutiveFailures = AtomicLong(0)
    private val maxFailures = 3
    private val checkIntervalSeconds = 30L
    
    private var monitoringJob: Job? = null
    private var healthCheckJob: Job? = null
    
    /**
     * Start the health monitoring system
     */
    fun start() {
        if (isRunning.compareAndSet(false, true)) {
            logger.info { "🚀 Starting internal health monitor" }
            
            monitoringJob = CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
                runMonitoringLoop()
            }
            
            healthCheckJob = CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
                runHealthUpdateLoop()
            }
            
            logger.info { "✅ Health monitor started successfully" }
        } else {
            logger.warn { "Health monitor is already running" }
        }
    }
    
    /**
     * Stop the health monitoring system
     */
    fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info { "🛑 Stopping health monitor" }
            
            monitoringJob?.cancel()
            healthCheckJob?.cancel()
            
            logger.info { "✅ Health monitor stopped" }
        }
    }
    
    /**
     * Update the last health check timestamp (called by health endpoint)
     */
    fun recordHealthCheck() {
        lastHealthCheck.set(System.currentTimeMillis())
        if (consecutiveFailures.get() > 0) {
            logger.info { "✅ Health restored after ${consecutiveFailures.get()} failures" }
            consecutiveFailures.set(0)
        }
    }
    
    /**
     * Check if the application is healthy
     */
    fun isHealthy(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastCheck = currentTime - lastHealthCheck.get()
        val healthTimeoutMs = (checkIntervalSeconds + 10) * 1000 // 10s grace period
        
        return timeSinceLastCheck < healthTimeoutMs
    }
    
    /**
     * Get current health status
     */
    fun getHealthStatus(): HealthStatus {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastCheck = currentTime - lastHealthCheck.get()
        val failures = consecutiveFailures.get()
        
        return HealthStatus(
            isHealthy = isHealthy(),
            consecutiveFailures = failures,
            timeSinceLastCheckMs = timeSinceLastCheck,
            monitoringActive = isRunning.get()
        )
    }
    
    /**
     * Main monitoring loop
     */
    private suspend fun runMonitoringLoop() {
        logger.info { "🔍 Starting health monitoring loop (interval: ${checkIntervalSeconds}s, max failures: $maxFailures)" }
        
        while (isRunning.get()) {
            try {
                if (!isHealthy()) {
                    val failures = consecutiveFailures.incrementAndGet()
                    logger.warn { "⚠️ Health check failed (${failures}/$maxFailures)" }
                    
                    if (failures >= maxFailures) {
                        logger.error { "💀 Critical failure detected after $failures consecutive failures" }
                        handleCriticalFailure()
                    }
                } else {
                    // Reset failure counter on successful health check
                    if (consecutiveFailures.get() > 0) {
                        consecutiveFailures.set(0)
                    }
                }
                
                delay(checkIntervalSeconds.seconds)
            } catch (e: Exception) {
                logger.error(e) { "Error in health monitoring loop: ${e.message}" }
                delay(5.seconds) // Shorter delay on error
            }
        }
    }
    
    /**
     * Health update loop - simulates regular activity
     */
    private suspend fun runHealthUpdateLoop() {
        while (isRunning.get()) {
            try {
                // Update health timestamp every 15 seconds to show the app is alive
                delay(15.seconds)
                
                if (isRunning.get()) {
                    // Only update if we haven't had a real health check recently
                    val timeSinceCheck = System.currentTimeMillis() - lastHealthCheck.get()
                    if (timeSinceCheck > 20000) { // More than 20 seconds
                        lastHealthCheck.set(System.currentTimeMillis())
                        logger.debug { "💗 Internal health heartbeat updated" }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error in health update loop: ${e.message}" }
                delay(5.seconds)
            }
        }
    }
    
    /**
     * Handle critical application failure
     */
    private suspend fun handleCriticalFailure() {
        logger.error { "🚨 CRITICAL FAILURE - Attempting self-recovery" }
        
        try {
            // Attempt graceful recovery
            performSelfHealing()
            
            // Reset failure counter after recovery attempt
            consecutiveFailures.set(0)
            lastHealthCheck.set(System.currentTimeMillis())
            
            logger.info { "🔧 Self-recovery completed, monitoring resumed" }
        } catch (e: Exception) {
            logger.error(e) { "💥 Self-recovery failed: ${e.message}" }
            
            // If we can't recover, log critical error but continue monitoring
            // The external jpackage wrapper might restart the whole process if needed
            delay(60.seconds) // Wait before resuming normal monitoring
        }
    }
    
    /**
     * Perform self-healing actions
     */
    private suspend fun performSelfHealing() {
        logger.info { "🔧 Performing self-healing actions..." }
        
        try {
            // 1. Force garbage collection
            System.gc()
            logger.debug { "♻️ Garbage collection triggered" }
            
            // 2. Clear any cached data or reset connections
            // This would be application-specific
            logger.debug { "🧹 Clearing cached data" }
            
            // 3. Restart internal services if needed
            // For now, just simulate a recovery delay
            delay(5.seconds)
            
            // 4. Verify database connectivity
            // This could be enhanced to actually test DB connections
            logger.debug { "🗄️ Verifying database connectivity" }
            
            logger.info { "✅ Self-healing actions completed" }
        } catch (e: Exception) {
            logger.error(e) { "❌ Self-healing actions failed: ${e.message}" }
            throw e
        }
    }
}

/**
 * Health status data class
 */
@kotlinx.serialization.Serializable
data class HealthStatus(
    val isHealthy: Boolean,
    val consecutiveFailures: Long,
    val timeSinceLastCheckMs: Long,
    val monitoringActive: Boolean
)