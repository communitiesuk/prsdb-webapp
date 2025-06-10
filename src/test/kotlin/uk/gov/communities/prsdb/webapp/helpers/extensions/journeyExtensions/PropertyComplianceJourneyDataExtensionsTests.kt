package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.AUTO_MATCHED_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_EPC_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAutoMatchedEpcIsCorrect
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
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcTask
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
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getLatestEpcCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withResetCheckMatchedEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.EpcLookupPagePropertyCompliance.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

        @JvmStatic
        private fun provideEpcDetailsJourneyDataKey() =
            arrayOf(
                Arguments.of(Named.of("for automatched EPC", true), AUTO_MATCHED_EPC_JOURNEY_DATA_KEY),
                Arguments.of(Named.of("for looked up EPC", false), LOOKED_UP_EPC_JOURNEY_DATA_KEY),
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

    @ParameterizedTest
    @MethodSource("provideEpcDetailsJourneyDataKey")
    fun `withEpcDetails returns a JourneyData with the EPC details set`(
        autoMatched: Boolean,
        journeyDataKey: String,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()
        val lookedUpEpcDetails = MockEpcData.createEpcDataModel()
        val expectedJourneyData = mutableMapOf(journeyDataKey to Json.encodeToString(lookedUpEpcDetails))

        // Act
        val updatedJourneyData = testJourneyData.withEpcDetails(lookedUpEpcDetails, autoMatched)

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
    }

    @ParameterizedTest
    @MethodSource("provideEpcDetailsJourneyDataKey")
    fun `withEpcDetails returns a JourneyData with the EPC details set to null if epcDetails is null`(
        autoMatched: Boolean,
        journeyDataKey: String,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()
        val expectedJourneyData = mutableMapOf(journeyDataKey to null)

        // Act
        val updatedJourneyData = testJourneyData.withEpcDetails(null, autoMatched)

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
    }

    @Test
    fun `getEpcDetails returns autoMatched details from the JourneyData if autoMatched is true`() {
        // Arrange
        val storedEpcDetails = MockEpcData.createEpcDataModel()
        val journeyData = journeyDataBuilder.withAutoMatchedEpcDetails(storedEpcDetails).build()

        // Act
        val retrievedEpcDetails = journeyData.getEpcDetails(autoMatched = true)

        // Assert
        assertEquals(storedEpcDetails, retrievedEpcDetails)
    }

    @Test
    fun `getEpcDetails returns looked up details from the JourneyData if autoMatched is false`() {
        // Arrange
        val storedEpcDetails = MockEpcData.createEpcDataModel()
        val journeyData = journeyDataBuilder.withLookedUpEpcDetails(storedEpcDetails).build()

        // Act
        val retrievedEpcDetails = journeyData.getEpcDetails(autoMatched = false)

        // Assert
        assertEquals(storedEpcDetails, retrievedEpcDetails)
    }

    @Test
    fun `getAcceptedEpcDetails returns autoMatched epc details if these were accepted by the user`() {
        // Arrange
        val autoMatchedEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER)
        val lookedUpEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER)
        val journeyData =
            journeyDataBuilder
                .withAutoMatchedEpcDetails(autoMatchedEpcDetails)
                .withLookedUpEpcDetails(lookedUpEpcDetails)
                .withCheckAutoMatchedEpcResult(true)
                .withCheckMatchedEpcResult(true)
                .build()

        // Act
        val retrievedEpcDetails = journeyData.getAcceptedEpcDetails()

        // Assert
        assertEquals(autoMatchedEpcDetails, retrievedEpcDetails)
    }

    @Test
    fun `getAcceptedEpcDetails returns looked up epc details if these were accepted by the user and automatched details were rejected`() {
        // Arrange
        val autoMatchedEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER)
        val lookedUpEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER)
        val journeyData =
            journeyDataBuilder
                .withAutoMatchedEpcDetails(autoMatchedEpcDetails)
                .withLookedUpEpcDetails(lookedUpEpcDetails)
                .withCheckAutoMatchedEpcResult(false)
                .withCheckMatchedEpcResult(true)
                .build()

        // Act
        val retrievedEpcDetails = journeyData.getAcceptedEpcDetails()

        // Assert
        assertEquals(lookedUpEpcDetails, retrievedEpcDetails)
    }

    @Test
    fun `getAutoMatchedEpcIsCorrect returns the submitted answer for the CheckAutoMatchedEpc step`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckAutoMatchedEpcResult(true).build()

        // Act, Assert
        assertNotNull(testJourneyData.getAutoMatchedEpcIsCorrect())
        assertTrue(testJourneyData.getAutoMatchedEpcIsCorrect()!!)
    }

    @Test
    fun `getAutoMatchedEpcIsCorrect returns the submitted answer for the CheckMatchedEpc step`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckAutoMatchedEpcResult(true).build()

        // Act, Assert
        assertNotNull(testJourneyData.getAutoMatchedEpcIsCorrect())
        assertTrue(testJourneyData.getAutoMatchedEpcIsCorrect()!!)
    }

    @Test
    fun `withResetCheckMatchedEpc removes the check-matched-epc key from the JourneyData`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckMatchedEpcResult(false).build()
        val expectedJourneyData = mutableMapOf<String, Any?>()

        // Act
        val updatedJourneyData = testJourneyData.withResetCheckMatchedEpc()

        // Assert
        assertEquals(expectedJourneyData, updatedJourneyData)
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
    fun `getLatestEpcCertificateNumber returns the latestCertificateNumber from a looked up EPC`() {
        val testJourneyData =
            journeyDataBuilder
                .withLookedUpEpcDetails(
                    MockEpcData.createEpcDataModel(
                        certificateNumber = SUPERSEDED_EPC_CERTIFICATE_NUMBER,
                        latestCertificateNumberForThisProperty = CURRENT_EPC_CERTIFICATE_NUMBER,
                    ),
                ).build()

        val retrievedCertificateNumber = testJourneyData.getLatestEpcCertificateNumber()

        assertEquals(CURRENT_EPC_CERTIFICATE_NUMBER, retrievedCertificateNumber)
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

    @Nested
    inner class GetHasCompletedEpcTask {
        @Test
        fun `getHasCompletedEpcTask returns true if the EpcExemptionConfirmation step has been completed`() {
            val testJourneyData = journeyDataBuilder.withEpcExemptionConfirmationStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcTask returns true if the EpcMissing step has been completed`() {
            val testJourneyData = journeyDataBuilder.withEpcMissingStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcTask returns true if the EpcExpired step has been completed`() {
            val testJourneyData = journeyDataBuilder.withEpcExpiredStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcTask returns true if the EpcNotFound step has been completed`() {
            val testJourneyData = journeyDataBuilder.withEpcNotFoundStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcTask returns true if the LowEnergyRating step has been completed`() {
            val testJourneyData = journeyDataBuilder.withLowEnergyRatingStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `returns true if the MeesExemption step has been completed`() {
            val testJourneyData = journeyDataBuilder.withMeesExemptionConfirmationStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        // TODO: PRSD-1146 - add check that this page was answered "Yes"
        @Disabled
        @Test
        fun `returns true if EpcExpiryCheck was answered Yes and the energy rating is E or better`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(
                        MockEpcData.createEpcDataModel(expiryDate = kotlinx.datetime.LocalDate(2022, 1, 5), energyRating = "A"),
                    ).withCheckAutoMatchedEpcResult(true)
                    // .withEpcExpiryCheck(true)  TODO: PRSD-1146
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        // TODO: PRSD-1146 - add check that this page was answered "Yes"
        @Disabled
        @Test
        fun `EpcExpiryCheck does not complete this task if the energy rating is worse than E and MEES steps are not completed`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(
                        MockEpcData.createEpcDataModel(expiryDate = kotlinx.datetime.LocalDate(2022, 1, 5), energyRating = "F"),
                    ).withCheckAutoMatchedEpcResult(true)
                    // .withEpcExpiryCheck(true)  TODO: PRSD-1146
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        // TODO: PRSD-1146 - add check that this page was answered "No"
        @Disabled
        @Test
        fun `EpcExpiryCheck does not complete this task if it is answered No`() {
            val testJourneyData =
                journeyDataBuilder
                    // .withEpcExpiryCheck(false)  TODO: PRSD-1146
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `returns true if CheckAutoMatchedEpc is answered Yes for an in date EPC with a good energy rating`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(
                        MockEpcData.createEpcDataModel(
                            expiryDate =
                                DateTimeHelper().getCurrentDateInUK().plus(
                                    DatePeriod(years = 5),
                                ),
                            energyRating = "A",
                        ),
                    ).withCheckAutoMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `CheckAutoMatchedEpc does not complete this task if it is answered No`() {
            val testJourneyData =
                journeyDataBuilder
                    .withCheckAutoMatchedEpcResult(false)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `CheckAutoMatchedEpc does not complete this task if the accepted EPC has expired`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(
                        MockEpcData.createEpcDataModel(
                            expiryDate = kotlinx.datetime.LocalDate(2022, 1, 5),
                        ),
                    ).withCheckAutoMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `CheckAutoMatchedEpc does not complete this task if the accepted EPC has a low energy rating`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(
                        MockEpcData.createEpcDataModel(
                            energyRating = "F",
                        ),
                    ).withCheckAutoMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `returns true if CheckMatchedEpc is answered Yes for an in date EPC with a good energy rating`() {
            val testJourneyData =
                journeyDataBuilder
                    .withLookedUpEpcDetails(
                        MockEpcData.createEpcDataModel(
                            expiryDate =
                                DateTimeHelper().getCurrentDateInUK().plus(
                                    DatePeriod(years = 5),
                                ),
                            energyRating = "A",
                        ),
                    ).withCheckMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `CheckMatchedEpc does not complete this task if it is answered No`() {
            val testJourneyData =
                journeyDataBuilder
                    .withCheckMatchedEpcResult(false)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `CheckMatchedEpc does not complete this task if the accepted EPC has expired`() {
            val testJourneyData =
                journeyDataBuilder
                    .withLookedUpEpcDetails(
                        MockEpcData.createEpcDataModel(
                            expiryDate = kotlinx.datetime.LocalDate(2022, 1, 5),
                        ),
                    ).withCheckMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `CheckMatchedEpc does not complete this task if the accepted EPC has a low energy rating`() {
            val testJourneyData =
                journeyDataBuilder
                    .withLookedUpEpcDetails(
                        MockEpcData.createEpcDataModel(
                            energyRating = "F",
                        ),
                    ).withCheckMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcTask()

            assertFalse(hasCompletedEpcTask)
        }
    }
}
