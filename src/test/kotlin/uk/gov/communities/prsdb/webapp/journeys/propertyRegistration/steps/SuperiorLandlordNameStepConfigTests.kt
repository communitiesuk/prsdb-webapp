package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordNameFormModel

@ExtendWith(MockitoExtension::class)
class SuperiorLandlordNameStepConfigTests {
    @Mock
    lateinit var mockJourneyState: SuperiorLandlordsDetailsState

    @Test
    fun `getStepSpecificContent returns the heading, hint and save and continue button`() {
        // Arrange
        val stepConfig = SuperiorLandlordNameStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.superiorLandlords.name.fieldSetHeading",
                "fieldSetHint" to "registerProperty.superiorLandlords.name.fieldSetHint",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `chooseTemplate returns the name form template`() {
        val stepConfig = SuperiorLandlordNameStepConfig()

        assertEquals("forms/nameForm", stepConfig.chooseTemplate(mockJourneyState))
    }

    @Test
    fun `resolvePageContent prefills the name of the superior landlord being changed`() {
        val stepConfig = SuperiorLandlordNameStepConfig()
        whenever(mockJourneyState.editingSuperiorLandlord).thenReturn(
            SuperiorLandlordDataModel(detailsKnown = true, name = "Existing name"),
        )

        val result = stepConfig.resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordNameFormModel()))

        assertEquals("Existing name", (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordNameFormModel).name)
    }

    @Test
    fun `resolvePageContent does not overwrite a name the user has already entered`() {
        val stepConfig = SuperiorLandlordNameStepConfig()
        val formModel = SuperiorLandlordNameFormModel().apply { name = "Entered name" }

        val result = stepConfig.resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        assertEquals("Entered name", (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordNameFormModel).name)
    }

    @Test
    fun `resolvePageContent leaves the name empty when adding a new superior landlord`() {
        val stepConfig = SuperiorLandlordNameStepConfig()
        whenever(mockJourneyState.editingSuperiorLandlord).thenReturn(null)

        val result = stepConfig.resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordNameFormModel()))

        assertNull((result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordNameFormModel).name)
    }
}
