package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.constants.EIC_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.MonthDay
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NftDataFakerTests {
    @Test
    fun `same seed produces the same scenario sequence`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val first = List(SAMPLE_SIZE) { NftDataFaker.generatePropertyScenario() }

        NftDataFaker.reset(seed = TEST_SEED)
        val second = List(SAMPLE_SIZE) { NftDataFaker.generatePropertyScenario() }

        assertEquals(first, second)
    }

    @Test
    fun `different seeds produce different scenario sequences`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val first = List(SAMPLE_SIZE) { NftDataFaker.generatePropertyScenario() }

        NftDataFaker.reset(seed = TEST_SEED + 1)
        val second = List(SAMPLE_SIZE) { NftDataFaker.generatePropertyScenario() }

        assertNotEquals(first, second)
    }

    @Test
    fun `scenario sample contains each supported property state`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val scenarios = List(SAMPLE_SIZE) { NftDataFaker.generatePropertyScenario() }

        assertTrue(scenarios.any { !it.isRegistrationComplete })
        assertTrue(scenarios.any { it.isRegistrationComplete && it.isOccupied })
        assertTrue(scenarios.any { it.isRegistrationComplete && !it.isOccupied })
        assertTrue(scenarios.any { it.isRegistrationComplete && it.hasLicence })
        assertTrue(scenarios.any { it.isRegistrationComplete && !it.hasLicence && it.licenseProvideLater == true })
        assertTrue(scenarios.any { it.isRegistrationComplete && it.isOccupied && it.tenancyProvideLater == true })
    }

    @Test
    fun `scenario states preserve property invariants`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val scenarios = List(SAMPLE_SIZE) { NftDataFaker.generatePropertyScenario() }

        scenarios.forEach {
            if (!it.isRegistrationComplete) {
                assertEquals(false, it.isOccupied)
                assertEquals(false, it.hasLicence)
            }
            if (!it.isOccupied) {
                assertEquals(null, it.tenancyProvideLater)
            }
            if (it.hasLicence) {
                assertEquals(null, it.licenseProvideLater)
            }
        }
    }

    @Test
    fun `same seed produces the same faker-generated values`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val first = generateFakerSample()

        NftDataFaker.reset(seed = TEST_SEED)
        val second = generateFakerSample()

        assertEquals(first, second)
    }

    @Test
    fun `different seeds produce different faker-generated values`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val first = generateFakerSample()

        NftDataFaker.reset(seed = TEST_SEED + 1)
        val second = generateFakerSample()

        assertNotEquals(first, second)
    }

    @Test
    fun `same seed and reference produce the same generated dates`() {
        NftDataFaker.reset(seed = TEST_SEED, reference = FIXED_REFERENCE)
        val first = List(FAKER_SAMPLE_SIZE) { NftDataFaker.generateCreatedDate().toString() }

        NftDataFaker.reset(seed = TEST_SEED, reference = FIXED_REFERENCE)
        val second = List(FAKER_SAMPLE_SIZE) { NftDataFaker.generateCreatedDate().toString() }

        assertEquals(first, second)
    }

    @Test
    fun `generateDateOfBirth returns dates between 18 and 120 years before the reference date`() {
        val datesOfBirth = generateDatesOfBirth()

        val referenceDate = LocalDate.of(2025, 1, 1)
        assertTrue(datesOfBirth.all { it >= referenceDate.minusYears(120) && it < referenceDate.minusYears(18) })
    }

    @Test
    fun `generateDateOfBirth returns the same dates whatever the default time zone`() {
        val datesOfBirthInUtcPlus14 = withDefaultTimeZone("Pacific/Kiritimati") { generateDatesOfBirth() }
        val datesOfBirthInUtcMinus12 = withDefaultTimeZone("Etc/GMT+12") { generateDatesOfBirth() }

        assertEquals(datesOfBirthInUtcPlus14, datesOfBirthInUtcMinus12)
    }

    @Test
    fun `generateLastEmailReminderSentDate returns the same instant whatever the default time zone`() {
        val createdDateBeforeClocksGoBack = Timestamp.from(Instant.parse("2025-10-20T12:00:00Z"))

        val ukReminderDate =
            withDefaultTimeZone("Europe/London") { NftDataFaker.generateLastEmailReminderSentDate(createdDateBeforeClocksGoBack) }
        val utcReminderDate = withDefaultTimeZone("UTC") { NftDataFaker.generateLastEmailReminderSentDate(createdDateBeforeClocksGoBack) }

        assertEquals(utcReminderDate, ukReminderDate)
    }

    @Test
    fun `generatePropertyComplianceData stores EIC issue dates and EICR expiry dates`() {
        NftDataFaker.reset(seed = TEST_SEED, reference = FIXED_REFERENCE)
        val sample = List(FAKER_SAMPLE_SIZE) { NftDataFaker.generatePropertyComplianceData(Timestamp.from(FIXED_REFERENCE)) }

        sample.forEach {
            when (it.electricalCertType) {
                CertificateType.Eic -> {
                    assertTrue(it.eicIssueDate != null && it.eicrExpiryDate == null)
                    assertEquals(
                        it.eicIssueDate!!.toLocalDate().plusYears(EIC_VALIDITY_YEARS.toLong()),
                        it.electricalSafetyExpiryDate!!.toLocalDate(),
                    )
                }

                CertificateType.Eicr -> assertTrue(it.eicIssueDate == null && it.eicrExpiryDate != null)
                else -> assertTrue(it.eicIssueDate == null && it.eicrExpiryDate == null)
            }
        }
        assertTrue(sample.any { it.electricalCertType == CertificateType.Eic })
        assertTrue(sample.any { it.electricalCertType == CertificateType.Eicr })
    }

    @Test
    fun `generateRenewalDate returns next year's anniversary when this year's has passed`() {
        // Arrange
        NftDataFaker.reset(seed = TEST_SEED, reference = MIDYEAR_REFERENCE)

        // Act
        val renewalDate = NftDataFaker.generateRenewalDate(MonthDay.of(3, 10))

        // Assert
        assertEquals(LocalDate.of(2026, 3, 10), renewalDate.toLocalDate())
    }

    @Test
    fun `generateRenewalDate returns this year's anniversary when it is still to come`() {
        // Arrange
        NftDataFaker.reset(seed = TEST_SEED, reference = MIDYEAR_REFERENCE)

        // Act
        val renewalDate = NftDataFaker.generateRenewalDate(MonthDay.of(9, 1))

        // Assert
        assertEquals(LocalDate.of(2025, 9, 1), renewalDate.toLocalDate())
    }

    @Test
    fun `isGasSafetyCertificateCurrent returns true on the expiry date 14 months after the issue date`() {
        NftDataFaker.reset(seed = TEST_SEED, reference = MIDYEAR_REFERENCE)

        assertTrue(NftDataFaker.isGasSafetyCertificateCurrent(Date.valueOf(LocalDate.of(2024, 4, 15))))
    }

    @Test
    fun `isGasSafetyCertificateCurrent returns false the day after the expiry date 14 months after the issue date`() {
        NftDataFaker.reset(seed = TEST_SEED, reference = MIDYEAR_REFERENCE)

        assertFalse(NftDataFaker.isGasSafetyCertificateCurrent(Date.valueOf(LocalDate.of(2024, 4, 14))))
    }

    @Test
    fun `isElectricalSafetyCertificateCurrent returns true on the expiry date`() {
        listOf(CertificateType.Eic, CertificateType.Eicr).forEach { certType ->
            assertTrue(
                NftDataFaker.isElectricalSafetyCertificateCurrent(
                    Date.valueOf(LocalDate.of(2025, 6, 15)),
                    certType,
                    Timestamp.from(MIDYEAR_REFERENCE),
                ),
            )
        }
    }

    @Test
    fun `isElectricalSafetyCertificateCurrent returns true the day before the expiry date`() {
        listOf(CertificateType.Eic, CertificateType.Eicr).forEach { certType ->
            assertTrue(
                NftDataFaker.isElectricalSafetyCertificateCurrent(
                    Date.valueOf(LocalDate.of(2025, 6, 16)),
                    certType,
                    Timestamp.from(MIDYEAR_REFERENCE),
                ),
            )
        }
    }

    @Test
    fun `isElectricalSafetyCertificateCurrent throws when the cert type is missing`() {
        assertThrows<IllegalArgumentException> {
            NftDataFaker.isElectricalSafetyCertificateCurrent(
                Date.valueOf(LocalDate.of(2025, 6, 16)),
                null,
                Timestamp.from(MIDYEAR_REFERENCE),
            )
        }
    }

    @Test
    fun `generateOrganisationalLandlordUsers always makes the first (registrant) user the admin`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val orgCreatedDate = Timestamp.from(FIXED_REFERENCE)

        repeat(SAMPLE_SIZE / 100) {
            val users =
                NftDataFaker.generateOrganisationalLandlordUsers(
                    registrantSubjectId = "registrant-subject-id",
                    registrantName = "Registrant Name",
                    registrantEmail = "registrant@example.com",
                    orgCreatedDate = orgCreatedDate,
                )

            assertTrue(users.isNotEmpty())
            val registrant = users.first()
            assertEquals("registrant-subject-id", registrant.subjectId)
            assertEquals("Registrant Name", registrant.name)
            assertEquals("registrant@example.com", registrant.email)
            assertEquals(orgCreatedDate, registrant.createdDate)
            assertEquals(OrganisationalLandlordUserRole.ADMIN, registrant.role)
        }
    }

    @Test
    fun `generateOrganisationalLandlordUsers sample contains organisations with and without extra users, and both roles`() {
        NftDataFaker.reset(seed = TEST_SEED)
        val orgCreatedDate = Timestamp.from(FIXED_REFERENCE)

        val samples =
            List(SAMPLE_SIZE / 100) {
                NftDataFaker.generateOrganisationalLandlordUsers(
                    registrantSubjectId = "registrant-subject-id-$it",
                    registrantName = "Registrant Name",
                    registrantEmail = "registrant@example.com",
                    orgCreatedDate = orgCreatedDate,
                )
            }

        assertTrue(samples.any { it.size == 1 }, "Expected at least one organisation with no extra users")
        assertTrue(samples.any { it.size > 1 }, "Expected at least one organisation with extra users")
        assertTrue(
            samples.flatMap { it.drop(1) }.any { it.role == OrganisationalLandlordUserRole.EDITOR },
            "Expected at least one extra user with the EDITOR role",
        )
    }

    @Test
    fun `generated associated parties are either named with one contact method or have no details at all`() {
        NftDataFaker.reset(seed = TEST_SEED)

        val parties = List(SAMPLE_SIZE / 100) { NftDataFaker.generateFreeholders() + NftDataFaker.generateSuperiorLandlords() }.flatten()

        parties.forEach { party ->
            if (party.name == null) {
                assertFalse(party.email != null || party.hasAddress, "Expected a party with no name to have no email or address")
            } else {
                assertNotEquals(
                    party.email != null,
                    party.hasAddress,
                    "Expected a named party to have exactly one of an email and an address",
                )
            }
        }

        assertTrue(parties.any { it.name == null }, "Expected at least one party with no details")
        assertTrue(parties.any { it.email != null }, "Expected at least one party contactable by email")
        assertTrue(parties.any { it.hasAddress }, "Expected at least one party contactable by post")
    }

    private fun generateFakerSample(): List<String> =
        List(FAKER_SAMPLE_SIZE) {
            listOf(
                NftDataFaker.generateNumberOfPropertiesForLandlord(),
                NftDataFaker.generateNumberLessThan(1000),
                NftDataFaker.generateBoolean(),
                NftDataFaker.generateInvitationToken(),
                NftDataFaker.pickOne(listOf(1, 2, 3, 4, 5)),
                NftDataFaker.shuffle(listOf(1, 2, 3, 4, 5)),
                NftDataFaker.generateStandardAndCustomBillsIncluded(),
            ).joinToString("|")
        }

    private fun generateDatesOfBirth(): List<LocalDate> {
        NftDataFaker.reset(seed = TEST_SEED, reference = FIXED_REFERENCE)
        return List(FAKER_SAMPLE_SIZE) { NftDataFaker.generateDateOfBirth().toLocalDate() }
    }

    private fun <T> withDefaultTimeZone(
        timeZoneId: String,
        block: () -> T,
    ): T {
        val originalTimeZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
            return block()
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }

    companion object {
        private const val TEST_SEED = 239L
        private const val SAMPLE_SIZE = 10_000
        private const val FAKER_SAMPLE_SIZE = 500
        private val FIXED_REFERENCE: Instant = Instant.parse("2025-01-01T00:00:00Z")
        private val MIDYEAR_REFERENCE: Instant = Instant.parse("2025-06-15T12:00:00Z")
    }
}
