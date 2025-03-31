package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@WebMvcTest(DeregisterLandlordController::class)
class DeregisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var landlordDeregistrationJourney: LandlordDeregistrationJourney

    @MockitoBean
    private lateinit var journeyDataServiceFactory: JourneyDataServiceFactory

    @MockitoBean
    private lateinit var journeyDataService: JourneyDataService

    @Test
    fun `checkForRegisteredProperties returns a redirect for an unauthenticated user`() {
        mvc
            .get(DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `checkForRegisteredProperties returns 403 for a user who is not a landlord`() {
        mvc
            .get(DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `checkForRegisteredProperties caches userHasRegisteredProperties then returns a redirect to the are you sure step`() {
        landlordDeregistrationJourney = mock()
        whenever(landlordDeregistrationJourneyFactory.create()).thenReturn(landlordDeregistrationJourney)
        whenever(landlordService.getLandlordHasRegisteredProperties(anyString())).thenReturn(false)
        whenever(
            landlordDeregistrationJourney
                .completeStep(
                    eq(DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment),
                    eq(
                        mutableMapOf(
                            USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to false,
                        ),
                    ),
                    eq(null),
                    anyOrNull(),
                ),
        ).thenReturn(ModelAndView("redirect:/are-you-sure"))

        mvc
            .get(DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
            .andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl("/${DeregisterLandlordStepId.AreYouSure.urlPathSegment}") }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if the current user is not in the landlord database`() {
        val journeyData =
            mutableMapOf(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment to
                    mutableMapOf(
                        USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to false,
                    ),
            ) as JourneyData
        journeyDataService = mock()
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)
        whenever(journeyDataServiceFactory.create(DEREGISTER_LANDLORD_JOURNEY_URL)).thenReturn(journeyDataService)

        mvc
            .get("/$DEREGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if the current user is still in the landlord database`() {
        whenever(landlordService.retrieveLandlordByBaseUserId("user"))
            .thenReturn(MockLandlordData.createLandlord())

        mvc
            .get("/$DEREGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { is5xxServerError() } }
    }
}
