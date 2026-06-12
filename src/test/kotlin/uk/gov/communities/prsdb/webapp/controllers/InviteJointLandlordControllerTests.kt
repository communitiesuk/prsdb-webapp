package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord.InviteJointLandlordJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@WebMvcTest(InviteJointLandlordController::class)
class InviteJointLandlordControllerTests(
    @Autowired webContext: WebApplicationContext,
) : BasePropertyDetailsUpdateControllerTests(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: InviteJointLandlordJourneyFactory

    @MockitoBean
    private lateinit var jointLandlordInvitationService: JointLandlordInvitationService

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    override lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    override lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    override val propertyOwnershipId = 1L

    override val updateStepRoute =
        InviteJointLandlordController.getInviteJointLandlordRoute(propertyOwnershipId) +
            "/${InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT}"

    override val formContent = "email=test@example.com"

    private val confirmationRoute =
        InviteJointLandlordController.getInviteJointLandlordRoute(propertyOwnershipId) + "/confirmation"

    private val resendRoute = InviteJointLandlordController.getResendInvitationPath(propertyOwnershipId, 123L)

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

    @Test
    fun `resendInvitation returns a redirect for unauthenticated user`() {
        mvc
            .get(resendRoute)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `resendInvitation returns 403 for an unauthorised user`() {
        mvc
            .get(resendRoute)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `resendInvitation returns 404 for a landlord user not authorised to edit the property`() {
        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(false)

        mvc
            .get(resendRoute)
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = LANDLORD_USER)
    fun `resendInvitation redirects to property details with flash attribute for authorised user`() {
        val mockPropertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyOwnershipId)
        val mockLandlord = MockLandlordData.createLandlord()

        whenever(propertyOwnershipService.getIsAuthorizedToEditRecord(propertyOwnershipId, LANDLORD_USER))
            .thenReturn(true)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId))
            .thenReturn(mockPropertyOwnership)
        whenever(landlordService.retrieveLandlordByBaseUserId(LANDLORD_USER))
            .thenReturn(mockLandlord)
        whenever(jointLandlordInvitationService.resendInvitation(eq(123L), any<PropertyOwnership>(), any<Landlord>()))
            .thenReturn("joint@example.com")

        mvc
            .get(resendRoute)
            .andExpect {
                status { is3xxRedirection() }
                flash { attributeExists("resendInvitationEmail") }
                flash { attribute("resendInvitationEmail", "joint@example.com") }
            }
    }
}
