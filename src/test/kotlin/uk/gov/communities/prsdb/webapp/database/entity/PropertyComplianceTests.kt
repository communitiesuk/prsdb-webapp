package uk.gov.communities.prsdb.webapp.database.entity

import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_MONTHS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import java.time.LocalDate

class PropertyComplianceTests {
    @Test
    fun `Calculated expiryDate values are correct when dates exist`() {
        val arbitraryIssueDate = LocalDate.of(2023, 1, 1)
        val arbitraryElectricalSafetyExpiryDate = LocalDate.of(2028, 1, 1)
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withGasSafetyCert(arbitraryIssueDate)
                .withElectricalSafety(arbitraryElectricalSafetyExpiryDate)
                .build()

        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        val expectedGasExpiryDate = arbitraryIssueDate.plusMonths(GAS_SAFETY_CERT_VALIDITY_MONTHS.toLong())
        assertEquals(expectedGasExpiryDate, propertyCompliance.getGasSafetyCertExpiryDate(isGas14MonthExpiryEnabled = true))
        assertEquals(arbitraryElectricalSafetyExpiryDate, propertyCompliance.electricalSafetyExpiryDate)
    }

    @Test
    fun `Calculated expiryDate values are null when issue dates do not exist`() {
        val propertyCompliance = PropertyComplianceBuilder().build()

        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        assertNull(propertyCompliance.getGasSafetyCertExpiryDate(isGas14MonthExpiryEnabled = true))
        assertNull(propertyCompliance.electricalSafetyExpiryDate)
    }

    @ParameterizedTest(name = "{1} when expiry date {0}")
    @MethodSource("provideGasCertExpiryDates")
    fun `isGasSafetyCertExpired is`(
        expiryDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        val issueDate = expiryDate.minusMonths(GAS_SAFETY_CERT_VALIDITY_MONTHS.toLong())
        // Month lengths vary, so some dates e.g. 31st January will clamp to November 30th when subtracting 14 months
        // In the codebase this is fine, but it means that if a test is run on 31st January then:
        //   Today - 14 months == 30th November
        //   Yesterday - 14 months == 30th November
        // So now our test isn't actually testing anything!
        // This is quite a rare edge case, so just skip the test if any clamping happened when working out the expiry date
        assumeTrue(
            issueDate.plusMonths(GAS_SAFETY_CERT_VALIDITY_MONTHS.toLong()) == expiryDate,
            "No gas certificate issue date expires on $expiryDate",
        )

        val propertyCompliance = PropertyComplianceBuilder().withGasSafetyCert(issueDate).build()
        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        assertEquals(expectedIsExpired, propertyCompliance.isGasSafetyCertExpired(isGas14MonthExpiryEnabled = true))
    }

    @ParameterizedTest(name = "{1} when expiry date {0}")
    @MethodSource("provideElectricalSafetyExpiryDates")
    fun `isElectricalSafetyExpired is`(
        expiryDate: LocalDate,
        expectedIsExpired: Boolean,
    ) {
        listOf(CertificateType.Eic, CertificateType.Eicr).forEach { certType ->
            val propertyCompliance =
                PropertyComplianceBuilder()
                    .withElectricalSafety(expiryDate)
                    .withElectricalCertType(certType)
                    .build()
            assertEquals(expectedIsExpired, propertyCompliance.isElectricalSafetyExpired, "for $certType")
        }
    }

    @Test
    fun `isElectricalSafetyExpired throws when the electrical cert type is missing`() {
        val propertyCompliance = PropertyComplianceBuilder().withElectricalSafety(currentDate.minusYears(1), certType = null).build()

        assertThrows<PrsdbWebException> { propertyCompliance.isElectricalSafetyExpired }
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
        // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled argument
        assertEquals(expectedIsXExpired, propertyCompliance.isGasSafetyCertExpired(isGas14MonthExpiryEnabled = true))
        assertEquals(expectedIsXExpired, propertyCompliance.isElectricalSafetyExpired)
        assertEquals(expectedIsXExpired, propertyCompliance.isEpcExpired)
    }

    @Test
    fun `isEpcExpired returns true when expiry date has passed even if tenancy started before expiry`() {
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withEpc(expiryDate = currentDate.minusYears(1))
                .withTenancyStartedBeforeEpcExpiry()
                .build()

        assertTrue(propertyCompliance.isEpcExpired!!)
    }

    @ParameterizedTest(name = "{1} when certs {0}")
    @MethodSource("providePropertyCompliancesWithExpectedMissingOrHasFaultsStatuses")
    fun `isXMissing or xHasFaults returns`(
        propertyCompliance: PropertyCompliance,
        expectedIsXMissing: Boolean?,
    ) {
        assertEquals(expectedIsXMissing, propertyCompliance.isGasSafetyCertMissing)
        assertEquals(expectedIsXMissing, propertyCompliance.isElectricalSafetyMissing)
        assertEquals(expectedIsXMissing, propertyCompliance.epcHasFaults)
    }

    @Test
    fun `epcHasFaults returns true when EPC rating is low and there's no MEES exemption`() {
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withEpc()
                .withLowEpcRating()
                .build()
        assertTrue(propertyCompliance.epcHasFaults)
    }

    @Test
    fun `epcHasFaults returns false when EPC rating is low and there is a MEES exemption`() {
        val propertyCompliance =
            PropertyComplianceBuilder()
                .withEpc()
                .withLowEpcRating()
                .withMeesExemption()
                .build()
        assertFalse(propertyCompliance.epcHasFaults)
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
        private val currentDate = DateTimeHelper().getCurrentDateInUK().toJavaLocalDate()

        private val propertyComplianceStatuses =
            arrayOf(
                named("are in date", PropertyComplianceBuilder.createWithInDateCerts()),
                named("are expired", PropertyComplianceBuilder.createWithExpiredCerts()),
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
        private fun provideGasCertExpiryDates() =
            arrayOf(
                arguments(named("was yesterday", currentDate.minusDays(1)), true),
                arguments(named("is today", currentDate), false),
                arguments(named("is tomorrow", currentDate.plusDays(1)), false),
            )

        @JvmStatic
        private fun provideElectricalSafetyExpiryDates() =
            arrayOf(
                arguments(named("was yesterday", currentDate.minusDays(1)), true),
                arguments(named("is today", currentDate), true),
                arguments(named("is tomorrow", currentDate.plusDays(1)), false),
            )

        @JvmStatic
        private fun provideEpcExpiryDates() =
            arrayOf(
                arguments(named("was yesterday", currentDate.minusDays(1)), true),
                arguments(named("is today", currentDate), false),
                arguments(named("is tomorrow", currentDate.plusDays(1)), false),
            )

        @JvmStatic
        private fun providePropertyCompliancesWithExpectedExpiryStatuses() =
            propertyComplianceStatuses.withExpectedStatuses(arrayOf(false, true, null))

        @JvmStatic
        private fun providePropertyCompliancesWithExpectedMissingOrHasFaultsStatuses() =
            propertyComplianceStatuses.withExpectedStatuses(arrayOf(false, false, true))

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
