package co.kandalabs.comandaai.core.utils

/**
 * Utility object for formatting currency values in Brazilian Real (BRL)
 */
object CurrencyFormatter {

    /**
     * Formats a value in cents to Brazilian Real currency format
     * @param valueInCents The value in cents (e.g., 1050 = R$ 10,50)
     * @return Formatted string in format "R$ XX,XX"
     */
    fun formatCents(valueInCents: Int): String {
        val reais = valueInCents / 100
        val centavos = valueInCents % 100
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }

    /**
     * Formats a value in cents to Brazilian Real currency format (Long version)
     * @param valueInCents The value in cents (e.g., 1050 = R$ 10,50)
     * @return Formatted string in format "R$ XX,XX"
     */
    fun formatCents(valueInCents: Long): String {
        val reais = valueInCents / 100
        val centavos = valueInCents % 100
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }

    /**
     * Parses a currency string to cents
     * @param value The currency string (e.g., "10,50" or "10.50" or "R$ 10,50")
     * @return Value in cents, or 0 if parsing fails
     */
    fun parseToCents(value: String): Int {
        val cleanedValue = value
            .replace("R$", "")
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()

        return try {
            val doubleValue = cleanedValue.toDoubleOrNull() ?: 0.0
            (doubleValue * 100).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Formats a decimal value to currency display format (without R$ symbol)
     * @param value The decimal value (e.g., 10.5)
     * @return Formatted string in format "XX,XX"
     */
    fun formatDecimal(value: Double): String {
        val cents = (value * 100).toInt()
        val reais = cents / 100
        val centavos = cents % 100
        return "$reais,${centavos.toString().padStart(2, '0')}"
    }
}