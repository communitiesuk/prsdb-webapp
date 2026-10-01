package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody.UpdateGoverningBodyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@WebMvcTest(UpdateGoverningBodyController::class)
class UpdateGoverningBodyControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseOrganisationalLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateGoverningBodyJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    override lateinit var userToLandlordService: UserToLandlordService

    override val updateStepRoute =
        UpdateGoverningBodyController.UPDATE_GOVERNING_BODY_ROUTE +
            "/${OrgGovBodyMemberListStep.ROUTE_SEGMENT}"

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(OrgGovBodyMemberListStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], value = "user")
    fun `getUpdateStep returns 403 for a registered company organisation landlord as cannot have governing body`() {
        val registeredCompanyOrg = OrganisationalLandlord()
        registeredCompanyOrg.companyNumber = "12345678"
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(registeredCompanyOrg)

        mvc.get(updateStepRoute).andExpect {
            status { isForbidden() }
        }
    }
}
