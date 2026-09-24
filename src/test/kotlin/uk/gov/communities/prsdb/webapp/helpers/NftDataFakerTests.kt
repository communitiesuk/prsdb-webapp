package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.MonthDay
import kotlin.test.assertEquals
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
    fun `generateAnniversary falls between the landlord's created date and the reference date`() {
        // Arrange
        NftDataFaker.reset(seed = TEST_SEED, reference = MIDYEAR_REFERENCE)
        val landlordCreatedDate = Timestamp.from(Instant.parse("2025-06-05T12:00:00Z"))

        // Act
        val anniversaries = List(SAMPLE_SIZE) { NftDataFaker.generateAnniversary(landlordCreatedDate) }

        // Assert
        anniversaries.forEach {
            assertTrue(it in MonthDay.of(6, 5)..MonthDay.of(6, 15), "Expected $it to be between 5 and 15 June")
        }
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

    companion object {
        private const val TEST_SEED = 239L
        private const val SAMPLE_SIZE = 10_000
        private const val FAKER_SAMPLE_SIZE = 500
        private val FIXED_REFERENCE: Instant = Instant.parse("2025-01-01T00:00:00Z")
        private val MIDYEAR_REFERENCE: Instant = Instant.parse("2025-06-15T12:00:00Z")
    }
}
