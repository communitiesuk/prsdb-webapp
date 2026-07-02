package uk.gov.communities.prsdb.webapp.helpers.extensions

class StringExtensions {
    companion object {
        fun String.toNormalizedCurrencyString(): String {
            val decimal = toBigDecimalOrNull()?.stripTrailingZeros() ?: return this
            val scaled = if (decimal.scale() < 2) decimal.setScale(2) else decimal
            return scaled.toPlainString()
        }

        fun String.toNormalizedIntegerString(): String = toIntOrNull()?.toString() ?: this

        fun String.toNormalizedEmail(): String = trim().lowercase()

        fun String.isSameEmailAs(other: String?): Boolean = other != null && toNormalizedEmail() == other.toNormalizedEmail()

        fun Iterable<String>.containsEmail(email: String): Boolean = any { it.isSameEmailAs(email) }
    }
}
