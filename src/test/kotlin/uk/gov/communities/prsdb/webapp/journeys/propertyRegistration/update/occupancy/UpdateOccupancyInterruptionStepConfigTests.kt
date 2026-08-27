package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class UpdateOccupancyInterruptionStepConfigTests {
    // TODO(PDJB-1417): these assert the skeleton todo page. Replace them with the real interruption / "are you sure"
    //  content assertions when the page is built.
    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    private val stepConfig = UpdateOccupancyInterruptionStepConfig()

    @Test
    fun `chooseTemplate returns the skeleton todo template`() {
        assertEquals("forms/todo", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `getStepSpecificContent exposes a non-blank todo comment`() {
        val content = stepConfig.getStepSpecificContent(mockState)

        val todoComment = content["todoComment"] as String
        assertTrue(todoComment.isNotBlank())
    }
}
