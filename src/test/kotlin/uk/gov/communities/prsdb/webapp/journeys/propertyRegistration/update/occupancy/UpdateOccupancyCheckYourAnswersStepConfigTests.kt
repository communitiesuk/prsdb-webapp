package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.journeys.Destination
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UpdateOccupancyCheckYourAnswersStepConfigTests {
    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    private val stepConfig = UpdateOccupancyCheckYourAnswersStepConfig()

    @Test
    fun `resolveNextDestination returns the default destination without deleting the journey`() {
        val defaultDestination = Destination.ExternalUrl("redirect")

        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        assertEquals(defaultDestination, result)
        verify(mockState, never()).deleteJourney()
    }

    @Test
    fun `getStepSpecificContent returns skeleton check-answers content`() {
        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("propertyDetails.update.title", content["title"])
        assertEquals(true, content["showWarning"])
        assertEquals("forms.buttons.confirmAndSubmitUpdate", content["submitButtonText"])
        assertEquals(emptyList<Any>(), content["summaryListData"])
    }
}
