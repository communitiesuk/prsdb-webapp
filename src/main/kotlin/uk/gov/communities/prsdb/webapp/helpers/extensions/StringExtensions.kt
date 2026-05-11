package uk.gov.communities.prsdb.webapp.helpers.extensions

class StringExtensions {
    companion object {
        fun String.toNormalizedCurrencyString(): String {
            val decimal = toBigDecimalOrNull()?.stripTrailingZeros() ?: return this
            val scaled = if (decimal.scale() < 2) decimal.setScale(2) else decimal
            return scaled.toPlainString()
        }

        fun String.toNormalizedIntegerString(): String = toIntOrNull()?.toString() ?: this
    }
}
