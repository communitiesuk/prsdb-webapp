package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.journeys.JourneyState

@ExtendWith(MockitoExtension::class)
class EmailStepConfigTests {
    @Mock
    lateinit var mockState: JourneyState

    @Test
    fun `chooseTemplate returns the default template by default`() {
        val stepConfig = EmailStepConfig()

        assertEquals(EmailStep.DEFAULT_TEMPLATE, stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `withCorrespondenceTemplate switches chooseTemplate to the correspondence template`() {
        val stepConfig = EmailStepConfig()

        stepConfig.withCorrespondenceTemplate()

        assertEquals(EmailStep.CORRESPONDENCE_TEMPLATE, stepConfig.chooseTemplate(mockState))
    }
}
