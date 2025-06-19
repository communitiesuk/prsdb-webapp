package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate

class PropertyComplianceTests {
    @Test
    fun `Calculated expiryDate values are correct when issue dates exist`() {
        val arbitraryIssueDate = LocalDate.of(2023, 1, 1)
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert(arbitraryIssueDate)
                .withEicr(arbitraryIssueDate)
                .build()

        val expectedGasExpiryDate = arbitraryIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())
        assertEquals(expectedGasExpiryDate, propertyCompliance.gasSafetyCertExpiryDate)

        val expectedEicrExpiryDate = arbitraryIssueDate.plusYears(EICR_VALIDITY_YEARS.toLong())
        assertEquals(expectedEicrExpiryDate, propertyCompliance.eicrExpiryDate)
    }

    @Test
    fun `Calculated expiryDate values are null when issue dates do not exist`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        assertNull(propertyCompliance.gasSafetyCertExpiryDate)
        assertNull(propertyCompliance.eicrExpiryDate)
    }

    @ParameterizedTest(name = "{1} when expiry date {0}")
    @MethodSource("provideGasCertIssueDates")
    fun `isGasSafetyCertExpired is`(
        issueDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        val propertyCompliance = PropertyComplianceBuilder().withGasSafetyCert(issueDate).build()
        assertEquals(expectedIsExpired, propertyCompliance.isGasSafetyCertExpired)
    }

    @ParameterizedTest(name = "{1} when expiry date {0}")
    @MethodSource("provideEicrIssueDates")
    fun `isEicrExpired is`(
        issueDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        val propertyCompliance = PropertyComplianceBuilder().withEicr(issueDate).build()
        assertEquals(expectedIsExpired, propertyCompliance.isEicrExpired)
    }

    @ParameterizedTest(name = "{1} when expiry date {0}")
    @MethodSource("provideEpcExpiryDates")
    fun `isEpcExpired is`(
        expiryDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        val propertyCompliance = PropertyComplianceBuilder().withEpc(expiryDate).build()
        assertEquals(expectedIsExpired, propertyCompliance.isEpcExpired)
    }

    @ParameterizedTest(name = "{1} when certs {0}")
    @MethodSource("providePropertyCompliancesWithExpectedExpiryStatuses")
    fun `isXExpired returns`(
        propertyCompliance: PropertyCompliance,
        expectedIsXExpired: Boolean?,
    ) {
        assertEquals(expectedIsXExpired, propertyCompliance.isGasSafetyCertExpired)
        assertEquals(expectedIsXExpired, propertyCompliance.isEicrExpired)
        assertEquals(expectedIsXExpired, propertyCompliance.isEpcExpired)
    }

    @Test
    fun `isEpcExpired returns false when expiry date has passed but tenancy started before expiry`() {
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withEpc(expiryDate = LocalDate.now().minusYears(1))
                .withTenancyStartedBeforeEpcExpiry()
                .build()

        assertFalse(propertyCompliance.isEpcExpired!!)
    }

    @ParameterizedTest(name = "{1} when certs {0}")
    @MethodSource("providePropertyCompliancesWithExpectedMissingStatuses")
    fun `isXMissing returns`(
        propertyCompliance: PropertyCompliance,
        expectedIsXMissing: Boolean?,
    ) {
        assertEquals(expectedIsXMissing, propertyCompliance.isGasSafetyCertMissing)
        assertEquals(expectedIsXMissing, propertyCompliance.isEicrMissing)
        assertEquals(expectedIsXMissing, propertyCompliance.isEpcMissing)
    }

    @ParameterizedTest(name = "{1} when EPC rating {0}")
    @MethodSource("providePropertyCompliancesWithEpcRatingStatuses")
    fun `isEpcRatingLow returns`(
        propertyCompliance: PropertyCompliance,
        expectedIsEpcRatingLow: Boolean?,
    ) {
        assertEquals(expectedIsEpcRatingLow, propertyCompliance.isEpcRatingLow)
    }

    companion object {
        private val propertyComplianceStatuses =
            arrayOf(
                named("are in date", PropertyComplianceBuilder.createWithInDateCerts()),
                named("are expired", PropertyComplianceBuilder.createWithExpiredCerts()),
                named("have exemptions", PropertyComplianceBuilder.createWithCertExemptions()),
                named("are missing", PropertyComplianceBuilder.createWithMissingCerts()),
            )

        private val lowEpcNoMeesPropertyCompliance =
            PropertyComplianceBuilder()
                .withEpc()
                .withLowEpcRating()
                .build()

        private val lowEpcWithMeesPropertyCompliance =
            PropertyComplianceBuilder()
                .withEpc()
                .withLowEpcRating()
                .withMeesExemption()
                .build()

        @JvmStatic
        private fun provideGasCertIssueDates() =
            arrayOf(
                arguments(named("was yesterday", LocalDate.now().minusDays(1).minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())), true),
                arguments(named("is today", LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())), true),
                arguments(named("is tomorrow", LocalDate.now().plusDays(1).minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())), false),
            )

        @JvmStatic
        private fun provideEicrIssueDates() =
            arrayOf(
                arguments(named("was yesterday", LocalDate.now().minusDays(1).minusYears(EICR_VALIDITY_YEARS.toLong())), true),
                arguments(named("is today", LocalDate.now().minusYears(EICR_VALIDITY_YEARS.toLong())), true),
                arguments(named("is tomorrow", LocalDate.now().plusDays(1).minusYears(EICR_VALIDITY_YEARS.toLong())), false),
            )

        @JvmStatic
        private fun provideEpcExpiryDates() =
            arrayOf(
                arguments(named("was yesterday", LocalDate.now().minusDays(1)), true),
                arguments(named("is today", LocalDate.now()), false),
                arguments(named("is tomorrow", LocalDate.now().plusDays(1)), false),
            )

        @JvmStatic
        private fun providePropertyCompliancesWithExpectedExpiryStatuses() =
            propertyComplianceStatuses.withExpectedStatuses(arrayOf(false, true, null, null))

        @JvmStatic
        private fun providePropertyCompliancesWithExpectedMissingStatuses() =
            propertyComplianceStatuses.withExpectedStatuses(arrayOf(false, false, false, true))

        @JvmStatic
        private fun providePropertyCompliancesWithEpcRatingStatuses() =
            arrayOf(
                arguments(named("is high", PropertyComplianceBuilder().withEpc().build()), false),
                arguments(named("is low and there's a MEES exemption", lowEpcWithMeesPropertyCompliance), false),
                arguments(named("is low and there's no MEES exemption", lowEpcNoMeesPropertyCompliance), true),
                arguments(named("does not exist", PropertyComplianceBuilder().build()), null),
            )

        private fun Array<Named<PropertyCompliance>>.withExpectedStatuses(expectedStatuses: Array<Boolean?>) =
            this.zip(expectedStatuses).map { (namedCompliance, expectedStatus) -> arguments(namedCompliance, expectedStatus) }
    }
}
