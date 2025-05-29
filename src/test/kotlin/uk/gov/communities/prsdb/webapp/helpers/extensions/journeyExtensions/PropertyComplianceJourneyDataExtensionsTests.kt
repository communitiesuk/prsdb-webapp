package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.ALLOW_CHECK_MATCHED_EPC_TO_BE_BYPASSED
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasFireSafetyDeclaration
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.resetCheckMatchedEpc
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.setAllowCheckMatchedEpcToBeBypassed
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.setEpcNotAutomatched
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withEpcDetails
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
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

    @Test
    fun `getHasEpc returns an enum if the corresponding page is in journeyData`() {
        val hasEpc = HasEpc.NOT_REQUIRED
        val testJourneyData = journeyDataBuilder.withEpcStatus(hasEpc).build()

        val retrievedHasEpc = testJourneyData.getHasEPC()

        assertEquals(hasEpc, retrievedHasEpc)
    }

    @Test
    fun `getHasEpc returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasEICR = testJourneyData.getHasEPC()

        assertNull(retrievedHasEICR)
    }

    @Test
    fun `getEpcLookupCertificateNumber returns the certificate number if it is in journeyData`() {
        val certificateNumber = "0000-0000-1234-5678-9100"
        val testJourneyData = journeyDataBuilder.withEpcLookupCertificateNumber(certificateNumber).build()

        val retrievedEpcCertificateNumber = testJourneyData.getEpcLookupCertificateNumber()

        assertEquals(certificateNumber, retrievedEpcCertificateNumber)
    }

    @Test
    fun `getEpcLookupCertificateNumber returns null if the EPC certificate number is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedEpcCertificateNumber = testJourneyData.getEpcLookupCertificateNumber()

        assertNull(retrievedEpcCertificateNumber)
    }

    @Test
    fun `withEpcDetails returns a JourneyData with the EPC details set`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()
        val lookedUpEpcDetails =
            EpcDataModel(
                certificateNumber = "0000-0000-0000-1234-5678",
                singleLineAddress = "1, Example Road, EG",
                energyRating = "C",
                expiryDate = LocalDate.of(2027, 1, 1).toKotlinLocalDate(),
                latestCertificateNumberForThisProperty = "0000-0000-0000-1234-5678",
            )
        val expectedJourneyData = mutableMapOf(LOOKED_UP_EPC_JOURNEY_DATA_KEY to Json.encodeToString(lookedUpEpcDetails))

        // Act
        val updatedJourneyData = testJourneyData.withEpcDetails(lookedUpEpcDetails)

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
    }

    @Test
    fun `withEpcDetails returns a JourneyData with the EPC details set to null if epcDetails is null`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()
        val expectedJourneyData = mutableMapOf(LOOKED_UP_EPC_JOURNEY_DATA_KEY to null)

        // Act
        val updatedJourneyData = testJourneyData.withEpcDetails(null)

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
    }

    @Test
    fun `getEpcDetails returns the EPC details from the JourneyData`() {
        // Arrange
        val storedEpcDetails =
            EpcDataModel(
                certificateNumber = "0000-0000-0000-1234-5678",
                singleLineAddress = "1, Example Road, EG",
                energyRating = "C",
                expiryDate = LocalDate.of(2027, 1, 1).toKotlinLocalDate(),
                latestCertificateNumberForThisProperty = "0000-0000-0000-1234-5678",
            )
        val journeyData = journeyDataBuilder.build().withEpcDetails(storedEpcDetails)

        // Act
        val retrievedEpcDetails = journeyData.getEpcDetails()

        // Assert
        assertEquals(storedEpcDetails, retrievedEpcDetails)
    }

    @Test
    fun `restCheckMatchedEpc removes the check-matched-epc key from the JourneyData`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckMatchedEpcResult(false).build()
        val expectedJourneyData = mutableMapOf<String, Any?>()

        // Act
        val updatedJourneyData = testJourneyData.resetCheckMatchedEpc()

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
    }

    @Test
    fun `setEpcNotAutomatched adds empty epc-not-automatched data to JourneyData`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckMatchedEpcResult(false).build()
        val expectedJourneyData = JourneyDataBuilder(mock()).withCheckMatchedEpcResult(false).withEpcNotAutomatched().build()

        // Act
        val updatedJourneyData = testJourneyData.setEpcNotAutomatched()

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `setAllowCheckMatchedEpcToBeBypassed sets a boolean in JourneyData`(allowBypass: Boolean) {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()
        val expectedUpdatedJourneyData = mutableMapOf(ALLOW_CHECK_MATCHED_EPC_TO_BE_BYPASSED to allowBypass)

        // Act
        val updatedJourneyData = testJourneyData.setAllowCheckMatchedEpcToBeBypassed(allowBypass)

        // Assert
        assertEquals(expectedUpdatedJourneyData, updatedJourneyData)
    }

    @Test
    fun `getMatchedEpcIsCorrect returns a boolean if the corresponding page is in journeyData`() {
        val matchedEpcIsCorrect = true
        val testJourneyData = journeyDataBuilder.withCheckMatchedEpcResult(matchedEpcIsCorrect).build()

        val retrievedMatchedEpcIsCorrect = testJourneyData.getMatchedEpcIsCorrect()

        assertEquals(matchedEpcIsCorrect, retrievedMatchedEpcIsCorrect)
    }

    @Test
    fun `getMatchedEpcIsCorrect returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedMatchedEpcIsCorrect = testJourneyData.getMatchedEpcIsCorrect()

        assertNull(retrievedMatchedEpcIsCorrect)
    }

    @Test
    fun `getHasFireSafetyDeclaration returns a boolean if the corresponding page is in journeyData`() {
        val hasFireSafetyDeclaration = true
        val testJourneyData = journeyDataBuilder.withFireSafetyDeclaration(hasFireSafetyDeclaration).build()

        val retrievedHasFireSafetyDeclaration = testJourneyData.getHasFireSafetyDeclaration()

        assertEquals(hasFireSafetyDeclaration, retrievedHasFireSafetyDeclaration)
    }

    @Test
    fun `getHasFireSafetyDeclaration returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasFireSafetyDeclaration = testJourneyData.getHasFireSafetyDeclaration()

        assertNull(retrievedHasFireSafetyDeclaration)
    }
}
