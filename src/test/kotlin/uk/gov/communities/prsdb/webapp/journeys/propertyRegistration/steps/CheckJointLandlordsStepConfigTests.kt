package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckJointLandlordsStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JointLandlordsState

    @Mock
    lateinit var urlParameterService: CollectionKeyParameterService

    @Mock
    lateinit var inviteAnotherJointLandlordStep: InviteJointLandlordStep

    @Mock
    lateinit var removeJointLandlordStep: RemoveJointLandlordStep

    private val routeSegment = CheckJointLandlordsStep.ROUTE_SEGMENT
    private val journeyId = "journey-123"

    @Test
    fun `getStepSpecificContent returns rows sorted by internal index with indexed action urls`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setupStepDestinations()
        whenever(urlParameterService.createParameterPair(any())).thenAnswer {
            "memberId" to it.getArgument<Int>(0).toString()
        }
        whenever(mockJourneyState.invitedJointLandlordEmailsMap)
            .thenReturn(mapOf(10 to "ten@example.com", 2 to "two@example.com"))

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>
        assertEquals(2, rows.size)

        assertEquals("two@example.com", rows[0].fieldValue)
        assertEquals(1, rows[0].optionalFieldHeadingParam)
        assertEquals("forms.links.change", rows[0].actions[0].text)
        assertEquals(
            "${InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId&memberId=2",
            rows[0].actions[0].url,
        )
        assertEquals("forms.links.remove", rows[0].actions[1].text)
        assertEquals(
            "${RemoveJointLandlordStep.ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId&memberId=2",
            rows[0].actions[1].url,
        )

        assertEquals("ten@example.com", rows[1].fieldValue)
        assertEquals(2, rows[1].optionalFieldHeadingParam)
        assertEquals(
            "${InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId&memberId=10",
            rows[1].actions[0].url,
        )
        assertEquals(
            "${RemoveJointLandlordStep.ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId&memberId=10",
            rows[1].actions[1].url,
        )

        assertEquals(2, content["optionalAddAnotherTitleParam"])
        assertEquals(
            "${InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId",
            content["addAnotherUrl"],
        )
    }

    private fun setupStepConfig(): CheckJointLandlordsStepConfig {
        val stepConfig = CheckJointLandlordsStepConfig(urlParameterService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun setupStepDestinations() {
        whenever(mockJourneyState.inviteAnotherJointLandlordStep).thenReturn(inviteAnotherJointLandlordStep)
        whenever(inviteAnotherJointLandlordStep.routeSegment).thenReturn(InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT)
        whenever(inviteAnotherJointLandlordStep.currentJourneyId).thenReturn(journeyId)
        whenever(inviteAnotherJointLandlordStep.isStepReachable).thenReturn(true)

        whenever(mockJourneyState.removeJointLandlordStep).thenReturn(removeJointLandlordStep)
        whenever(removeJointLandlordStep.routeSegment).thenReturn(RemoveJointLandlordStep.ROUTE_SEGMENT)
        whenever(removeJointLandlordStep.currentJourneyId).thenReturn(journeyId)
        whenever(removeJointLandlordStep.isStepReachable).thenReturn(true)
    }
}
