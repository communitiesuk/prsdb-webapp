package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState

@ExtendWith(MockitoExtension::class)
class FreeholderNameStepConfigTests {
    @Mock
    lateinit var mockJourneyState: FreeholdersDetailsState

    @Test
    fun `getStepSpecificContent returns the non-joint freeholder heading, hint and save and continue button`() {
        // Arrange
        val stepConfig = FreeholderNameStepConfig()
        whenever(mockJourneyState.isJointFreeholder).thenReturn(false)

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.nonJoint",
                "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `getStepSpecificContent returns the joint freeholder heading when the landlord is a joint freeholder`() {
        // Arrange
        val stepConfig = FreeholderNameStepConfig()
        whenever(mockJourneyState.isJointFreeholder).thenReturn(true)

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals("registerProperty.freeholders.name.fieldSetHeading.joint", content["fieldSetHeading"])
    }

    @Test
    fun `chooseTemplate returns the name form template`() {
        val stepConfig = FreeholderNameStepConfig()

        assertEquals("forms/nameForm", stepConfig.chooseTemplate(mockJourneyState))
    }
}
