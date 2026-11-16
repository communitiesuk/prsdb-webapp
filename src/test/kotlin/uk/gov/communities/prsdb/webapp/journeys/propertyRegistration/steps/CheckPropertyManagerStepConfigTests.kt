package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.PropertyManagerSummaryCardHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckPropertyManagerStepConfigTests {
    @Mock
    lateinit var mockState: PropertyManagerState

    @Mock
    lateinit var mockPropertyManagerDetailsStep: PropertyManagerDetailsStep

    @Mock
    lateinit var mockRemovePropertyManagerStep: RemovePropertyManagerStep

    @Test
    fun `card is titled with the property manager's name and has an email address row`() {
        val stepConfig = setupStepConfig()

        val card = getCard(stepConfig)

        assertEquals(NAME, card.title)
        assertEquals("registerProperty.propertyManager.check.emailAddress", card.summaryList.single().fieldHeading)
        assertEquals(EMAIL, card.summaryList.single().fieldValue)
    }

    @Test
    fun `card has a Change action to the details step and a Remove action to the remove step`() {
        val stepConfig = setupStepConfig()

        val actions = getCard(stepConfig).actions!!

        assertEquals(listOf("forms.links.change", "forms.links.remove"), actions.map { it.text })
        assertEquals(
            listOf(
                JourneyStateService.urlWithJourneyState(PropertyManagerDetailsStep.ROUTE_SEGMENT, JOURNEY_ID),
                JourneyStateService.urlWithJourneyState(RemovePropertyManagerStep.ROUTE_SEGMENT, JOURNEY_ID),
            ),
            actions.map { it.url },
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun getCard(stepConfig: CheckPropertyManagerStepConfig) =
        (stepConfig.getStepSpecificContent(mockState)["summaryCards"] as List<SummaryCardViewModel>).single()

    private fun setupStepConfig(): CheckPropertyManagerStepConfig {
        val stepConfig = CheckPropertyManagerStepConfig(PropertyManagerSummaryCardHelper())
        stepConfig.urlPath = CheckPropertyManagerStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()

        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerDetailsStep.formModel).thenReturn(
            PropertyManagerDetailsFormModel().apply {
                name = NAME
                emailAddress = EMAIL
            },
        )
        whenever(mockPropertyManagerDetailsStep.urlPath).thenReturn(PropertyManagerDetailsStep.ROUTE_SEGMENT)
        whenever(mockPropertyManagerDetailsStep.currentJourneyId).thenReturn(JOURNEY_ID)
        whenever(mockPropertyManagerDetailsStep.isStepReachable).thenReturn(true)
        whenever(mockState.removePropertyManagerStep).thenReturn(mockRemovePropertyManagerStep)
        whenever(mockRemovePropertyManagerStep.urlPath).thenReturn(RemovePropertyManagerStep.ROUTE_SEGMENT)
        whenever(mockRemovePropertyManagerStep.currentJourneyId).thenReturn(JOURNEY_ID)
        whenever(mockRemovePropertyManagerStep.isStepReachable).thenReturn(true)
        return stepConfig
    }

    companion object {
        private const val NAME = "Best Property Managers Ltd"
        private const val EMAIL = "manager@example.com"
        private const val JOURNEY_ID = "journey-123"
    }
}
