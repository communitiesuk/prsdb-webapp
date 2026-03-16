package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

abstract class BasePropertyDetailsUpdateControllerTests(
    webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    protected abstract val propertyOwnershipId: Long
    protected abstract val updateStepRoute: String
    protected abstract val formContent: String
    protected abstract val propertyOwnershipService: PropertyOwnershipService
    protected abstract val stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    protected abstract fun stubJourneyStepGet()

    protected abstract fun stubJourneyStepPost(redirectUrl: String)

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateStepRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getUpdateStep returns 403 for an unauthorised user`() {
        mvc.get(updateStepRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getUpdateStep returns 404 for a landlord user not authorised to edit the property`() {
        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(false)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getUpdateStep returns 200 for a landlord user`() {
        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(true)
        stubJourneyStepGet()
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `postUpdateStep returns a redirect for unauthenticated user`() {
        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postUpdateStep returns 403 for an unauthorised user`() {
        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `postUpdateStep returns 404 for a landlord user not authorised to edit the property`() {
        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(false)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `postUpdateStep redirects for a valid landlord request`() {
        val redirectUrl = "/landlord/property-details/$propertyOwnershipId"

        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(true)
        stubJourneyStepPost(redirectUrl)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
    }

    companion object {
        const val LANDLORD_USER = "user"
    }
}
