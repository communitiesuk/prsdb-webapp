package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel

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

    @Test
    fun `resolvePageContent prefills the name of the freeholder being changed`() {
        val stepConfig = FreeholderNameStepConfig()
        whenever(mockJourneyState.editingFreeholder).thenReturn(FreeholderDataModel(detailsKnown = true, name = "Existing name"))

        val result = stepConfig.resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to FreeholderNameFormModel()))

        assertEquals("Existing name", (result[FORM_MODEL_ATTR_NAME] as FreeholderNameFormModel).name)
    }

    @Test
    fun `resolvePageContent does not overwrite a name the user has already entered`() {
        val stepConfig = FreeholderNameStepConfig()
        val formModel = FreeholderNameFormModel().apply { name = "Entered name" }

        val result = stepConfig.resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        assertEquals("Entered name", (result[FORM_MODEL_ATTR_NAME] as FreeholderNameFormModel).name)
    }

    @Test
    fun `resolvePageContent leaves the name empty when adding a new freeholder`() {
        val stepConfig = FreeholderNameStepConfig()
        whenever(mockJourneyState.editingFreeholder).thenReturn(null)

        val result = stepConfig.resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to FreeholderNameFormModel()))

        assertNull((result[FORM_MODEL_ATTR_NAME] as FreeholderNameFormModel).name)
    }
}
