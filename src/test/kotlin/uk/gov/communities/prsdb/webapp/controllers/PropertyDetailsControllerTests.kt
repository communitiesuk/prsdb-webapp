package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import kotlin.test.Test

@WebMvcTest(PropertyDetailsController::class)
class PropertyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var viewModelFactory: PropertyComplianceViewModelFactory

    @MockitoBean
    private lateinit var jointLandlordsStrategy: JointLandlordsPropertyRegistrationStrategy

    @MockitoBean
    private lateinit var jointLandlordInvitationService: JointLandlordInvitationService

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @Nested
    inner class GetPropertyDetailsLandlordViewTests {
        @Test
        fun `getPropertyDetails returns a redirect for an unauthenticated user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = false)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getPropertyDetails returns 403 for an unauthorized user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = false)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getPropertyDetails returns 403 for an unauthorized user with local council admin role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = false)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetails returns 403 for an unauthorized user with local council user role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = false)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails returns 200 for a valid request from a landlord`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails fetches invitations when joint landlords feature is enabled`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(true)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attribute("jointLandlordsIsEnabled", true) }
                model { attributeExists("pendingInvitations") }
                model { attributeExists("expiredInvitations") }
            }

            verify(jointLandlordInvitationService).getPendingAndExpiredInvitations(propertyOwnership)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails does not fetch invitations when joint landlords feature is disabled`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(false)

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attribute("jointLandlordsIsEnabled", false) }
                model { attributeDoesNotExist("pendingInvitations") }
                model { attributeDoesNotExist("expiredInvitations") }
            }

            verify(jointLandlordInvitationService, never()).getPendingAndExpiredInvitations(any())
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails shows invite joint landlord button when feature flag is enabled`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(true)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeExists("inviteJointLandlordUrl") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails does not show invite joint landlord button when feature flag is disabled`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeDoesNotExist("inviteJointLandlordUrl") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails passes markedJointLandlord false when property is individual`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = false)

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attribute("markedJointLandlord", false) }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails passes markedJointLandlord true when property is joint`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attribute("markedJointLandlord", true) }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails shows switch to individual inset if the property is marked as JL and there is only one landlord`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model {
                    attribute(
                        "switchToIndividualLink",
                        SwitchToIndividualController.getSwitchToIndividualFirstStepPath(propertyOwnership.id),
                    )
                }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails does not show switch to individual inset when feature flag is disabled`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeDoesNotExist("switchToIndividualLink") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails does not show switch to individual inset when property is not marked as joint landlord`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = false)

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeDoesNotExist("switchToIndividualLink") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails does not show switch to individual inset when property has multiple landlords`() {
            val propertyOwnership =
                createPropertyOwnership(
                    markedJointLandlord = true,
                    otherLandlords = mutableSetOf(createLandlord()),
                )

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordsStrategy.ifEnabled(any())).doAnswer { invocation ->
                val action = invocation.getArgument<() -> Unit>(0)
                action()
            }
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeDoesNotExist("switchToIndividualLink") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails with joint landlords enabled adds landlordSummaryCards to model`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(true)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeExists("landlordSummaryCards") }
                model { attributeExists("landlordCount") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails with joint landlords disabled adds landlordDetails to model`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(false)

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeExists("landlordDetails") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetails with joint landlords enabled includes correct landlord count`() {
            val landlord1 = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
            val landlord2 = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser("user-2"))
            val propertyOwnership = createPropertyOwnership(primaryLandlord = landlord1, otherLandlords = mutableSetOf(landlord2))

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(propertyOwnership.id), any()))
                .thenReturn(propertyOwnership)
            whenever(featureFlagManager.checkFeature(JOINT_LANDLORDS)).thenReturn(true)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attribute("landlordCount", 2) }
            }
        }
    }

    @Nested
    inner class GetPropertyDetailsLocalCouncilViewTests {
        @Test
        fun `getPropertyDetailsLocalCouncilView returns a redirect for an unauthenticated user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getPropertyDetailsLocalCouncilView returns 403 for an unauthorized user`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getPropertyDetailsLocalCouncilView returns 403 for an unauthorized user with only the landlord role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView returns 200 for a valid request from an LocalCouncil user`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(1), any()))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getPropertyDetailsLocalCouncilView returns 200 for a valid request from an LocalCouncil admin`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfAuthorizedUser(eq(1), any()))
                .thenReturn(
                    propertyOwnership,
                )

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isOk() } }
            }
        }
    }

    @Nested
    inner class RemoveExpiredInviteTests {
        @Test
        fun `removeExpiredInvite returns a redirect for an unauthenticated user`() {
            mvc.get(PropertyDetailsController.getRemoveExpiredInvitePath(1L, 1L)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `removeExpiredInvite returns 403 for an unauthorized user`() {
            mvc.get(PropertyDetailsController.getRemoveExpiredInvitePath(1L, 1L)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `removeExpiredInvite redirects to property details with flash attribute on success`() {
            mvc.get(PropertyDetailsController.getRemoveExpiredInvitePath(1L, 1L)).andExpect {
                status { is3xxRedirection() }
                flash { attribute("inviteRemoved", true) }
            }

            verify(jointLandlordInvitationService).hideExpiredInvitation(eq(1L), any())
        }
    }
}
