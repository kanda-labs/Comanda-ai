package co.kandalabs.comandaai.features.attendance.utils

object CurrencyFormatter {
    
    /**
     * Formats a raw input string to BRL currency format (R$ X,XX)
     * @param input Raw input string with only digits
     * @return Formatted string like "R$ 25,50"
     */
    fun formatToBRL(input: String): String {
        if (input.isEmpty()) return ""
        
        // Remove all non-digit characters
        val digitsOnly = input.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return ""
        
        // Convert to cents value
        val cents = digitsOnly.toLongOrNull() ?: return ""
        
        // Format to currency
        val reais = cents / 100
        val centavos = cents % 100
        
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }
    
    /**
     * Extracts raw digits from a formatted BRL string
     * @param formattedValue Formatted string like "R$ 25,50"
     * @return Raw digits string like "2550"
     */
    fun extractDigitsFromBRL(formattedValue: String): String {
        return formattedValue.filter { it.isDigit() }
    }
    
    /**
     * Converts formatted BRL string to centavos
     * @param formattedValue Formatted string like "R$ 25,50"
     * @return Value in centavos (2550)
     */
    fun brlToCentavos(formattedValue: String): Long {
        val digitsOnly = extractDigitsFromBRL(formattedValue)
        return digitsOnly.toLongOrNull() ?: 0L
    }
    
    /**
     * Validates if input contains only valid characters for currency input
     * @param input Raw input string
     * @return true if valid
     */
    fun isValidCurrencyInput(input: String): Boolean {
        return input.all { it.isDigit() || it == 'R' || it == '$' || it == ' ' || it == ',' }
    }
}