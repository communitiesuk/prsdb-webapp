package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.UpdateOrganisationTypeJourneyFactory
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord

@WebMvcTest(UpdateOrganisationTypeController::class)
class UpdateOrganisationTypeControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateOrganisationTypeJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var mockUserToLandlordService: UserToLandlordService

    val updateOrgTypeRoute =
        UpdateOrganisationTypeController.UPDATE_ORG_TYPE_ROUTE +
            "/${OrgTypeStep.ROUTE_SEGMENT}"

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateOrgTypeRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getUpdateStep returns 403 for an unauthorised user`() {
        mvc
            .get(updateOrgTypeRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 403 for a non-organisation landlord`() {
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .get(updateOrgTypeRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 200 for an organisation landlord`() {
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(OrganisationalLandlord())
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(OrgTypeStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get(updateOrgTypeRoute)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `postUpdateStep returns a redirect for unauthenticated user`() {
        mvc
            .post(updateOrgTypeRoute) {
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
            .post(updateOrgTypeRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postUpdateStep returns 403 for a non-organisation landlord`() {
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .post(updateOrgTypeRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postUpdateStep returns 200 for an organisation landlord`() {
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(OrganisationalLandlord())
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(OrgTypeStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.postStepModelAndView(any()),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .post(updateOrgTypeRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }
}
