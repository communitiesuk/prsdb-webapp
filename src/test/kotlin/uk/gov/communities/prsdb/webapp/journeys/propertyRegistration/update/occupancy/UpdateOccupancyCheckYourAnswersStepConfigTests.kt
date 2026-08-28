package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class UpdateOccupancyCheckYourAnswersStepConfigTests {
    // TODO(PDJB-1635): these assert the skeleton todo page. Replace them with the real occupancy check-your-answers
    //  content assertions when the page is built.
    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    private val stepConfig = UpdateOccupancyCheckYourAnswersStepConfig()

    @Test
    fun `chooseTemplate returns the skeleton todo template`() {
        assertEquals("forms/todo", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `getStepSpecificContent exposes a todo comment referencing the follow-up ticket`() {
        val content = stepConfig.getStepSpecificContent(mockState)

        val todoComment = content["todoComment"] as String
        assertTrue(todoComment.isNotBlank())
        assertContains(todoComment, "PDJB-1635")
    }
}
