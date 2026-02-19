package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep

@WebMvcTest(JoinPropertyController::class)
class JoinPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var joinPropertyJourneyFactory: JoinPropertyJourneyFactory

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc
            .get(JOIN_PROPERTY_ROUTE)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `index returns 403 for unauthorized user`() {
        mvc
            .get(JOIN_PROPERTY_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `index returns start page for authorized landlord`() {
        mvc
            .get(JOIN_PROPERTY_ROUTE)
            .andExpect {
                status { isOk() }
                view { name("joinPropertyStartPage") }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getStart redirects to find property step with journey state`() {
        val journeyId = "test-journey-id"
        whenever(joinPropertyJourneyFactory.initializeJourneyState(org.mockito.kotlin.any())).thenReturn(journeyId)

        mvc
            .get(JOIN_PROPERTY_START_PAGE_ROUTE)
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(FindPropertyStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        whenever(joinPropertyJourneyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())
        whenever(joinPropertyJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .get("$JOIN_PROPERTY_ROUTE/${FindPropertyStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(FindPropertyStep.ROUTE_SEGMENT, journeyId))
            }
    }
}
