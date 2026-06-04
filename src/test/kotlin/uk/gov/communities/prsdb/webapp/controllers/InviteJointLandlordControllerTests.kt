package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord.InviteJointLandlordJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(InviteJointLandlordController::class)
class InviteJointLandlordControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: InviteJointLandlordJourneyFactory

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        InviteJointLandlordController.getInviteJointLandlordRoute(propertyOwnershipId) +
            "/${InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT}"

    override val formContent = "email=test@example.com"

    override fun stubCreateJourneySteps() {
        whenever(journeyFactory.createJourneySteps(propertyOwnershipId))
            .thenReturn(mapOf(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    fun `getConfirmation returns a redirect for unauthenticated user`() {
        mvc.get(confirmationRoute).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 403 for an unauthorised user`() {
        mvc.get(confirmationRoute).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getConfirmation returns 404 for a landlord user not authorised to edit the property`() {
        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(false)

        mvc.get(confirmationRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `getConfirmation returns 200 for an authorised landlord user`() {
        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(true)

        mvc.get(confirmationRoute).andExpect {
            status { isOk() }
            model { attributeExists("propertyDetailsUrl") }
        }
    }

    private val confirmationRoute =
        InviteJointLandlordController.getInviteJointLandlordRoute(propertyOwnershipId) + "/confirmation"
}
