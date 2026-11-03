package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class FreeholderContactTypeStepConfigTests {
    private val state = mock<FreeholdersDetailsState>()
    private val freeholderNameStep = mock<FreeholderNameStep>()

    @Test
    fun `getStepSpecificContent includes freeholder name and contact type content`() {
        // Arrange
        whenever(state.freeholderNameStep).thenReturn(freeholderNameStep)
        whenever(freeholderNameStep.formModelOrNull).thenReturn(
            FreeholderNameFormModel().apply { name = "James Hassan" },
        )

        // Act
        val content = FreeholderContactTypeStepConfig().getStepSpecificContent(state)

        // Assert
        assertEquals(
            mapOf(
                "fieldName" to "contactType",
                "fieldSetHeading" to "registerProperty.freeholders.contactType.fieldSetHeading",
                "fieldSetHeadingParam" to "James Hassan",
                "fieldSetHint" to "registerProperty.freeholders.contactType.fieldSetHint",
                "radioOptions" to
                    listOf(
                        RadiosButtonViewModel(
                            value = FreeholderContactType.EMAIL,
                            labelMsgKey = "registerProperty.freeholders.contactType.radios.option.email.label",
                            hintMsgKey = "registerProperty.freeholders.contactType.radios.option.email.hint",
                        ),
                        RadiosButtonViewModel(
                            value = FreeholderContactType.POSTAL_ADDRESS,
                            labelMsgKey = "registerProperty.freeholders.contactType.radios.option.postalAddress.label",
                        ),
                    ),
                "submitButtonText" to "forms.buttons.continue",
            ),
            content,
        )
    }

    @Test
    fun `chooseTemplate returns the contact type form template`() {
        assertEquals("forms/contactTypeForm", FreeholderContactTypeStepConfig().chooseTemplate(state))
    }
}
