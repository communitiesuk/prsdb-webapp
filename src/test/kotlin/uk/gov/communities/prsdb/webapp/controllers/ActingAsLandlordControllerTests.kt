package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.controllers.ActingAsLandlordController.Companion.STOP_ACTING_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.ActingAsLandlordController.Companion.getActAsLandlordPath
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.ACTING_AS_LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService

@WebMvcTest(ActingAsLandlordController::class)
class ActingAsLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var actingAsLandlordService: ActingAsLandlordService

    @Test
    fun `act-as redirects an unauthenticated user to sign in`() {
        mvc.post(getActAsLandlordPath(LANDLORD_ID)) { with(csrf()) }.andExpect { status { is3xxRedirection() } }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `act-as returns 403 for a user who is not a system operator`() {
        mvc.post(getActAsLandlordPath(LANDLORD_ID)) { with(csrf()) }.andExpect { status { isForbidden() } }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as starts acting as the landlord and redirects to the acting dashboard`() {
        mvc.post(getActAsLandlordPath(LANDLORD_ID)) { with(csrf()) }.andExpect {
            status { is3xxRedirection() }
            redirectedUrl(ACTING_AS_LANDLORD_DASHBOARD_URL)
        }

        verify(actingAsLandlordService).startActingAsLandlord(LANDLORD_ID, null)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as passes the member ID to the service`() {
        mvc.post(getActAsLandlordPath(LANDLORD_ID, MEMBER_ID)) { with(csrf()) }.andExpect { status { is3xxRedirection() } }

        verify(actingAsLandlordService).startActingAsLandlord(LANDLORD_ID, MEMBER_ID)
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `act-as returns the status of a rejection from the service`() {
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
            .whenever(actingAsLandlordService)
            .startActingAsLandlord(LANDLORD_ID, null)

        mvc.post(getActAsLandlordPath(LANDLORD_ID)) { with(csrf()) }.andExpect { status { isNotFound() } }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `stop-acting stops acting and redirects to the system operator dashboard`() {
        mvc.post(STOP_ACTING_ROUTE) { with(csrf()) }.andExpect {
            status { is3xxRedirection() }
            redirectedUrl(SYSTEM_OPERATOR_DASHBOARD_URL)
        }

        verify(actingAsLandlordService).stopActing()
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
    fun `stop-acting returns 403 for a user who is not a system operator`() {
        mvc.post(STOP_ACTING_ROUTE) { with(csrf()) }.andExpect { status { isForbidden() } }
    }

    companion object {
        private const val LANDLORD_ID = 1L
        private const val MEMBER_ID = 7L
    }
}
