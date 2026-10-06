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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee.UpdateLeadTrusteeJourneyFactory
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@WebMvcTest(UpdateLeadTrusteeController::class)
class UpdateLeadTrusteeControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BaseOrganisationalLandlordUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var mockJourneyFactory: UpdateLeadTrusteeJourneyFactory

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    override lateinit var userToLandlordService: UserToLandlordService

    override val updateStepRoute =
        UpdateLeadTrusteeController.UPDATE_LEAD_TRUSTEE_ROUTE +
            "/${LeadTrusteeNameStep.ROUTE_SEGMENT}"

    override fun validOrganisationLandlord(): OrganisationalLandlord {
        val trustOrg = OrganisationalLandlord()
        trustOrg.isTrust = true
        return trustOrg
    }

    override fun stubCreateJourneySteps() {
        whenever(mockJourneyFactory.createJourneySteps())
            .thenReturn(mapOf(LeadTrusteeNameStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], value = "user")
    fun `getUpdateStep returns 403 for a non-trust organisation landlord`() {
        val nonTrustOrg = OrganisationalLandlord()
        nonTrustOrg.isTrust = false
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(nonTrustOrg)

        mvc.get(updateStepRoute).andExpect {
            status { isForbidden() }
        }
    }
}
