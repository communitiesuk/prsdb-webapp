package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAutoMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrUploadId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertUploadId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcAdded
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewGasSafetyCertificate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getLatestEpcCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getPropertyHasMeesExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withResetCheckMatchedEpc
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.CURRENT_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage.Companion.SUPERSEDED_EPC_CERTIFICATE_NUMBER
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyComplianceJourneyDataExtensionsTests {
    companion object {
        private val arbitraryCurrentDate = LocalDate.of(2020, 1, 5).toKotlinLocalDate()
        private val pastDate = DateTimeHelper().getCurrentDateInUK().minus(DatePeriod(years = 1))
        private val futureDate = DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 1))

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
                Arguments.of(Named.of("not other", EicrExemptionReason.LONG_LEASE), false),
            )

        @JvmStatic
        private fun provideEpcDetailsJourneyDataKey() =
            arrayOf(
                Arguments.of(Named.of("for automatched EPC", true), NonStepJourneyDataKey.AutoMatchedEpc.key),
                Arguments.of(Named.of("for looked up EPC", false), NonStepJourneyDataKey.LookedUpEpc.key),
            )

        @JvmStatic
        private fun provideMatchedEpcJourneyStepIds() =
            arrayOf(
                Arguments.of(
                    Named.of("for full EPC update", false),
                    PropertyComplianceStepId.CheckAutoMatchedEpc,
                    PropertyComplianceStepId.CheckMatchedEpc,
                ),
                Arguments.of(
                    Named.of("for MEES-only update", true),
                    PropertyComplianceStepId.UpdateMeesCheckAutoMatchedEpc,
                    PropertyComplianceStepId.UpdateMeesCheckMatchedEpc,
                ),
            )

        @JvmStatic
        private fun provideEpcExemptionReasonStepId() =
            arrayOf(
                Arguments.of(
                    Named.of("for full EPC update", false),
                    PropertyComplianceStepId.EpcExemptionReason,
                ),
                Arguments.of(
                    Named.of("for MEES-only update", true),
                    PropertyComplianceStepId.UpdateMeesEpcExemptionReason,
                ),
            )

        @JvmStatic
        private fun provideEpcExpiryCheckStepId() =
            arrayOf(
                Arguments.of(
                    Named.of("for full EPC update", false),
                    PropertyComplianceStepId.EpcExpiryCheck,
                ),
                Arguments.of(
                    Named.of("for MEES-only update", true),
                    PropertyComplianceStepId.UpdateMeesEpcExpiryCheck,
                ),
            )

        @JvmStatic
        private fun provideMeesExemptionCheckStepId() =
            arrayOf(
                Arguments.of(
                    Named.of("for full EPC update", false),
                    PropertyComplianceStepId.MeesExemptionCheck,
                ),
                Arguments.of(
                    Named.of("for MEES-only update", true),
                    PropertyComplianceStepId.UpdateMeesMeesExemptionCheck,
                ),
            )

        @JvmStatic
        private fun provideMeesExemptionReasonStepId() =
            arrayOf(
                Arguments.of(
                    Named.of("for full EPC update", false),
                    PropertyComplianceStepId.MeesExemptionReason,
                ),
                Arguments.of(
                    Named.of("for MEES-only update", true),
                    PropertyComplianceStepId.UpdateMeesMeesExemptionReason,
                ),
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
    fun `getHasNewGasSafetyCertificate returns a boolean if the corresponding page is in journeyData`() {
        val hasNewGasSafetyCert = true
        val testJourneyData = journeyDataBuilder.withNewGasSafetyCertStatus(hasNewGasSafetyCert).build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasNewGasSafetyCertificate()

        assertEquals(hasNewGasSafetyCert, retrievedHasGasSafetyCert)
    }

    @Test
    fun `getHasGasSafetyCert returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasGasSafetyCert()

        assertNull(retrievedHasGasSafetyCert)
    }

    @Test
    fun `getHasNewGasSafetyCertificate returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasGasSafetyCert = testJourneyData.getHasNewGasSafetyCertificate()

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
        mockConstruction(DateTimeHelper::class.java) { mock, _ -> whenever(mock.getCurrentDateInUK()).thenReturn(arbitraryCurrentDate) }
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
    fun `getGasSafetyCertUploadId returns a string if the corresponding page is in journeyData`() {
        val gasSafetyFileUploadId = 33L
        val testJourneyData = journeyDataBuilder.withGasCertFileUploadId(gasSafetyFileUploadId).build()

        val retrievedGasSafetyCertUploadId = testJourneyData.getGasSafetyCertUploadId()?.toLong()

        assertEquals(gasSafetyFileUploadId, retrievedGasSafetyCertUploadId)
    }

    @Test
    fun `getGasSafetyCertUploadId returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedGasSafeEngineerNum = testJourneyData.getGasSafetyCertUploadId()

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
    fun `getHasNewEICR returns a boolean if the corresponding page is in journeyData`() {
        val hasNewEICR = true
        val testJourneyData = journeyDataBuilder.withNewEicrStatus(hasNewEICR).build()

        val retrievedHasEICR = testJourneyData.getHasNewEICR()

        assertEquals(hasNewEICR, retrievedHasEICR)
    }

    @Test
    fun `getEicrIssueDate returns a LocalDate if the corresponding page is in journeyData`() {
        val eicrIssueDate = LocalDate.now()
        val testJourneyData = journeyDataBuilder.withEicrIssueDate(eicrIssueDate).build()

        val retrievedEicrIssueDate = testJourneyData.getEicrIssueDate()?.toJavaLocalDate()

        assertEquals(eicrIssueDate, retrievedEicrIssueDate)
    }

    @Test
    fun `getHasNewEICR returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasEICR = testJourneyData.getHasNewEICR()

        assertNull(retrievedHasEICR)
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
        mockConstruction(DateTimeHelper::class.java) { mock, _ -> whenever(mock.getCurrentDateInUK()).thenReturn(arbitraryCurrentDate) }
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
    fun `getEicrUploadId returns a string if the corresponding page is in journeyData`() {
        val eicrUploadId = 933L
        val testJourneyData = journeyDataBuilder.withEicrUploadId(eicrUploadId).build()

        val retrievedEicrUploadId = testJourneyData.getEicrUploadId()?.toLong()

        assertEquals(eicrUploadId, retrievedEicrUploadId)
    }

    @Test
    fun `getEicrUploadId returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedEicrUploadId = testJourneyData.getEicrUploadId()

        assertNull(retrievedEicrUploadId)
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
        val reason = EicrExemptionReason.LONG_LEASE
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
    fun `getHasNewEpc returns a boolean if the corresponding page is in journeyData`() {
        val hasNewEpc = true
        val testJourneyData = journeyDataBuilder.withNewEpcStatus(hasNewEpc).build()

        val retrievedHasNewEpc = testJourneyData.getHasNewEPC()

        assertEquals(hasNewEpc, retrievedHasNewEpc)
    }

    @Test
    fun `getHasNewEpc returns null if the corresponding page is not in journeyData`() {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedHasNewEpc = testJourneyData.getHasNewEPC()

        assertNull(retrievedHasNewEpc)
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
    fun `getEpcLookupCertificateNumber returns the certificate number if UpdateMeesEpcLookup is in journeyData`() {
        val certificateNumber = "0000-0000-1234-5678-9100"
        val testJourneyData = journeyDataBuilder.withEpcLookupCertificateNumber(certificateNumber, meesOnlyUpdate = true).build()

        val retrievedEpcCertificateNumber = testJourneyData.getEpcLookupCertificateNumber(PropertyComplianceStepId.UpdateMeesEpcLookup)

        assertEquals(certificateNumber, retrievedEpcCertificateNumber)
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

    @ParameterizedTest
    @MethodSource("provideMatchedEpcJourneyStepIds")
    fun `getAcceptedEpcDetails returns autoMatched epc details if these were accepted by the user`(
        meesOnlyUpdate: Boolean,
        checkAutoMatchedEpcStepId: PropertyComplianceStepId,
        checkMatchedEpcStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val autoMatchedEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER)
        val lookedUpEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER)
        val journeyData =
            journeyDataBuilder
                .withAutoMatchedEpcDetails(autoMatchedEpcDetails)
                .withLookedUpEpcDetails(lookedUpEpcDetails)
                .withCheckAutoMatchedEpcResult(true, meesOnlyUpdate)
                .withCheckMatchedEpcResult(true, meesOnlyUpdate)
                .build()

        // Act
        val retrievedEpcDetails = journeyData.getAcceptedEpcDetails(checkAutoMatchedEpcStepId, checkMatchedEpcStepId)

        // Assert
        assertEquals(autoMatchedEpcDetails, retrievedEpcDetails)
    }

    @ParameterizedTest
    @MethodSource("provideMatchedEpcJourneyStepIds")
    fun `getAcceptedEpcDetails returns looked up epc details if these were accepted by the user and automatched details were rejected`(
        meesOnlyUpdate: Boolean,
        checkAutoMatchedEpcStepId: PropertyComplianceStepId,
        checkMatchedEpcStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val autoMatchedEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER)
        val lookedUpEpcDetails = MockEpcData.createEpcDataModel(MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER)
        val journeyData =
            journeyDataBuilder
                .withAutoMatchedEpcDetails(autoMatchedEpcDetails)
                .withLookedUpEpcDetails(lookedUpEpcDetails)
                .withCheckAutoMatchedEpcResult(false, meesOnlyUpdate)
                .withCheckMatchedEpcResult(true, meesOnlyUpdate)
                .build()

        // Act
        val retrievedEpcDetails = journeyData.getAcceptedEpcDetails(checkAutoMatchedEpcStepId, checkMatchedEpcStepId)

        // Assert
        assertEquals(lookedUpEpcDetails, retrievedEpcDetails)
    }

    @ParameterizedTest
    @MethodSource("provideMatchedEpcJourneyStepIds")
    fun `getAutoMatchedEpcIsCorrect returns the submitted answer for the CheckAutoMatchedEpc step`(
        meesOnlyUpdate: Boolean,
        checkAutoMatchedEpcStepId: PropertyComplianceStepId,
        checkMatchedEpcStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckAutoMatchedEpcResult(true, meesOnlyUpdate).build()

        // Act, Assert
        assertTrue(testJourneyData.getAutoMatchedEpcIsCorrect(checkAutoMatchedEpcStepId)!!)
    }

    @ParameterizedTest
    @MethodSource("provideMatchedEpcJourneyStepIds")
    fun `getMatchedEpcIsCorrect returns the submitted answer for the CheckMatchedEpc step`(
        meesOnlyUpdate: Boolean,
        checkAutoMatchedEpcStepId: PropertyComplianceStepId,
        checkMatchedEpcStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckMatchedEpcResult(true, meesOnlyUpdate).build()

        // Act, Assert
        assertTrue(testJourneyData.getMatchedEpcIsCorrect(checkMatchedEpcStepId)!!)
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

    @ParameterizedTest
    @MethodSource("provideEpcExemptionReasonStepId")
    fun `getEpcExemptionReason returns the exemption reason if the corresponding page is in journeyData`(
        meesOnlyUpdate: Boolean,
        epcExemptionReasonStepId: PropertyComplianceStepId,
    ) {
        val reason = EpcExemptionReason.LISTED_BUILDING
        val testJourneyData = journeyDataBuilder.withEpcExemptionReason(reason, meesOnlyUpdate).build()

        val retrievedReason = testJourneyData.getEpcExemptionReason(epcExemptionReasonStepId)

        assertEquals(reason, retrievedReason)
    }

    @ParameterizedTest
    @MethodSource("provideEpcExemptionReasonStepId")
    fun `getEpcExemptionReason returns null if the corresponding page is not in journeyData`(
        meesOnlyUpdate: Boolean,
        epcExemptionReasonStepId: PropertyComplianceStepId,
    ) {
        val testJourneyData = journeyDataBuilder.build()

        val retrievedReason = testJourneyData.getEpcExemptionReason(epcExemptionReasonStepId)

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

    @ParameterizedTest
    @MethodSource("provideEpcExpiryCheckStepId")
    fun `getDidTenancyStartBeforeEpcExpiry returns the submitted answer for the EpcExpiryCheck step`(
        meesOnlyUpdate: Boolean,
        epcExpiryCheckStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.withEpcExpiryCheckStep(true, meesOnlyUpdate).build()

        // Act, Assert
        assertTrue(testJourneyData.getDidTenancyStartBeforeEpcExpiry(epcExpiryCheckStepId)!!)
    }

    @ParameterizedTest
    @MethodSource("provideEpcExpiryCheckStepId")
    fun `getDidTenancyStartBeforeEpcExpiry returns null if EpcExpiryCheck is not in JourneyData`(
        meesOnlyUpdate: Boolean,
        epcExpiryCheckStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()

        // Act, Assert
        assertNull(testJourneyData.getDidTenancyStartBeforeEpcExpiry(epcExpiryCheckStepId))
    }

    @ParameterizedTest
    @MethodSource("provideMeesExemptionCheckStepId")
    fun `getPropertyHasMeesExemption returns the submitted answer for the MeesExemptionCheck step`(
        meesOnlyUpdate: Boolean,
        meesExemptionCheckStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.withMeesExemptionCheckStep(true, meesOnlyUpdate).build()

        // Act, Assert
        assertTrue(testJourneyData.getPropertyHasMeesExemption(meesExemptionCheckStepId)!!)
    }

    @ParameterizedTest
    @MethodSource("provideMeesExemptionCheckStepId")
    fun `getPropertyHasMeesExemption returns null if MeesExemptionCheck is not in JourneyData`(
        meesOnlyUpdate: Boolean,
        meesExemptionCheckStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()

        // Act, Assert
        assertNull(testJourneyData.getPropertyHasMeesExemption(meesExemptionCheckStepId))
    }

    @ParameterizedTest
    @MethodSource("provideMeesExemptionReasonStepId")
    fun `getMeesExemptionReason returns the submitted answer for the MeesExemptionReason step`(
        meesOnlyUpdate: Boolean,
        meesExemptionReasonStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData =
            journeyDataBuilder
                .withMeesExemptionReasonStep(MeesExemptionReason.HIGH_COST, meesOnlyUpdate)
                .build()

        // Act
        val retrievedExemptionReason = testJourneyData.getMeesExemptionReason(meesExemptionReasonStepId)!!

        // Act, Assert
        assertEquals(MeesExemptionReason.HIGH_COST, retrievedExemptionReason)
    }

    @ParameterizedTest
    @MethodSource("provideMeesExemptionReasonStepId")
    fun `getMeesExemptionReason returns null is MeesExemptionReason is not in JourneyData`(
        meesOnlyUpdate: Boolean,
        meesExemptionReasonStepId: PropertyComplianceStepId,
    ) {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()

        // Act, Assert
        assertNull(testJourneyData.getMeesExemptionReason(meesExemptionReasonStepId))
    }

    @Nested
    inner class GetHasCompletedEpcAdded {
        @Test
        fun `getHasCompletedEpcAdded returns true if the LowEnergyRating step has been completed`() {
            val testJourneyData = journeyDataBuilder.withLowEnergyRatingStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcAdded returns true if the MeesExemptionConfirmation step has been completed`() {
            val testJourneyData = journeyDataBuilder.withMeesExemptionConfirmationStep().build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcAdded returns true if EpcExpiryCheck was answered Yes and the energy rating is E or better`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(expiryDate = pastDate, energyRating = "A"))
                    .withCheckAutoMatchedEpcResult(true)
                    .withEpcExpiryCheckStep(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertTrue(hasCompletedEpcTask)
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `getHasCompletedEpcAdded returns false if EpcExpiryCheck was answered Yes but the energy rating is low and MEES steps are not completed`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(expiryDate = pastDate, energyRating = "F"))
                    .withCheckAutoMatchedEpcResult(true)
                    .withEpcExpiryCheckStep(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcAdded returns false if EpcExpiryCheck was answered No`() {
            val testJourneyData =
                journeyDataBuilder
                    .withEpcExpiryCheckStep(false)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcAdded returns true for an in date EPC with a good energy rating`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(expiryDate = futureDate, energyRating = "A"))
                    .withCheckAutoMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertTrue(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcAdded returns false if no EPC has been accepted`() {
            val testJourneyData =
                journeyDataBuilder
                    .withCheckAutoMatchedEpcResult(false)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertFalse(hasCompletedEpcTask)
        }

        @Test
        fun `getHasCompletedEpcAdded returns false if the accepted in-date EPC has a low energy rating`() {
            val testJourneyData =
                journeyDataBuilder
                    .withAutoMatchedEpcDetails(MockEpcData.createEpcDataModel(energyRating = "F"))
                    .withCheckAutoMatchedEpcResult(true)
                    .build()

            val hasCompletedEpcTask = testJourneyData.getHasCompletedEpcAdded()

            assertFalse(hasCompletedEpcTask)
        }
    }
}
