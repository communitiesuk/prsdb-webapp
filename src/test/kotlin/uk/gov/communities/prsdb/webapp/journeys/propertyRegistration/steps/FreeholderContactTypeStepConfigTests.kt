package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@ExtendWith(MockitoExtension::class)
class FreeholderContactTypeStepConfigTests {
    @Mock
    lateinit var mockJourneyState: FreeholdersDetailsState

    @Mock
    lateinit var mockFreeholderNameStep: FreeholderNameStep

    @Test
    fun `getStepSpecificContent includes freeholder name and contact type content`() {
        // Arrange
        val stepConfig = FreeholderContactTypeStepConfig()
        whenever(mockJourneyState.freeholderNameStep).thenReturn(mockFreeholderNameStep)
        whenever(mockFreeholderNameStep.formModelOrNull).thenReturn(
            FreeholderNameFormModel().apply { name = FREEHOLDER_NAME },
        )

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals(
            mapOf(
                "fieldName" to "contactType",
                "fieldSetHeading" to "registerProperty.freeholders.contactType.fieldSetHeading",
                "fieldSetHeadingParam" to FREEHOLDER_NAME,
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
        assertEquals("forms/contactTypeForm", FreeholderContactTypeStepConfig().chooseTemplate(mockJourneyState))
    }

    @Test
    fun `resolvePageContent does not overwrite a contact type the user has already chosen`() {
        // Arrange
        val formModel = FreeholderContactTypeFormModel(contactType = FreeholderContactType.POSTAL_ADDRESS)

        // Act
        val result = FreeholderContactTypeStepConfig().resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        // Assert
        assertEquals(
            FreeholderContactType.POSTAL_ADDRESS,
            (result[FORM_MODEL_ATTR_NAME] as FreeholderContactTypeFormModel).contactType,
        )
    }

    @Test
    fun `resolvePageContent preselects email when editing a freeholder with an email`() {
        // Arrange
        whenever(mockJourneyState.editingFreeholder)
            .thenReturn(FreeholderDataModel(detailsKnown = true, name = FREEHOLDER_NAME, email = "email@example.com"))

        // Act
        val result =
            FreeholderContactTypeStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to FreeholderContactTypeFormModel()))

        // Assert
        assertEquals(FreeholderContactType.EMAIL, (result[FORM_MODEL_ATTR_NAME] as FreeholderContactTypeFormModel).contactType)
    }

    @Test
    fun `resolvePageContent preselects email when no form model exists`() {
        // Arrange
        whenever(mockJourneyState.editingFreeholder)
            .thenReturn(FreeholderDataModel(detailsKnown = true, name = FREEHOLDER_NAME, email = "email@example.com"))

        // Act
        val result = FreeholderContactTypeStepConfig().resolvePageContent(mockJourneyState, emptyMap())

        // Assert
        assertEquals(FreeholderContactType.EMAIL, (result[FORM_MODEL_ATTR_NAME] as FreeholderContactTypeFormModel).contactType)
    }

    @ParameterizedTest
    @MethodSource("provideEditedFreeholders")
    fun `resolvePageContent preselects contact type only when contact details are available`(
        editedFreeholder: FreeholderDataModel?,
        expectedContactType: FreeholderContactType?,
    ) {
        // Arrange
        whenever(mockJourneyState.editingFreeholder).thenReturn(editedFreeholder)

        // Act
        val result =
            FreeholderContactTypeStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to FreeholderContactTypeFormModel()))

        // Assert
        assertEquals(expectedContactType, (result[FORM_MODEL_ATTR_NAME] as FreeholderContactTypeFormModel).contactType)
    }

    companion object {
        private const val FREEHOLDER_NAME = "James Hassan"

        @JvmStatic
        fun provideEditedFreeholders() =
            listOf(
                Arguments.of(
                    named(
                        "freeholder with a postal address",
                        FreeholderDataModel(
                            detailsKnown = true,
                            name = FREEHOLDER_NAME,
                            address = AddressDataModel.fromManualAddressData("1 Street", "Town", "AB1 2CD"),
                        ),
                    ),
                    FreeholderContactType.POSTAL_ADDRESS,
                ),
                Arguments.of(
                    named(
                        "saved freeholder without a stored address",
                        FreeholderDataModel(databaseId = 1, detailsKnown = true, name = FREEHOLDER_NAME),
                    ),
                    null,
                ),
                Arguments.of(named("adding a new freeholder", null), null),
            )
    }
}
