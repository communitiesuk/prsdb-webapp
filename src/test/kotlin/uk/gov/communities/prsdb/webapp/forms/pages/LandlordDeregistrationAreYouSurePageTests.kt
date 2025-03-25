package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.assertNotNull

class LandlordDeregistrationAreYouSurePageTests {
    @MockBean
    private lateinit var journeyDataService: JourneyDataService

    private lateinit var page: LandlordDeregistrationAreYouSurePage

    @BeforeEach
    fun setup() {
        journeyDataService = mock()

        page =
            LandlordDeregistrationAreYouSurePage(mutableMapOf(), journeyDataService) {
                mock()
            }
    }

    @Test
    fun `bindDataToFormModel adds userHasRegisteredProperties to the model `() {
        // Arrange
        val formData =
            mapOf(
                "wantsToProceed" to null,
            ) as PageData
        val journeyData =
            mutableMapOf(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to true,
                    ),
            ) as JourneyData
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val mockValidator = mock<Validator>()
        whenever(mockValidator.supports(anyOrNull())).thenReturn(true)

        // Act
        val modelAndView =
            page.getModelAndView(
                mockValidator,
                formData,
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment,
                journeyData,
                null,
            )

        // Assert
        val userHasRegisteredProperties =
            (
                (
                    modelAndView
                        .model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
                ).target as LandlordDeregistrationAreYouSureFormModel
            ).userHasRegisteredProperties

        assertNotNull(userHasRegisteredProperties)
        assertTrue(userHasRegisteredProperties)
    }
}
