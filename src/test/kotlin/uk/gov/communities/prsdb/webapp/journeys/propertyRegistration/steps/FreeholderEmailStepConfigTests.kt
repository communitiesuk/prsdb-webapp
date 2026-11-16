package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel

@ExtendWith(MockitoExtension::class)
class FreeholderEmailStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersDetailsState

    @Mock
    lateinit var mockFreeholderNameStep: FreeholderNameStep

    @Test
    fun `getStepSpecificContent includes the named heading and save and continue button`() {
        // Arrange
        val stepConfig = FreeholderEmailStepConfig()
        whenever(mockState.freeholderNameStep).thenReturn(mockFreeholderNameStep)
        whenever(mockFreeholderNameStep.formModelOrNull).thenReturn(
            FreeholderNameFormModel().apply { name = "James Hassan" },
        )

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.freeholders.email.fieldSetHeading",
                "fieldSetHeadingParam" to "James Hassan",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `chooseTemplate returns the email form template`() {
        // Arrange
        val stepConfig = FreeholderEmailStepConfig()

        // Act
        val template = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/emailForm", template)
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation excludes the freeholder being edited`() {
        // Arrange
        val stepConfig = FreeholderEmailStepConfig()
        whenever(mockState.freeholdersMap).thenReturn(
            mapOf(
                1 to FreeholderDataModel(detailsKnown = true, email = "first@example.com"),
                2 to FreeholderDataModel(detailsKnown = true, email = "editing@example.com"),
                3 to FreeholderDataModel(detailsKnown = true, email = "third@example.com"),
            ),
        )
        whenever(mockState.editingFreeholderId).thenReturn(2)

        // Act
        val enrichedFormData = stepConfig.enrichSubmittedDataBeforeValidation(mockState, emptyMap())

        // Assert
        assertEquals(
            listOf("first@example.com", "third@example.com"),
            enrichedFormData[FreeholderEmailFormModel::existingFreeholderEmails.name],
        )
    }

    @Test
    fun `resolvePageContent prepopulates the current freeholder email`() {
        // Arrange
        val stepConfig = FreeholderEmailStepConfig()
        whenever(mockState.editingFreeholder).thenReturn(
            FreeholderDataModel(detailsKnown = true, email = "existing@example.com"),
        )
        val defaultContent = emptyMap<String, Any?>()

        // Act
        val resolvedContent = stepConfig.resolvePageContent(mockState, defaultContent)

        // Assert
        val formModel = resolvedContent[FORM_MODEL_ATTR_NAME] as FreeholderEmailFormModel
        assertEquals("existing@example.com", formModel.emailAddress)
    }

    @Test
    fun `resolvePageContent preserves email already entered in the form`() {
        // Arrange
        val stepConfig = FreeholderEmailStepConfig()
        val formModel = FreeholderEmailFormModel().apply { emailAddress = "entered@example.com" }
        val defaultContent = mapOf(FORM_MODEL_ATTR_NAME to formModel)

        // Act
        val resolvedContent = stepConfig.resolvePageContent(mockState, defaultContent)

        // Assert
        assertSame(defaultContent, resolvedContent)
        assertEquals("entered@example.com", formModel.emailAddress)
    }
}
