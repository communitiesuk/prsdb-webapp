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
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@WebMvcTest(RegisterLandlordController::class)
class RegisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordRegistrationJourneyFactory: LandlordRegistrationJourneyFactory

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @Test
    fun `index returns 200 for unauthenticated user`() {
        mvc.get(LANDLORD_REGISTRATION_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `index returns 308 for unauthenticated user with trailing slash`() {
        mvc.get("$LANDLORD_REGISTRATION_ROUTE/").andExpect {
            status { isPermanentRedirect() }
        }
    }

    @Test
    fun `getStart returns 200 for unauthenticated user`() {
        mvc.get(LANDLORD_REGISTRATION_START_PAGE_ROUTE).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `getStart returns 308 for unauthenticated user with trailing slash`() {
        mvc.get("$LANDLORD_REGISTRATION_START_PAGE_ROUTE/").andExpect {
            status { isPermanentRedirect() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep redirects to dashboard for landlord on privacy notice step`() {
        whenever(userRolesService.getHasLandlordUserRole(any())).thenReturn(true)

        mvc
            .get("$LANDLORD_REGISTRATION_ROUTE/${PrivacyNoticeStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(LANDLORD_DASHBOARD_URL)
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns privacy notice step for authenticated user on it`() {
        whenever(userRolesService.getHasLandlordUserRole(any())).thenReturn(false)

        val journeyId = "test-journey-id"
        whenever(landlordRegistrationJourneyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())
        whenever(landlordRegistrationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .get("$LANDLORD_REGISTRATION_ROUTE/${PrivacyNoticeStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(PrivacyNoticeStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 for user registered as landlord`() {
        val landlord = MockLandlordData.createLandlord()
        whenever(landlordService.retrieveLandlordByBaseUserId(any())).thenReturn(landlord)

        mvc
            .get(LANDLORD_REGISTRATION_CONFIRMATION_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 for user not registered as landlord`() {
        whenever(landlordService.retrieveLandlordByBaseUserId(any())).thenReturn(null)

        mvc
            .get(LANDLORD_REGISTRATION_CONFIRMATION_ROUTE)
            .andExpect {
                status { is4xxClientError() }
            }
    }
}
