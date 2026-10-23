package uk.gov.communities.prsdb.webapp.journeyMap.discovery

import java.time.LocalDate

data class SeededAddress(
    val postcode: String,
    val buildingNameOrNumber: String,
)

enum class InputKind {
    DATE_DAY,
    DATE_MONTH,
    DATE_YEAR,
    POSTCODE,
    BUILDING,
    EMAIL,
    PHONE,
    PASSWORD,
    PERSON_NAME,
    ORGANISATION_NAME,
    ADDRESS_LINE,
    TOWN,
    MONEY,
    INTEGER,
    CERTIFICATE_NUMBER,
    IDENTIFIER,
    TEXT,
    ;

    val isDatePart: Boolean get() = this == DATE_DAY || this == DATE_MONTH || this == DATE_YEAR
}

class ValueGenerator(
    private val today: LocalDate,
    private val address: SeededAddress,
) {
    fun kindOf(
        field: FormField,
        validators: List<String> = emptyList(),
    ): InputKind {
        val name = (field.name ?: field.id ?: "").lowercase().substringBefore('[')
        val type = field.type.lowercase()
        val autocomplete = field.autocomplete?.lowercase() ?: ""
        datePartKind(name, field)?.let { return it }
        return when {
            name.contains("postcode") || autocomplete == "postal-code" -> InputKind.POSTCODE
            buildingWords.any { name.contains(it) } -> InputKind.BUILDING
            type == "email" || name.contains("email") -> InputKind.EMAIL
            type == "tel" || name.contains("phone") -> InputKind.PHONE
            type == "password" || name.contains("password") -> InputKind.PASSWORD
            name.contains("addressline") -> InputKind.ADDRESS_LINE
            name.contains("town") || name.contains("city") -> InputKind.TOWN
            name.contains("name") && organisationWords.any { name.contains(it) } -> InputKind.ORGANISATION_NAME
            name.contains("name") -> InputKind.PERSON_NAME
            name.contains("certificatenumber") -> InputKind.CERTIFICATE_NUMBER
            validators.any { it.contains("BigDecimal") } || name.contains("amount") || name.contains("rent") -> InputKind.MONEY
            validators.any { it.contains("PositiveInteger") } || type == "number" || field.inputmode == "numeric" ||
                name.startsWith("number") -> InputKind.INTEGER
            validators.any { identifierValidators.any(it::contains) } || name.contains("number") -> InputKind.IDENTIFIER
            else -> InputKind.TEXT
        }
    }

    fun valuesFor(kind: InputKind): List<String> =
        when (kind) {
            InputKind.POSTCODE -> listOf(address.postcode, "ZZ9 9ZZ")
            InputKind.BUILDING -> listOf(address.buildingNameOrNumber)
            InputKind.EMAIL -> listOf("jane.smith@example.com", "sam.jones@example.com", "alex.taylor@example.com")
            InputKind.PHONE -> listOf("07123456789")
            // The local seed data's letting agent password hashes are for this password
            InputKind.PASSWORD -> listOf("Password123!")
            InputKind.PERSON_NAME -> listOf("Jane Smith")
            InputKind.ORGANISATION_NAME -> listOf("Example Lettings Ltd")
            InputKind.ADDRESS_LINE -> listOf("1 Example Street")
            InputKind.TOWN -> listOf("Exampleton")
            InputKind.MONEY -> listOf("850")
            InputKind.INTEGER -> listOf("2", "1", "0", "10")
            InputKind.CERTIFICATE_NUMBER -> listOf("0000-0000-0000-0892-1563")
            InputKind.IDENTIFIER -> listOf("12345678", "1234567", "123456", "SC012345")
            InputKind.TEXT -> listOf("Example text")
            InputKind.DATE_DAY, InputKind.DATE_MONTH, InputKind.DATE_YEAR -> emptyList()
        }

    // The page context, such as its URL, helps when the date group has a generic name
    fun datesFor(
        groupName: String,
        pageContext: String = "",
    ): List<LocalDate> {
        val name = "$groupName $pageContext".lowercase()
        return when {
            name.contains("birth") || name.contains("dob") -> listOf(LocalDate.of(1980, 1, 1), today.minusYears(10))
            name.contains("expiry") ->
                listOf(today.plusYears(1), today.minusMonths(1), today.minusYears(2), today.minusYears(11), today.minusYears(20), today)
            else -> listOf(today.minusMonths(1), today.plusYears(1), today.minusYears(2), today.minusYears(11), today.minusYears(20), today)
        }
    }

    fun datePart(
        kind: InputKind,
        date: LocalDate,
    ): String =
        when (kind) {
            InputKind.DATE_DAY -> date.dayOfMonth.toString()
            InputKind.DATE_MONTH -> date.monthValue.toString()
            InputKind.DATE_YEAR -> date.year.toString()
            else -> throw IllegalArgumentException("$kind is not part of a date")
        }

    private fun datePartKind(
        name: String,
        field: FormField,
    ): InputKind? {
        if (field.dateGroup == null && name !in setOf("day", "month", "year")) return null
        return when {
            name.endsWith("day") -> InputKind.DATE_DAY
            name.endsWith("month") -> InputKind.DATE_MONTH
            name.endsWith("year") -> InputKind.DATE_YEAR
            else -> null
        }
    }

    companion object {
        private val buildingWords = listOf("housenameornumber", "buildingnameornumber", "housenumber")
        private val organisationWords = listOf("org", "company", "charity", "business", "trust")
        private val identifierValidators = listOf("DigitsOnly", "Alphanumeric", "GasSafe")
    }
}
