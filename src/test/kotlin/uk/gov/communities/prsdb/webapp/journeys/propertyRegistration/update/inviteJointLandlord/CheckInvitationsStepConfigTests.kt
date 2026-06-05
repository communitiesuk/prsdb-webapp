package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckInvitationsStepConfigTests {
    @Mock
    private lateinit var mockJourneyState: InviteJointLandlordJourneyState

    @Mock
    private lateinit var mockCheckJointLandlordsStep: CheckJointLandlordsStep

    private val journeyId = "journey-123"

    @Test
    fun `getStepSpecificContent returns summary rows with invited emails`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.invitedJointLandlords).thenReturn(listOf("first@example.com", "second@example.com"))

        val content = stepConfig.getStepSpecificContent(mockJourneyState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        assertEquals(1, rows.size)
        assertEquals(
            listOf("first@example.com", "second@example.com"),
            rows.first().fieldValue,
        )
        assertEquals("forms.links.change", rows.first().actions.single().text)
        assertEquals(
            "${CheckJointLandlordsStep.ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId",
            rows.first().actions.single().url,
        )
    }

    @Test
    fun `getStepSpecificContent returns correct summary name and submit button text`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        assertEquals("inviteJointLandlord.checkInvitations.summaryName", content["summaryName"])
        assertEquals("inviteJointLandlord.checkInvitations.submitButtonText", content["submitButtonText"])
    }

    @Test
    fun `mode returns null when form model is not saved`() {
        val stepConfig = CheckInvitationsStepConfig()
        stepConfig.routeSegment = CheckInvitationsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        whenever(mockJourneyState.getStepData(CheckInvitationsStep.ROUTE_SEGMENT)).thenReturn(null)

        assertNull(stepConfig.mode(mockJourneyState))
    }

    @Test
    fun `mode returns COMPLETE when form model is saved`() {
        val stepConfig = CheckInvitationsStepConfig()
        stepConfig.routeSegment = CheckInvitationsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        whenever(mockJourneyState.getStepData(CheckInvitationsStep.ROUTE_SEGMENT)).thenReturn(emptyMap())

        assertEquals(Complete.COMPLETE, stepConfig.mode(mockJourneyState))
    }

    private fun setupStepConfig(): CheckInvitationsStepConfig {
        val stepConfig = CheckInvitationsStepConfig()
        stepConfig.routeSegment = CheckInvitationsStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockJourneyState.checkJointLandlordsStep).thenReturn(mockCheckJointLandlordsStep)
        whenever(mockCheckJointLandlordsStep.routeSegment).thenReturn(CheckJointLandlordsStep.ROUTE_SEGMENT)
        whenever(mockCheckJointLandlordsStep.currentJourneyId).thenReturn(journeyId)
        whenever(mockCheckJointLandlordsStep.isStepReachable).thenReturn(true)

        return stepConfig
    }
}
