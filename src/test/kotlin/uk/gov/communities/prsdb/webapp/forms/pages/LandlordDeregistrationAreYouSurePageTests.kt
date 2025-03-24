package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.assertNotNull

class LandlordDeregistrationAreYouSurePageTests {
    @MockBean
    private lateinit var journeyDataService: JourneyDataService

    private lateinit var page: LandlordDeregistrationAreYouSurePage

    @BeforeEach
    fun setup() {
        journeyDataService = mock()
        page = LandlordDeregistrationAreYouSurePage(mutableMapOf(), journeyDataService)
    }

    @Test
    fun `enrichModel adds the no properties fieldset heading if the user has no registered properties`() {
        val journeyData =
            mutableMapOf(
                CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to false,
                    ),
            ) as JourneyData
        val modelAndView = ModelAndView()

        page.enrichModel(modelAndView, journeyData)

        assertEquals("forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading", modelAndView.model["fieldSetHeading"])
        assertNull(modelAndView.model["fieldSetHintList"])
    }

    @Test
    fun `enrichModel adds the with properties fieldset heading and hint if the user has registered properties`() {
        val journeyData =
            mutableMapOf(
                CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to true,
                    ),
            ) as JourneyData
        val modelAndView = ModelAndView()

        page.enrichModel(modelAndView, journeyData)

        assertEquals("forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHeading", modelAndView.model["fieldSetHeading"])
        assertEquals(listOf("forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHint"), modelAndView.model["fieldSetHintList"])
    }

    @Test
    fun `enrichModel throws an exception if userHasRegisteredProperties is not found in JourneyData`() {
        val journeyData = mutableMapOf<String, Any>() as JourneyData
        val thrownError = assertThrows<ResponseStatusException> { page.enrichModel(ModelAndView(), journeyData) }
        assertEquals(thrownError.statusCode, HttpStatus.INTERNAL_SERVER_ERROR)
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
                CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT to
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
                CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT,
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
