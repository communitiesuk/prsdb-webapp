package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyExitStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IndividualLandlordLocationTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgLandlordRegistrationTask

@ExtendWith(MockitoExtension::class)
class LandlordTypeChangeRedirectStepConfigTests {
    @Mock
    private lateinit var mockState: LandlordRegistrationState

    private val stepConfig = LandlordTypeChangeRedirectStepConfig()

    @Test
    fun `mode returns ORGANISATION_TASK when the organisation type is selected and its task is incomplete`() {
        setupLandlordType(LandlordTypeMode.ORGANISATION)
        setupOrgTaskExitOutcome(null)
        setupLandlordTypeUnchanged(true)

        val result = stepConfig.mode(mockState)

        assertEquals(LandlordTypeChangeDestination.ORGANISATION_TASK, result)
    }

    @Test
    fun `mode returns CHECK_ANSWERS when the organisation type is unchanged and its task is complete`() {
        setupLandlordType(LandlordTypeMode.ORGANISATION)
        setupOrgTaskExitOutcome(SubjourneyComplete.COMPLETE)
        setupLandlordTypeUnchanged(true)

        val result = stepConfig.mode(mockState)

        assertEquals(LandlordTypeChangeDestination.CHECK_ANSWERS, result)
    }

    @Test
    fun `mode returns ORGANISATION_TASK when the organisation type is newly selected even if its task is complete`() {
        setupLandlordType(LandlordTypeMode.ORGANISATION)
        setupOrgTaskExitOutcome(SubjourneyComplete.COMPLETE)
        setupLandlordTypeUnchanged(false)

        val result = stepConfig.mode(mockState)

        assertEquals(LandlordTypeChangeDestination.ORGANISATION_TASK, result)
    }

    @Test
    fun `mode returns INDIVIDUAL_TASK when the individual type is selected and its task is incomplete`() {
        setupLandlordType(LandlordTypeMode.INDIVIDUAL)
        setupIndividualTaskExitOutcome(null)
        setupLandlordTypeUnchanged(true)

        val result = stepConfig.mode(mockState)

        assertEquals(LandlordTypeChangeDestination.INDIVIDUAL_TASK, result)
    }

    @Test
    fun `mode returns CHECK_ANSWERS when the individual type is unchanged and its task is complete`() {
        setupLandlordType(LandlordTypeMode.INDIVIDUAL)
        setupIndividualTaskExitOutcome(SubjourneyComplete.COMPLETE)
        setupLandlordTypeUnchanged(true)

        val result = stepConfig.mode(mockState)

        assertEquals(LandlordTypeChangeDestination.CHECK_ANSWERS, result)
    }

    @Test
    fun `mode returns INDIVIDUAL_TASK when the individual type is newly selected even if its task is complete`() {
        setupLandlordType(LandlordTypeMode.INDIVIDUAL)
        setupIndividualTaskExitOutcome(SubjourneyComplete.COMPLETE)
        setupLandlordTypeUnchanged(false)

        val result = stepConfig.mode(mockState)

        assertEquals(LandlordTypeChangeDestination.INDIVIDUAL_TASK, result)
    }

    @Test
    fun `mode throws UnrecoverableJourneyStateException when no landlord type has been selected`() {
        setupLandlordType(null)
        whenever(mockState.journeyId).thenReturn("journey-id")

        assertThrows(UnrecoverableJourneyStateException::class.java) {
            stepConfig.mode(mockState)
        }
    }

    private fun setupLandlordType(outcome: LandlordTypeMode?) {
        val mockLandlordTypeStep = mock<LandlordTypeStep>()
        whenever(mockLandlordTypeStep.outcome).thenReturn(outcome)
        whenever(mockState.landlordTypeStep).thenReturn(mockLandlordTypeStep)
    }

    private fun setupLandlordTypeUnchanged(unchanged: Boolean) {
        val currentData = mapOf<String, Any?>("landlordType" to "ORGANISATION")
        val baseData = if (unchanged) currentData else mapOf<String, Any?>("landlordType" to "INDIVIDUAL")
        whenever(mockState.getStepData(LandlordTypeStep.ROUTE_SEGMENT)).thenReturn(currentData)
        val mockBaseState = mock<LandlordRegistrationState>()
        whenever(mockBaseState.getStepData(LandlordTypeStep.ROUTE_SEGMENT)).thenReturn(baseData)
        whenever(mockState.getBaseJourneyState()).thenReturn(mockBaseState)
    }

    private fun setupOrgTaskExitOutcome(outcome: SubjourneyComplete?) {
        val mockTask = mock<OrgLandlordRegistrationTask>()
        val exitStep = mockExitStep(outcome)
        whenever(mockTask.exitStep).thenReturn(exitStep)
        whenever(mockState.orgLandlordRegistrationTask).thenReturn(mockTask)
    }

    private fun setupIndividualTaskExitOutcome(outcome: SubjourneyComplete?) {
        val mockTask = mock<IndividualLandlordLocationTask>()
        val exitStep = mockExitStep(outcome)
        whenever(mockTask.exitStep).thenReturn(exitStep)
        whenever(mockState.individualLandlordLocationTask).thenReturn(mockTask)
    }

    private fun mockExitStep(outcome: SubjourneyComplete?): SubjourneyExitStep {
        val mockExitStep = mock<SubjourneyExitStep>()
        whenever(mockExitStep.outcome).thenReturn(outcome)
        return mockExitStep
    }
}
