package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
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
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.getLandlordDeregistrationPath
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory

@WebMvcTest(DeregisterLandlordController::class)
class DeregisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @MockBean
    private lateinit var landlordDeregistrationJourney: LandlordDeregistrationJourney

    @Test
    fun `checkForRegisteredProperties returns a redirect for an unauthenticated user`() {
        mvc
            .get(getLandlordDeregistrationPath())
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `checkForRegisteredProperties returns 403 for a user who is not a landlord`() {
        mvc
            .get(getLandlordDeregistrationPath())
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `checkForRegisteredProperties caches userHasRegisteredProperties then returns a redirect to the are you sure step`() {
        landlordDeregistrationJourney = mock()
        whenever(landlordDeregistrationJourneyFactory.create()).thenReturn(landlordDeregistrationJourney)
        whenever(
            landlordDeregistrationJourney
                .completeStep(
                    eq(CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT),
                    // TODO: PRSD-703 - Replace this with a service mock when we are checking the database for the user's registered properties
                    eq(
                        mutableMapOf(
                            USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY to false.toString(),
                        ),
                    ),
                    eq(null),
                    anyOrNull(),
                ),
        ).thenReturn(ModelAndView("redirect:/are-you-sure"))

        mvc
            .get(getLandlordDeregistrationPath())
            .andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl("/are-you-sure") }
    }
}
