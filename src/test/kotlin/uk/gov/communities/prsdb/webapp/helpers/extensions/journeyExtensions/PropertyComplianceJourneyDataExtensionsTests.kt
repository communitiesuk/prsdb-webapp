package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PropertyComplianceJourneyDataExtensionsTests {
    companion object {
        // currentDate is an arbitrary date
        private val currentDate = LocalDate.of(2020, 1, 5).toKotlinLocalDate()

        @JvmStatic
        private fun provideGasSafetyCertIssueDates() =
            arrayOf(
                Arguments.of(Named.of("over a year old", LocalDate.of(2019, 1, 4)), true),
                Arguments.of(Named.of("a year old", LocalDate.of(2019, 1, 5)), true),
                Arguments.of(Named.of("less than a year old", LocalDate.of(2019, 1, 6)), false),
            )

        @JvmStatic
        private fun provideGasExemptionReasons() =
            arrayOf(
                Arguments.of(Named.of("other", GasSafetyExemptionReason.OTHER), true),
                Arguments.of(Named.of("not other", GasSafetyExemptionReason.NO_GAS_SUPPLY), false),
            )

        @JvmStatic
        private fun provideEicrIssueDates() =
            arrayOf(
                Arguments.of(Named.of("over 5 years old", LocalDate.of(2015, 1, 4)), true),
                Arguments.of(Named.of("5 years old", LocalDate.of(2015, 1, 5)), true),
                Arguments.of(Named.of("less than 5 years old", LocalDate.of(2015, 1, 6)), false),
            )

        @JvmStatic
        private fun provideEicrExemptionReasons() =
            arrayOf(
                Arguments.of(Named.of("other", EicrExemptionReason.OTHER), true),
                Arguments.of(Named.of("not other", EicrExemptionReason.LIVE_IN_LANDLORD), false),
            )
    }

    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock())
    }

    @Test
    fun `getHasGasSafetyCert returns a boolean if the corresponding page is in journeyData`() {
        val hasGasSafetyCert = true
        val testJourneyData = journeyDataBuilder.withGasSafetyCertStatus(hasGasSafetyCert).build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasGasSafetyCert()

        assertEquals(hasGasSafetyCert, retrievedHasGasSafetyCert)
    }

    @Test
    fun `getHasGasSafetyCert returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasGasSafetyCert()

        assertNull(retrievedHasGasSafetyCert)
    }

    @ParameterizedTest(name = "{1} when the certificate is {0}")
    @MethodSource("provideGasSafetyCertIssueDates")
    fun `getIsGasSafetyCertOutdated returns`(
        issueDate: LocalDate,
        expectedResult: Boolean,
    ) {
        mockConstruction(DateTimeHelper::class.java) { mock, _ -> whenever(mock.getCurrentDateInUK()).thenReturn(currentDate) }
            .use {
                val testJourneyData = journeyDataBuilder.withGasSafetyIssueDate(issueDate).build()

                val retrievedIsGasSafetyCertOutdated = testJourneyData.getIsGasSafetyCertOutdated()

                assertEquals(expectedResult, retrievedIsGasSafetyCertOutdated)
            }
    }

    @Test
    fun `getIsGasSafetyCertOutdated returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedIsGasSafetyCertOutdated = testJourneyData.getIsGasSafetyCertOutdated()

        assertNull(retrievedIsGasSafetyCertOutdated)
    }

    @Test
    fun `getHasGasSafetyCertExemption returns a boolean if the corresponding page is in journeyData`() {
        val hasGasSafetyCertExemption = true
        val testJourneyData = journeyDataBuilder.withGasSafetyCertExemptionStatus(hasGasSafetyCertExemption).build()

        val retrievedHasGasSafetyCertExemption = testJourneyData.getHasGasSafetyCertExemption()

        assertEquals(hasGasSafetyCertExemption, retrievedHasGasSafetyCertExemption)
    }

    @Test
    fun `getHasGasSafetyCertExemption returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasGasSafetyCertExemption = testJourneyData.getHasGasSafetyCertExemption()

        assertNull(retrievedHasGasSafetyCertExemption)
    }

    @ParameterizedTest(name = "{1} when the reason is {0}")
    @MethodSource("provideGasExemptionReasons")
    fun `getIsGasSafetyExemptionReasonOther returns`(
        reason: GasSafetyExemptionReason,
        expectedResult: Boolean,
    ) {
        val testJourneyData = journeyDataBuilder.withGasSafetyCertExemptionReason(reason).build()

        val retrievedIsGasSafetyCertExemptionReasonOther = testJourneyData.getIsGasSafetyExemptionReasonOther()!!

        assertEquals(expectedResult, retrievedIsGasSafetyCertExemptionReasonOther)
    }

    @Test
    fun `getIsGasSafetyExemptionReasonOther returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedIsGasSafetyCertExemptionReasonOther = testJourneyData.getIsGasSafetyExemptionReasonOther()

        assertNull(retrievedIsGasSafetyCertExemptionReasonOther)
    }

    @Test
    fun `getHasEICR returns a boolean if the corresponding page is in journeyData`() {
        val hasEICR = true
        val testJourneyData = journeyDataBuilder.withEicrStatus(hasEICR).build()

        val retrievedHasEICR = testJourneyData.getHasEICR()

        assertEquals(hasEICR, retrievedHasEICR)
    }

    @Test
    fun `getHasEICR returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasEICR = testJourneyData.getHasEICR()

        assertNull(retrievedHasEICR)
    }

    @ParameterizedTest(name = "{1} when the EICR is {0}")
    @MethodSource("provideEicrIssueDates")
    fun `getIsEicrOutdated returns`(
        issueDate: LocalDate,
        expectedResult: Boolean,
    ) {
        mockConstruction(DateTimeHelper::class.java) { mock, _ -> whenever(mock.getCurrentDateInUK()).thenReturn(currentDate) }
            .use {
                val testJourneyData = journeyDataBuilder.withEicrIssueDate(issueDate).build()

                val retrievedIsEicrOutdated = testJourneyData.getIsEicrOutdated()

                assertEquals(expectedResult, retrievedIsEicrOutdated)
            }
    }

    @Test
    fun `getIsEicrOutdated returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedIsEicrOutdated = testJourneyData.getIsEicrOutdated()

        assertNull(retrievedIsEicrOutdated)
    }

    @Test
    fun `getHasEicrExemption returns a boolean if the corresponding page is in journeyData`() {
        val hasEicrExemption = true
        val testJourneyData = journeyDataBuilder.withEicrExemptionStatus(hasEicrExemption).build()

        val retrievedHasEicrExemption = testJourneyData.getHasEicrExemption()

        assertEquals(hasEicrExemption, retrievedHasEicrExemption)
    }

    @Test
    fun `getHasEicrExemption returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasEicrExemption = testJourneyData.getHasEicrExemption()

        assertNull(retrievedHasEicrExemption)
    }

    @ParameterizedTest(name = "{1} when the reason is {0}")
    @MethodSource("provideEicrExemptionReasons")
    fun `getIsEicrExemptionReasonOther returns`(
        reason: EicrExemptionReason,
        expectedResult: Boolean,
    ) {
        val testJourneyData = journeyDataBuilder.withEicrExemptionReason(reason).build()

        val retrievedIsEicrExemptionReasonOther = testJourneyData.getIsEicrExemptionReasonOther()!!

        assertEquals(expectedResult, retrievedIsEicrExemptionReasonOther)
    }

    @Test
    fun `getIsEicrExemptionReasonOther returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedIsEicrExemptionReasonOther = testJourneyData.getIsEicrExemptionReasonOther()

        assertNull(retrievedIsEicrExemptionReasonOther)
    }
}
