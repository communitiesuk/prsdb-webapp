package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState

@ExtendWith(MockitoExtension::class)
class PersonNameStepConfigTests {
    @Mock
    lateinit var mockJourneyState: FreeholdersState

    @Test
    fun `getStepSpecificContent returns the single freeholder heading, hint and save and continue button`() {
        // Arrange
        val stepConfig = PersonNameStepConfig()
        whenever(mockJourneyState.isJointFreeholder).thenReturn(false)

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.single",
                "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `getStepSpecificContent returns the joint freeholder heading when the landlord is a joint freeholder`() {
        // Arrange
        val stepConfig = PersonNameStepConfig()
        whenever(mockJourneyState.isJointFreeholder).thenReturn(true)

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals("registerProperty.freeholders.name.fieldSetHeading.joint", content["fieldSetHeading"])
    }

    @Test
    fun `chooseTemplate returns the name form template`() {
        val stepConfig = PersonNameStepConfig()

        assertEquals("forms/nameForm", stepConfig.chooseTemplate(mockJourneyState))
    }
}
