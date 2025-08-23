package kandalabs.commander.core.monitoring

import mu.KotlinLogging
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import javax.swing.JOptionPane
import kotlin.system.exitProcess

/**
 * System tray integration for CommanderAPI
 * Provides visual status and basic controls in the system tray
 */
class SystemTrayManager(private val healthMonitor: HealthMonitor) {
    private val logger = KotlinLogging.logger {}
    private var trayIcon: TrayIcon? = null
    private var tray: SystemTray? = null
    
    /**
     * Initialize and show system tray icon
     */
    fun initialize() {
        if (!SystemTray.isSupported()) {
            logger.warn { "System tray not supported on this platform" }
            return
        }
        
        try {
            logger.info { "🖥️ Initializing system tray" }
            
            tray = SystemTray.getSystemTray()
            val image = createTrayImage()
            
            // Create popup menu
            val popup = PopupMenu()
            
            // Status item
            val statusItem = MenuItem("CommanderAPI - Starting...")
            statusItem.isEnabled = false
            popup.add(statusItem)
            
            popup.addSeparator()
            
            // Open API docs
            val docsItem = MenuItem("Open API Docs")
            docsItem.addActionListener {
                openUrl("http://localhost:8081/swagger-ui")
            }
            popup.add(docsItem)
            
            // Health status
            val healthItem = MenuItem("Show Health Status")
            healthItem.addActionListener {
                showHealthStatus()
            }
            popup.add(healthItem)
            
            popup.addSeparator()
            
            // Exit
            val exitItem = MenuItem("Exit CommanderAPI")
            exitItem.addActionListener {
                shutdown()
            }
            popup.add(exitItem)
            
            // Create tray icon
            trayIcon = TrayIcon(image, "CommanderAPI", popup).apply {
                isImageAutoSize = true
                addActionListener { showHealthStatus() }
            }
            
            tray?.add(trayIcon)
            updateStatus("Running", true)
            
            logger.info { "✅ System tray initialized successfully" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize system tray: ${e.message}" }
        }
    }
    
    /**
     * Update tray icon status
     */
    fun updateStatus(status: String, isHealthy: Boolean) {
        trayIcon?.let { icon ->
            val statusText = "CommanderAPI - $status"
            icon.toolTip = statusText
            
            // Update popup menu status
            if (icon.popupMenu is PopupMenu) {
                val popup = icon.popupMenu as PopupMenu
                if (popup.itemCount > 0) {
                    popup.getItem(0).label = statusText
                }
            }
            
            // Update icon color based on health
            icon.image = createTrayImage(isHealthy)
            
            // Show notification on status change
            if (!isHealthy) {
                icon.displayMessage(
                    "CommanderAPI Warning",
                    "API health issues detected",
                    TrayIcon.MessageType.WARNING
                )
            }
        }
    }
    
    /**
     * Show health status dialog
     */
    private fun showHealthStatus() {
        try {
            val status = healthMonitor.getHealthStatus()
            val message = buildString {
                appendLine("CommanderAPI Health Status")
                appendLine("========================")
                appendLine("Status: ${if (status.isHealthy) "✅ Healthy" else "❌ Unhealthy"}")
                appendLine("Monitoring: ${if (status.monitoringActive) "✅ Active" else "❌ Inactive"}")
                appendLine("Consecutive Failures: ${status.consecutiveFailures}")
                appendLine("Last Check: ${status.timeSinceLastCheckMs / 1000}s ago")
                appendLine()
                appendLine("API Endpoint: http://localhost:8081")
                appendLine("Swagger Docs: http://localhost:8081/swagger-ui")
            }
            
            JOptionPane.showMessageDialog(
                null,
                message,
                "CommanderAPI Health Status",
                if (status.isHealthy) JOptionPane.INFORMATION_MESSAGE else JOptionPane.WARNING_MESSAGE
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to show health status: ${e.message}" }
        }
    }
    
    /**
     * Open URL in default browser
     */
    private fun openUrl(url: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI(url))
            } else {
                logger.warn { "Desktop browse not supported" }
                showMessage("API URL", "Open manually: $url")
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to open URL: $url" }
            showMessage("API URL", "Open manually: $url")
        }
    }
    
    /**
     * Show simple message dialog
     */
    private fun showMessage(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
    }
    
    /**
     * Create tray icon image
     */
    private fun createTrayImage(isHealthy: Boolean = true): Image {
        val size = 16
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        
        // Enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        // Set color based on health status
        g.color = if (isHealthy) Color.GREEN else Color.RED
        g.fillOval(2, 2, size - 4, size - 4)
        
        // Add border
        g.color = Color.BLACK
        g.drawOval(2, 2, size - 4, size - 4)
        
        // Add "C" for CommanderAPI
        g.color = Color.WHITE
        g.font = Font(Font.SANS_SERIF, Font.BOLD, 10)
        val fm = g.fontMetrics
        val textWidth = fm.stringWidth("C")
        val textHeight = fm.height
        g.drawString("C", (size - textWidth) / 2, (size + textHeight) / 2 - 2)
        
        g.dispose()
        return image
    }
    
    /**
     * Shutdown and cleanup
     */
    fun shutdown() {
        try {
            logger.info { "🛑 Shutting down CommanderAPI" }
            
            trayIcon?.displayMessage(
                "CommanderAPI",
                "Shutting down...",
                TrayIcon.MessageType.INFO
            )
            
            // Remove from tray
            trayIcon?.let { tray?.remove(it) }
            
            // Stop health monitor
            healthMonitor.stop()
            
            // Exit application
            exitProcess(0)
        } catch (e: Exception) {
            logger.error(e) { "Error during shutdown: ${e.message}" }
            exitProcess(1)
        }
    }
    
    /**
     * Update status periodically based on health monitor
     */
    fun startStatusUpdater() {
        Thread {
            while (true) {
                try {
                    val status = healthMonitor.getHealthStatus()
                    val statusText = when {
                        !status.monitoringActive -> "Monitoring Disabled"
                        status.isHealthy -> "Running"
                        status.consecutiveFailures > 0 -> "Health Issues (${status.consecutiveFailures})"
                        else -> "Unknown"
                    }
                    
                    updateStatus(statusText, status.isHealthy)
                    Thread.sleep(15000) // Update every 15 seconds
                } catch (e: Exception) {
                    logger.error(e) { "Error updating tray status: ${e.message}" }
                    Thread.sleep(30000) // Wait longer on error
                }
            }
        }.apply {
            isDaemon = true
            name = "TrayStatusUpdater"
            start()
        }
    }
}