package uk.gov.communities.prsdb.webapp.helpers.extensions

class StringExtensions {
    companion object {
        fun String.toNormalizedDecimalString(): String = toBigDecimalOrNull()?.toPlainString() ?: this

        fun String.toNormalizedIntegerString(): String = toIntOrNull()?.toString() ?: this
    }
}
