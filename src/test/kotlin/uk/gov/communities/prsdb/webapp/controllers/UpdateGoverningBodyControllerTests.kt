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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody.UpdateGoverningBodyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord

@WebMvcTest(UpdateGoverningBodyController::class)
class UpdateGoverningBodyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateGoverningBodyJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var mockUserToLandlordService: UserToLandlordService

    val updateGoverningBodyRoute =
        UpdateGoverningBodyController.UPDATE_GOVERNING_BODY_ROUTE +
            "/${OrgGovBodyMemberListStep.ROUTE_SEGMENT}"

    @Test
    fun `getUpdateStep returns a redirect for unauthenticated user`() {
        mvc.get(updateGoverningBodyRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getUpdateStep returns 403 for an unauthorised user`() {
        mvc.get(updateGoverningBodyRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 403 for a non-organisation landlord`() {
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc.get(updateGoverningBodyRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 403 for a registered company organisation landlord as cannot have governing body`() {
        val registeredCompanyOrg = OrganisationalLandlord()
        registeredCompanyOrg.companyNumber = "12345678"
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(registeredCompanyOrg)

        mvc.get(updateGoverningBodyRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getUpdateStep returns 200 for an eligible organisation landlord`() {
        val eligibleOrg = OrganisationalLandlord()
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(eligibleOrg)
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(OrgGovBodyMemberListStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateGoverningBodyRoute).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `postUpdateStep returns a redirect for unauthenticated user`() {
        mvc
            .post(updateGoverningBodyRoute) {
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
            .post(updateGoverningBodyRoute) {
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
            .post(updateGoverningBodyRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postUpdateStep returns 200 for an eligible organisation landlord`() {
        val eligibleOrg = OrganisationalLandlord()
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(eligibleOrg)
        whenever(
            mockJourneyFactory.createJourneySteps(),
        ).thenReturn(mapOf(OrgGovBodyMemberListStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.postStepModelAndView(any()),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .post(updateGoverningBodyRoute) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }
}
