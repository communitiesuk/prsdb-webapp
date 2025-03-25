package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService

@WebMvcTest(DeregisterLandlordController::class)
class DeregisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @MockBean
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @MockBean
    private lateinit var landlordDeregistrationJourney: LandlordDeregistrationJourney

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
        whenever(landlordDeregistrationService.getLandlordHasRegisteredProperties(anyString())).thenReturn(false)
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
            .andExpect { redirectedUrl("/are-you-sure") }
    }
}
