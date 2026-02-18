package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.JoinPropertyJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.StartPageStep

@WebMvcTest(JoinPropertyController::class)
class JoinPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var joinPropertyJourneyFactory: JoinPropertyJourneyFactory

    @Test
    fun `getJourneyStep returns a redirect for unauthenticated user`() {
        mvc
            .get(JOIN_PROPERTY_START_PAGE_ROUTE)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for unauthorized user`() {
        mvc
            .get(JOIN_PROPERTY_START_PAGE_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        whenever(joinPropertyJourneyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())
        whenever(joinPropertyJourneyFactory.initializeJourneyState(org.mockito.kotlin.any())).thenReturn(journeyId)

        mvc
            .get("$JOIN_PROPERTY_ROUTE/${StartPageStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(StartPageStep.ROUTE_SEGMENT, journeyId))
            }
    }
}
