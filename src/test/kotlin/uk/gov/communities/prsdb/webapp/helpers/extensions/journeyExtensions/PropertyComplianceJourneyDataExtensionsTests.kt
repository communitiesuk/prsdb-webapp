package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
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
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasCompletedEpcAdded
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
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
        private fun provideEicrIssueDates() =
            arrayOf(
                Arguments.of(Named.of("over 5 years old", LocalDate.of(2015, 1, 4)), true),
                Arguments.of(Named.of("5 years old", LocalDate.of(2015, 1, 5)), true),
                Arguments.of(Named.of("less than 5 years old", LocalDate.of(2015, 1, 6)), false),
            )

        @JvmStatic
        private fun provideMatchedEpcJourneyStepIds() =
            arrayOf(
                Arguments.of(
                    Named.of("for full EPC update", false),
                    CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT,
                    CheckMatchedEpcStep.ROUTE_SEGMENT,
                ),
                Arguments.of(
                    Named.of("for MEES-only update", true),
                    PropertyComplianceStepId.UpdateMeesCheckAutoMatchedEpc.urlPathSegment,
                    PropertyComplianceStepId.UpdateMeesCheckMatchedEpc.urlPathSegment,
                ),
            )
    }

    private lateinit var journeyDataBuilder: JourneyDataBuilder

    @BeforeEach
    fun setup() {
        journeyDataBuilder = JourneyDataBuilder(mock())
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
        checkAutoMatchedEpcStepId: String,
        checkMatchedEpcStepId: String,
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
        checkAutoMatchedEpcStepId: String,
        checkMatchedEpcStepId: String,
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

    @Test
    fun `getMatchedEpcIsCorrect returns the submitted answer for the CheckMatchedEpc step`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withCheckMatchedEpcResult(true).build()

        // Act, Assert
        assertTrue(testJourneyData.getMatchedEpcIsCorrect(CheckMatchedEpcStep.ROUTE_SEGMENT)!!)
    }

    @Test
    fun `getDidTenancyStartBeforeEpcExpiry returns true for full EPC update when tenancy started before expiry`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withEpcExpiryCheckStep(true, meesOnlyUpdate = false).build()

        // Act, Assert
        assertTrue(testJourneyData.getDidTenancyStartBeforeEpcExpiry()!!)
    }

    @Test
    fun `getDidTenancyStartBeforeEpcExpiry returns true for MEES-only update when tenancy started before expiry`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.withEpcExpiryCheckStep(true, meesOnlyUpdate = true).build()

        // Act, Assert
        assertTrue(testJourneyData.getDidTenancyStartBeforeEpcExpiry(PropertyComplianceStepId.UpdateMeesEpcExpiryCheck.urlPathSegment)!!)
    }

    @Test
    fun `getDidTenancyStartBeforeEpcExpiry returns null if EpcExpiryCheck is not in JourneyData`() {
        // Arrange
        val testJourneyData = journeyDataBuilder.build()

        // Act, Assert
        assertNull(testJourneyData.getDidTenancyStartBeforeEpcExpiry())
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
