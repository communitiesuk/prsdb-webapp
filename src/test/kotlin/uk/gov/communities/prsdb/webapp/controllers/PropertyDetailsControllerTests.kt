package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
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
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attribute("isJointLandlordsEnabled", true) }
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
                model { attribute("isJointLandlordsEnabled", false) }
                model { attributeDoesNotExist("pendingInvitations") }
                model { attributeDoesNotExist("expiredInvitations") }
            }

            verify(jointLandlordInvitationService, never()).getPendingAndExpiredInvitations(any())
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
}
