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
    lateinit var removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep

    private val routeSegment = CheckJointLandlordsStep.ROUTE_SEGMENT
    private val journeyId = "journey-123"

    @Test
    fun `getStepSpecificContent returns single row when one landlord is invited`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setupMockEmailMap(mapOf(1 to "landlord@example.com"))

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        // Assert
        assertEquals(1, rows.size)
        assertEquals(1, content["optionalAddAnotherTitleParam"])
        assertRowIsCorrect(rows[0], 1, "landlord@example.com", 1)
    }

    @Test
    fun `getStepSpecificContent returns sorted rows when two landlords are invited`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setupMockEmailMap(mapOf(1 to "first@example.com", 2 to "second@example.com"))

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        // Assert
        assertEquals(2, rows.size)
        assertEquals(2, content["optionalAddAnotherTitleParam"])
        assertRowIsCorrect(rows[0], 1, "first@example.com", 1)
        assertRowIsCorrect(rows[1], 2, "second@example.com", 2)
    }

    @Test
    fun `getStepSpecificContent uses display index not internal id when multiple non-sequential landlords invited`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setupMockEmailMap(mapOf(100 to "hundred@example.com", 5 to "five@example.com", 25 to "twenty-five@example.com"))

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)
        val rows = content["summaryListData"] as List<SummaryListRowViewModel>

        // Assert
        assertEquals(3, rows.size)
        assertEquals(3, content["optionalAddAnotherTitleParam"])
        assertRowIsCorrect(rows[0], 5, "five@example.com", 1)
        assertRowIsCorrect(rows[1], 25, "twenty-five@example.com", 2)
        assertRowIsCorrect(rows[2], 100, "hundred@example.com", 3)
    }

    private fun setupStepConfig(): CheckJointLandlordsStepConfig {
        val stepConfig = CheckJointLandlordsStepConfig(urlParameterService)
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        setupStepDestinations()
        whenever(urlParameterService.createParameterPair(any())).thenAnswer {
            "memberId" to it.getArgument<Int>(0).toString()
        }

        return stepConfig
    }

    private fun setupMockEmailMap(emailMap: Map<Int, String>) {
        whenever(mockJourneyState.invitedJointLandlordEmailsMap).thenReturn(emailMap)
    }

    private fun setupStepDestinations() {
        whenever(mockJourneyState.inviteAnotherJointLandlordStep).thenReturn(inviteAnotherJointLandlordStep)
        whenever(inviteAnotherJointLandlordStep.routeSegment).thenReturn(InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT)
        whenever(inviteAnotherJointLandlordStep.currentJourneyId).thenReturn(journeyId)
        whenever(inviteAnotherJointLandlordStep.isStepReachable).thenReturn(true)

        whenever(mockJourneyState.removeJointLandlordAreYouSureStep).thenReturn(removeJointLandlordAreYouSureStep)
        whenever(removeJointLandlordAreYouSureStep.routeSegment).thenReturn(RemoveJointLandlordAreYouSureStep.ROUTE_SEGMENT)
        whenever(removeJointLandlordAreYouSureStep.currentJourneyId).thenReturn(journeyId)
        whenever(removeJointLandlordAreYouSureStep.isStepReachable).thenReturn(true)
    }

    private fun assertRowIsCorrect(
        row: SummaryListRowViewModel,
        memberId: Int,
        email: String,
        displayIndex: Int,
    ) {
        assertEquals(email, row.fieldValue)
        assertEquals(displayIndex, row.optionalFieldHeadingParam)
        assertEquals("forms.links.change", row.actions[0].text)
        assertEquals(
            "${InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId&memberId=$memberId",
            row.actions[0].url,
        )
        assertEquals("forms.links.remove", row.actions[1].text)
        assertEquals(
            "${RemoveJointLandlordAreYouSureStep.ROUTE_SEGMENT}?${JourneyIdProvider.PARAMETER_NAME}=$journeyId&memberId=$memberId",
            row.actions[1].url,
        )
    }
}
