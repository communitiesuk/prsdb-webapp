package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrOriginalName
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertOriginalName
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

    @Test
    fun `getGasSafetyCertIssueDate returns a LocalDate if the corresponding page is in journeyData`() {
        val gasSafetyIssueDate = LocalDate.now()
        val testJourneyData = journeyDataBuilder.withGasSafetyIssueDate(gasSafetyIssueDate).build()

        val retrievedGasSafetyIssueDate = testJourneyData.getGasSafetyCertIssueDate()?.toJavaLocalDate()

        assertEquals(gasSafetyIssueDate, retrievedGasSafetyIssueDate)
    }

    @Test
    fun `getGasSafetyCertIssueDate returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedGasSafetyIssueDate = testJourneyData.getGasSafetyCertIssueDate()

        assertNull(retrievedGasSafetyIssueDate)
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
    fun `getGasSafetyCertEngineerNum returns a string if the corresponding page is in journeyData`() {
        val gasSafeEngineerNum = "1234567"
        val testJourneyData = journeyDataBuilder.withGasSafeEngineerNum(gasSafeEngineerNum).build()

        val retrievedGasSafeEngineerNum = testJourneyData.getGasSafetyCertEngineerNum()

        assertEquals(gasSafeEngineerNum, retrievedGasSafeEngineerNum)
    }

    @Test
    fun `getGasSafetyCertEngineerNum returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedGasSafeEngineerNum = testJourneyData.getGasSafetyCertEngineerNum()

        assertNull(retrievedGasSafeEngineerNum)
    }

    @Test
    fun `getGasSafetyCertOriginalName returns a string if the corresponding page is in journeyData`() {
        val gasSafetyCertOriginalName = "file.png"
        val testJourneyData = journeyDataBuilder.withOriginalGasSafetyCertName(gasSafetyCertOriginalName).build()

        val retrievedGasSafetyCertOriginalName = testJourneyData.getGasSafetyCertOriginalName()

        assertEquals(gasSafetyCertOriginalName, retrievedGasSafetyCertOriginalName)
    }

    @Test
    fun `getGasSafetyCertOriginalName returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedGasSafeEngineerNum = testJourneyData.getGasSafetyCertOriginalName()

        assertNull(retrievedGasSafeEngineerNum)
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

    @Test
    fun `getGasSafetyCertExemptionReason returns the exemption reason if the corresponding page is in journeyData`() {
        val reason = GasSafetyExemptionReason.NO_GAS_SUPPLY
        val testJourneyData = journeyDataBuilder.withGasSafetyCertExemptionReason(reason).build()

        val retrievedReason = testJourneyData.getGasSafetyCertExemptionReason()

        assertEquals(reason, retrievedReason)
    }

    @Test
    fun `getGasSafetyCertExemptionReason returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedReason = testJourneyData.getGasSafetyCertExemptionReason()

        assertNull(retrievedReason)
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
    fun `getGasSafetyCertExemptionOtherReason returns a string if the corresponding page is in journeyData`() {
        val otherReason = "Some other gas safety exemption reason"
        val testJourneyData = journeyDataBuilder.withGasSafetyCertExemptionOtherReason(otherReason).build()

        val retrievedOtherReason = testJourneyData.getGasSafetyCertExemptionOtherReason()

        assertEquals(otherReason, retrievedOtherReason)
    }

    @Test
    fun `getGasSafetyCertExemptionOtherReason returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedOtherReason = testJourneyData.getGasSafetyCertExemptionOtherReason()

        assertNull(retrievedOtherReason)
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

    @Test
    fun `getEicrIssueDate returns a LocalDate if the corresponding page is in journeyData`() {
        val eicrIssueDate = LocalDate.now()
        val testJourneyData = journeyDataBuilder.withEicrIssueDate(eicrIssueDate).build()

        val retrievedEicrIssueDate = testJourneyData.getEicrIssueDate()?.toJavaLocalDate()

        assertEquals(eicrIssueDate, retrievedEicrIssueDate)
    }

    @Test
    fun `getEicrIssueDate returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedEicrIssueDate = testJourneyData.getEicrIssueDate()

        assertNull(retrievedEicrIssueDate)
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
    fun `getEicrOriginalName returns a string if the corresponding page is in journeyData`() {
        val eicrOriginalName = "eicr.pdf"
        val testJourneyData = journeyDataBuilder.withOriginalEicrName(eicrOriginalName).build()

        val retrievedEicrOriginalName = testJourneyData.getEicrOriginalName()

        assertEquals(eicrOriginalName, retrievedEicrOriginalName)
    }

    @Test
    fun `getEicrOriginalName returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedEicrOriginalName = testJourneyData.getEicrOriginalName()

        assertNull(retrievedEicrOriginalName)
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

    @Test
    fun `getEicrExemptionReason returns the exemption reason if the corresponding page is in journeyData`() {
        val reason = EicrExemptionReason.LIVE_IN_LANDLORD
        val testJourneyData = journeyDataBuilder.withEicrExemptionReason(reason).build()

        val retrievedReason = testJourneyData.getEicrExemptionReason()

        assertEquals(reason, retrievedReason)
    }

    @Test
    fun `getEicrExemptionReason returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedReason = testJourneyData.getEicrExemptionReason()

        assertNull(retrievedReason)
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
    fun `getEicrExemptionOtherReason returns a string if the corresponding page is in journeyData`() {
        val otherReason = "Some other EICR exemption reason"
        val testJourneyData = journeyDataBuilder.withEicrExemptionOtherReason(otherReason).build()

        val retrievedOtherReason = testJourneyData.getEicrExemptionOtherReason()

        assertEquals(otherReason, retrievedOtherReason)
    }

    @Test
    fun `getEicrExemptionOtherReason returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedOtherReason = testJourneyData.getEicrExemptionOtherReason()

        assertNull(retrievedOtherReason)
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
    fun `getEpcExemptionReason returns the exemption reason if the corresponding page is in journeyData`() {
        val reason = EpcExemptionReason.LISTED_BUILDING
        val testJourneyData = journeyDataBuilder.withEpcExemptionReason(reason).build()

        val retrievedReason = testJourneyData.getEpcExemptionReason()

        assertEquals(reason, retrievedReason)
    }

    @Test
    fun `getEpcExemptionReason returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedReason = testJourneyData.getEpcExemptionReason()

        assertNull(retrievedReason)
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
