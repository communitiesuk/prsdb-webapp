package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class InviteJointLandlordStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JointLandlordsState

    @Mock
    lateinit var urlParameterService: CollectionKeyParameterService

    @Mock
    lateinit var inviteJointLandlordStep: InviteJointLandlordStep

    @Mock
    lateinit var inviteAnotherJointLandlordStep: InviteJointLandlordStep

    private val routeSegment = InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT

    @Test
    fun `afterStepDataIsAdded uses nextJointLandlordMemberId when adding a new email`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("emailAddress" to "new@example.com"))
        whenever(mockJourneyState.invitedJointLandlordEmailsMap).thenReturn(mapOf(1 to "one@example.com", 4 to "four@example.com"))
        whenever(mockJourneyState.nextJointLandlordMemberId).thenReturn(7)
        whenever(urlParameterService.getParameterOrNull()).thenReturn(null)

        stepConfig.afterStepDataIsAdded(mockJourneyState)

        val updatedMapCaptor = argumentCaptor<Map<Int, String>>()
        verify(mockJourneyState).invitedJointLandlordEmailsMap = updatedMapCaptor.capture()
        assertEquals("new@example.com", updatedMapCaptor.firstValue[7])
        verify(mockJourneyState).nextJointLandlordMemberId = 8
        verify(inviteJointLandlordStep).clearFormData()
        verify(inviteAnotherJointLandlordStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded initializes next id from current max key when counter is null`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("emailAddress" to "new@example.com"))
        whenever(mockJourneyState.invitedJointLandlordEmailsMap).thenReturn(mapOf(2 to "two@example.com", 4 to "four@example.com"))
        whenever(mockJourneyState.nextJointLandlordMemberId).thenReturn(null)
        whenever(urlParameterService.getParameterOrNull()).thenReturn(null)

        stepConfig.afterStepDataIsAdded(mockJourneyState)

        val updatedMapCaptor = argumentCaptor<Map<Int, String>>()
        verify(mockJourneyState).invitedJointLandlordEmailsMap = updatedMapCaptor.capture()
        assertEquals("new@example.com", updatedMapCaptor.firstValue[5])
        verify(mockJourneyState).nextJointLandlordMemberId = 6
    }

    @Test
    fun `afterStepDataIsAdded updates existing email without changing next id`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("emailAddress" to "updated@example.com"))
        whenever(mockJourneyState.invitedJointLandlordEmailsMap).thenReturn(mapOf(2 to "two@example.com", 4 to "old@example.com"))
        whenever(urlParameterService.getParameterOrNull()).thenReturn(4)

        stepConfig.afterStepDataIsAdded(mockJourneyState)

        val updatedMapCaptor = argumentCaptor<Map<Int, String>>()
        verify(mockJourneyState).invitedJointLandlordEmailsMap = updatedMapCaptor.capture()
        assertEquals("updated@example.com", updatedMapCaptor.firstValue[4])
        verify(mockJourneyState, never()).nextJointLandlordMemberId = 10
    }

    private fun setupStepConfig(): InviteJointLandlordStepConfig {
        val stepConfig = InviteJointLandlordStepConfig(urlParameterService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockJourneyState.inviteJointLandlordStep).thenReturn(inviteJointLandlordStep)
        whenever(mockJourneyState.inviteAnotherJointLandlordStep).thenReturn(inviteAnotherJointLandlordStep)

        return stepConfig
    }
}
