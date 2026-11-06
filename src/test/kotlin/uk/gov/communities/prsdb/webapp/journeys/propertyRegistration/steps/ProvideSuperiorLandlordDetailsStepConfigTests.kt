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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@ExtendWith(MockitoExtension::class)
class ProvideSuperiorLandlordDetailsStepConfigTests {
    @Mock
    lateinit var mockJourneyState: SuperiorLandlordsDetailsState

    @Test
    fun `getStepSpecificContent returns the provide details content`() {
        // Act
        val content = ProvideSuperiorLandlordDetailsStepConfig().getStepSpecificContent(mockJourneyState)

        // Assert
        assertEquals(
            mapOf(
                "fieldName" to "provideDetails",
                "fieldSetHeading" to "registerProperty.superiorLandlords.provideDetails.fieldSetHeading",
                "radioOptions" to
                    RadiosViewModel.yesOrNoRadios(
                        yesHint = "registerProperty.superiorLandlords.provideDetails.radios.option.yes.hint",
                        noHint = "registerProperty.superiorLandlords.provideDetails.radios.option.no.hint",
                    ),
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `chooseTemplate returns the provide details form template`() {
        assertEquals("forms/provideDetailsForm", ProvideSuperiorLandlordDetailsStepConfig().chooseTemplate(mockJourneyState))
    }

    @Test
    fun `resolvePageContent prefills yes when the superior landlord being changed has known details`() {
        // Arrange
        whenever(mockJourneyState.editingSuperiorLandlord)
            .thenReturn(SuperiorLandlordDataModel(detailsKnown = true, name = "Name", email = "email@example.com"))

        // Act
        val result =
            ProvideSuperiorLandlordDetailsStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordProvideDetailsFormModel()))

        // Assert
        assertEquals(true, (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordProvideDetailsFormModel).provideDetails)
    }

    @Test
    fun `resolvePageContent prefills no when the superior landlord being changed has unknown details`() {
        // Arrange
        whenever(mockJourneyState.editingSuperiorLandlord).thenReturn(SuperiorLandlordDataModel(detailsKnown = false))

        // Act
        val result =
            ProvideSuperiorLandlordDetailsStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordProvideDetailsFormModel()))

        // Assert
        assertEquals(false, (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordProvideDetailsFormModel).provideDetails)
    }

    @Test
    fun `resolvePageContent does not overwrite an answer the user has already given`() {
        // Arrange
        val formModel = SuperiorLandlordProvideDetailsFormModel(provideDetails = false)

        // Act
        val result =
            ProvideSuperiorLandlordDetailsStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to formModel))

        // Assert
        assertEquals(false, (result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordProvideDetailsFormModel).provideDetails)
    }

    @Test
    fun `resolvePageContent leaves the answer empty when adding a new superior landlord`() {
        // Arrange
        whenever(mockJourneyState.editingSuperiorLandlord).thenReturn(null)

        // Act
        val result =
            ProvideSuperiorLandlordDetailsStepConfig()
                .resolvePageContent(mockJourneyState, mapOf(FORM_MODEL_ATTR_NAME to SuperiorLandlordProvideDetailsFormModel()))

        // Assert
        assertNull((result[FORM_MODEL_ATTR_NAME] as SuperiorLandlordProvideDetailsFormModel).provideDetails)
    }
}
