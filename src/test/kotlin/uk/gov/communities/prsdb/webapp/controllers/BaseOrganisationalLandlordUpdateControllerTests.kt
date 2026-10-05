package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord

abstract class BaseOrganisationalLandlordUpdateControllerTests(
    webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    protected abstract val updateStepRoute: String
    protected abstract val stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator
    protected abstract val userToLandlordService: UserToLandlordService
    protected abstract val organisationalLandlordUserService: OrganisationalLandlordUserService

    protected abstract fun stubCreateJourneySteps()

    protected open fun validOrganisationLandlord(): OrganisationalLandlord = OrganisationalLandlord()

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
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"], value = "user")
    fun `getUpdateStep returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc.get(updateStepRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"], value = "user")
    fun `getUpdateStep returns 200 for an organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(validOrganisationLandlord())
        stubCreateJourneySteps()
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
                param("formData", "")
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
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"], value = "user")
    fun `postUpdateStep returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .post(updateStepRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"], value = "user")
    fun `postUpdateStep returns 200 for an organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(validOrganisationLandlord())
        stubCreateJourneySteps()
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .post(updateStepRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }
}
