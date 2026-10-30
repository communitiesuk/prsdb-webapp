package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PersonNameStepConfigTests {
    private val state = mock<FreeholdersState>()

    @Test
    fun `getStepSpecificContent returns the single freeholder heading, hint and save and continue button`() {
        // Arrange
        val stepConfig = PersonNameStepConfig()
        whenever(state.isJointFreeholder).thenReturn(false)

        // Act
        val content = stepConfig.getStepSpecificContent(state)

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
        whenever(state.isJointFreeholder).thenReturn(true)

        // Act
        val content = stepConfig.getStepSpecificContent(state)

        // Assert
        assertEquals("registerProperty.freeholders.name.fieldSetHeading.joint", content["fieldSetHeading"])
    }
}
