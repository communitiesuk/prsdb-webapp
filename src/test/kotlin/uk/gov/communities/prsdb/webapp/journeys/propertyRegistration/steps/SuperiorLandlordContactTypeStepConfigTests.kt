package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordNameFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@ExtendWith(MockitoExtension::class)
class SuperiorLandlordContactTypeStepConfigTests {
    @Mock
    lateinit var mockJourneyState: SuperiorLandlordsDetailsState

    @Mock
    lateinit var mockSuperiorLandlordNameStep: SuperiorLandlordNameStep

    @Test
    fun `getStepSpecificContent includes superior landlord name and contact type content`() {
        // Arrange
        val stepConfig = SuperiorLandlordContactTypeStepConfig()
        whenever(mockJourneyState.superiorLandlordNameStep).thenReturn(mockSuperiorLandlordNameStep)
        whenever(mockSuperiorLandlordNameStep.formModelOrNull).thenReturn(
            SuperiorLandlordNameFormModel().apply { name = "James Hassan" },
        )

        // Act
        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals(
            mapOf(
                "fieldName" to "contactType",
                "fieldSetHeading" to "registerProperty.superiorLandlords.contactType.fieldSetHeading",
                "fieldSetHeadingParam" to "James Hassan",
                "fieldSetHint" to "registerProperty.superiorLandlords.contactType.fieldSetHint",
                "radioOptions" to
                    listOf(
                        RadiosButtonViewModel(
                            value = SuperiorLandlordContactType.EMAIL,
                            labelMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.email.label",
                            hintMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.email.hint",
                        ),
                        RadiosButtonViewModel(
                            value = SuperiorLandlordContactType.POSTAL_ADDRESS,
                            labelMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.postalAddress.label",
                        ),
                    ),
                "submitButtonText" to "forms.buttons.continue",
            ),
            content,
        )
    }

    @Test
    fun `chooseTemplate returns the contact type form template`() {
        assertEquals("forms/contactTypeForm", SuperiorLandlordContactTypeStepConfig().chooseTemplate(mockJourneyState))
    }

    @Test
    fun `resolvePageContent prefills email when the superior landlord being changed has an email`() {
        // Arrange
        whenever(mockJourneyState.editingSuperiorLandlord)
            .thenReturn(SuperiorLandlordDataModel(detailsKnown = true, name = "Name", email = "email@example.com"))

        // Act
        val result =
            SuperiorLandlordContactTypeStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordContactTypeFormModel()))

        // Assert
        assertEquals(
            SuperiorLandlordContactType.EMAIL,
            (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordContactTypeFormModel).contactType,
        )
    }

    @Test
    fun `resolvePageContent prefills postal address when the superior landlord being changed has an address`() {
        // Arrange
        val address = AddressDataModel.fromManualAddressData("1 Street", "Town", "AB1 2CD")
        whenever(mockJourneyState.editingSuperiorLandlord)
            .thenReturn(SuperiorLandlordDataModel(detailsKnown = true, name = "Name", address = address))

        // Act
        val result =
            SuperiorLandlordContactTypeStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordContactTypeFormModel()))

        // Assert
        assertEquals(
            SuperiorLandlordContactType.POSTAL_ADDRESS,
            (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordContactTypeFormModel).contactType,
        )
    }

    @Test
    fun `resolvePageContent does not overwrite a contact type the user has already chosen`() {
        // Arrange
        val formModel = SuperiorLandlordContactTypeFormModel(contactType = SuperiorLandlordContactType.POSTAL_ADDRESS)

        // Act
        val result =
            SuperiorLandlordContactTypeStepConfig().resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        // Assert
        assertEquals(
            SuperiorLandlordContactType.POSTAL_ADDRESS,
            (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordContactTypeFormModel).contactType,
        )
    }

    @Test
    fun `resolvePageContent leaves the contact type empty when adding a new superior landlord`() {
        // Arrange
        whenever(mockJourneyState.editingSuperiorLandlord).thenReturn(null)

        // Act
        val result =
            SuperiorLandlordContactTypeStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordContactTypeFormModel()))

        // Assert
        assertNull((result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordContactTypeFormModel).contactType)
    }
}
