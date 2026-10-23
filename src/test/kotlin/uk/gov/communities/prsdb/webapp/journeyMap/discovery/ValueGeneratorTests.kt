package uk.gov.communities.prsdb.webapp.journeyMap.discovery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ValueGeneratorTests {
    private val today = LocalDate.of(2026, 10, 20)
    private val generator = ValueGenerator(today, SeededAddress("EG1 2AA", "1"))

    @Test
    fun `infers input kinds from field names, types and validators`() {
        assertEquals(InputKind.POSTCODE, generator.kindOf(field("postcode")))
        assertEquals(InputKind.BUILDING, generator.kindOf(field("houseNameOrNumber")))
        assertEquals(InputKind.EMAIL, generator.kindOf(field("emailAddress", type = "email")))
        assertEquals(InputKind.PHONE, generator.kindOf(field("phoneNumber", type = "tel")))
        assertEquals(InputKind.ORGANISATION_NAME, generator.kindOf(field("orgName")))
        assertEquals(InputKind.PERSON_NAME, generator.kindOf(field("name")))
        assertEquals(InputKind.INTEGER, generator.kindOf(field("numberOfHouseholds"), listOf("PositiveIntegerValidator")))
        assertEquals(InputKind.MONEY, generator.kindOf(field("rentAmount")))
        assertEquals(InputKind.CERTIFICATE_NUMBER, generator.kindOf(field("certificateNumber")))
        assertEquals(InputKind.DATE_DAY, generator.kindOf(field("day", dateGroup = "date")))
        assertEquals(InputKind.DATE_YEAR, generator.kindOf(field("year", dateGroup = "date")))
        assertEquals(InputKind.TEXT, generator.kindOf(field("somethingElse")))
    }

    @Test
    fun `postcode values start with the seeded address and then a postcode with no addresses`() {
        assertEquals(listOf("EG1 2AA", "ZZ9 9ZZ"), generator.valuesFor(InputKind.POSTCODE))
    }

    @Test
    fun `certificate numbers are 20 digits in groups of four`() {
        assertEquals(listOf("0000-0000-0000-0892-1563"), generator.valuesFor(InputKind.CERTIFICATE_NUMBER))
    }

    @Test
    fun `date groups start with a birth date when the group looks like a date of birth`() {
        assertEquals(LocalDate.of(1980, 1, 1), generator.datesFor("dateOfBirth").first())
        assertEquals(today.minusMonths(1), generator.datesFor("issueDate").first())
    }

    @Test
    fun `expiry dates start in the future so that certificates are valid`() {
        assertEquals(today.plusYears(1), generator.datesFor("expiryDate").first())
        assertEquals(today.plusYears(1), generator.datesFor("date", "http://localhost/certificate-expiry-date").first())
        assertTrue(today.minusMonths(1) in generator.datesFor("expiryDate"))
    }

    @Test
    fun `date values are split into day, month and year`() {
        val date = LocalDate.of(2024, 3, 7)

        assertEquals("7", generator.datePart(InputKind.DATE_DAY, date))
        assertEquals("3", generator.datePart(InputKind.DATE_MONTH, date))
        assertEquals("2024", generator.datePart(InputKind.DATE_YEAR, date))
    }

    private fun field(
        name: String,
        type: String = "text",
        dateGroup: String? = null,
    ) = FormField(name = name, id = name, type = type, dateGroup = dateGroup)
}
