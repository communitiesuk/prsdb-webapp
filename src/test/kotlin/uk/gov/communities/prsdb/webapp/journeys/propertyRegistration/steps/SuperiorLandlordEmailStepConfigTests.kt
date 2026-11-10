package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordNameFormModel

@ExtendWith(MockitoExtension::class)
class SuperiorLandlordEmailStepConfigTests {
    @Mock
    lateinit var mockState: SuperiorLandlordsDetailsState

    @Mock
    lateinit var mockSuperiorLandlordNameStep: SuperiorLandlordNameStep

    @Test
    fun `getStepSpecificContent includes the heading with the superior landlord name and save and continue button`() {
        // Arrange
        val stepConfig = SuperiorLandlordEmailStepConfig()
        whenever(mockState.superiorLandlordNameStep).thenReturn(mockSuperiorLandlordNameStep)
        whenever(mockSuperiorLandlordNameStep.formModelOrNull).thenReturn(
            SuperiorLandlordNameFormModel().apply { name = "James Hassan" },
        )

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.superiorLandlords.email.fieldSetHeading",
                "fieldSetHeadingParam" to "James Hassan",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `chooseTemplate returns the email form template`() {
        // Arrange
        val stepConfig = SuperiorLandlordEmailStepConfig()

        // Act
        val template = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/emailForm", template)
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation excludes the superior landlord being edited`() {
        // Arrange
        val stepConfig = SuperiorLandlordEmailStepConfig()
        whenever(mockState.superiorLandlordsMap).thenReturn(
            mapOf(
                1 to SuperiorLandlordDataModel(detailsKnown = true, email = "first@example.com"),
                2 to SuperiorLandlordDataModel(detailsKnown = true, email = "editing@example.com"),
                3 to SuperiorLandlordDataModel(detailsKnown = false),
                4 to SuperiorLandlordDataModel(detailsKnown = true, email = "fourth@example.com"),
            ),
        )
        whenever(mockState.editingSuperiorLandlordId).thenReturn(2)

        // Act
        val enrichedFormData = stepConfig.enrichSubmittedDataBeforeValidation(mockState, emptyMap())

        // Assert
        assertEquals(
            listOf("first@example.com", "fourth@example.com"),
            enrichedFormData[SuperiorLandlordEmailFormModel::existingSuperiorLandlordEmails.name],
        )
    }

    @Test
    fun `resolvePageContent prepopulates the email of the superior landlord being edited`() {
        // Arrange
        val stepConfig = SuperiorLandlordEmailStepConfig()
        whenever(mockState.editingSuperiorLandlord).thenReturn(
            SuperiorLandlordDataModel(detailsKnown = true, email = "existing@example.com"),
        )
        val defaultContent = emptyMap<String, Any?>()

        // Act
        val resolvedContent = stepConfig.resolvePageContent(mockState, defaultContent)

        // Assert
        val formModel = resolvedContent[FORM_MODEL_ATTR_NAME] as SuperiorLandlordEmailFormModel
        assertEquals("existing@example.com", formModel.emailAddress)
    }

    @Test
    fun `resolvePageContent preserves email already entered in the form`() {
        // Arrange
        val stepConfig = SuperiorLandlordEmailStepConfig()
        val formModel = SuperiorLandlordEmailFormModel().apply { emailAddress = "entered@example.com" }
        val defaultContent = mapOf(FORM_MODEL_ATTR_NAME to formModel)

        // Act
        val resolvedContent = stepConfig.resolvePageContent(mockState, defaultContent)

        // Assert
        assertSame(defaultContent, resolvedContent)
        assertEquals("entered@example.com", formModel.emailAddress)
    }
}
