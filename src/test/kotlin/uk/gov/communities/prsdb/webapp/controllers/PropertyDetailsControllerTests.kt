package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.FreeholderSummaryCardHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.PropertyManagerSummaryCardHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.SuperiorLandlordSummaryCardHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsNotificationBannerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PropertyDetailsViewType
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.NotificationBannerViewModelService
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.PropertyComplianceViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createFreeholder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyManager
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createSuperiorLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData.Companion.statusDerivingService
import java.util.UUID
import kotlin.test.Test

@WebMvcTest(PropertyDetailsController::class)
class PropertyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var freeholderSummaryCardHelper: FreeholderSummaryCardHelper

    @MockitoBean
    private lateinit var superiorLandlordSummaryCardHelper: SuperiorLandlordSummaryCardHelper

    @MockitoBean
    private lateinit var propertyManagerSummaryCardHelper: PropertyManagerSummaryCardHelper

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var viewModelFactory: PropertyComplianceViewModelFactory

    @MockitoBean
    private lateinit var notificationBannerViewModelService: NotificationBannerViewModelService

    @MockitoBean
    private lateinit var jointLandlordInvitationService: JointLandlordInvitationService

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    // TODO PDJB-2016 and PDJB-2149: remove this mock when the ASSOCIATED_PARTIES and ACTIVE_INACTIVE flags are removed
    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @BeforeEach
    fun setUp() {
        // TODO PDJB-2016: remove this stubbing when the ASSOCIATED_PARTIES feature flag is removed
        whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(true)
        val propertyCompliance = PropertyComplianceBuilder.createWithInDateCerts()
        val complianceStatus = statusDerivingService.getComplianceStatus(propertyCompliance)
        whenever(propertyComplianceService.getComplianceForPropertyOrNull(any()))
            .thenReturn(propertyCompliance)
        whenever(propertyComplianceService.getComplianceStatus(any()))
            .thenReturn(complianceStatus)
        whenever(
            notificationBannerViewModelService.getPropertyDetailsNotificationBanner(
                anyOrNull(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(
            PropertyDetailsNotificationBannerViewModel.fromState(
                isLandlordView = true,
                isOccupied = false,
                isLicensingProvideLater = false,
                isTenancyProvideLater = false,
                complianceMessages = emptyList(),
            ),
        )
    }

    @Nested
    inner class GetPropertyDetailsLandlordViewTests {
        @BeforeEach
        fun setUpLandlord() {
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())
        }

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
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails returns 200 for a valid request from a landlord`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(
                    propertyOwnership,
                )
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { status { isOk() } }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails fetches invitations`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("pendingInvitations") }
                    model { attributeExists("expiredInvitations") }
                }

            verify(jointLandlordInvitationService).getPendingAndExpiredInvitations(propertyOwnership)
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails selects the property details view`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    view { name(PropertyDetailsController.PROPERTY_DETAILS_VIEW) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails adds cards built from the property's persisted freeholders`() {
            val propertyOwnership = createPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY)
            propertyOwnership.freeholders.add(
                createFreeholder(propertyOwnership, name = "Freeholder One", email = "freeholder@example.com"),
            )
            val expectedCards = listOf(SummaryCardViewModel(title = "Freeholder One", summaryList = emptyList()))
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(freeholderSummaryCardHelper.getSummaryCards(propertyOwnership.freeholders)).thenReturn(expectedCards)

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("freeholderSummaryCards", expectedCards) }
                }

            verify(freeholderSummaryCardHelper).getSummaryCards(propertyOwnership.freeholders)
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails adds cards built from the property's persisted superior landlords`() {
            val propertyOwnership = createPropertyOwnership(ownershipType = OwnershipType.SHARE_OF_SOLE_FREEHOLD)
            propertyOwnership.superiorLandlords.add(
                createSuperiorLandlord(propertyOwnership, name = "Superior One", email = "superior@example.com"),
            )
            val expectedCards = listOf(SummaryCardViewModel(title = "Superior One", summaryList = emptyList()))
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(superiorLandlordSummaryCardHelper.getSummaryCards(propertyOwnership.superiorLandlords)).thenReturn(expectedCards)

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("superiorLandlordSummaryCards", expectedCards) }
                }

            verify(superiorLandlordSummaryCardHelper).getSummaryCards(propertyOwnership.superiorLandlords)
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows correspondence section`() {
            val propertyOwnership =
                createPropertyOwnership(
                    correspondenceEmail = "correspondence@example.com",
                    correspondenceAddress = createAddress("25 Contact Road, Bristol, BS1 2AB"),
                )

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    content { string(containsString("correspondence@example.com")) }
                    content { string(containsString("25 Contact Road")) }
                    content { string(containsString("Bristol")) }
                    content { string(containsString("BS1 2AB")) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows invite joint landlord button`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("inviteJointLandlordUrl") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails passes markedJointLandlord false when property is individual`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = false)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("markedJointLandlord", false) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails passes markedJointLandlord true when property is joint`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("markedJointLandlord", true) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows the deactivate link and not the deregister link`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
            whenever(propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnership.id)).thenReturn(propertyOwnership)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            val deactivatePropertyLink = DeactivatePropertyController.getPropertyDeactivationPath(propertyOwnership.id)
            val deregisterPropertyLink = DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnership.id)

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("isActiveInactiveEnabled", true) }
                    model { attribute("deactivatePropertyLink", deactivatePropertyLink) }
                    content { string(containsString("href=\"$deactivatePropertyLink\"")) }
                    content { string(not(containsString("href=\"$deregisterPropertyLink\""))) }
                }
        }

        // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows the deregister link and not the deactivate link when ACTIVE_INACTIVE is disabled`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            val deactivatePropertyLink = DeactivatePropertyController.getPropertyDeactivationPath(propertyOwnership.id)
            val deregisterPropertyLink = DeregisterPropertyController.getPropertyDeregistrationPath(propertyOwnership.id)

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("isActiveInactiveEnabled", false) }
                    content { string(containsString("href=\"$deregisterPropertyLink\"")) }
                    content { string(not(containsString("href=\"$deactivatePropertyLink\""))) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows the deactivation success banner when the flash attribute is present`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
            whenever(propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnership.id)).thenReturn(propertyOwnership)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)) {
                    flashAttr("deactivationSuccessBanner", "propertyDetails.deactivationSuccessBanner.propertyDeactivated")
                }.andExpect {
                    status { isOk() }
                    content { string(containsString("id=\"deactivation-success-banner\"")) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows switch to individual inset if the property is marked as JL and there is only one landlord`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
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
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails does not show switch to individual inset when property is not marked as joint landlord`() {
            val propertyOwnership = createPropertyOwnership(markedJointLandlord = false)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeDoesNotExist("switchToIndividualLink") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails does not show switch to individual inset when property has multiple landlords`() {
            val propertyOwnership =
                createPropertyOwnership(
                    markedJointLandlord = true,
                    landlords =
                        mutableSetOf(
                            createIndividualLandlord(name = "Landlord 1"),
                            createIndividualLandlord(name = "Landlord 2"),
                        ),
                )

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeDoesNotExist("switchToIndividualLink") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails adds landlordSummaryCards to model`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("landlordSummaryCards") }
                    model { attributeExists("landlordCount") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails adds the property manager card when the property has a property manager`() {
            val propertyManager = createPropertyManager()
            val propertyOwnership = createPropertyOwnership().apply { this.propertyManager = propertyManager }
            val expectedCard = SummaryCardViewModel(title = propertyManager.name, summaryList = emptyList())
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(propertyManagerSummaryCardHelper.getSummaryCard(propertyManager)).thenReturn(expectedCard)

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("propertyManagerSummaryCard", expectedCard) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails does not add a property manager card when the property has no property manager`() {
            val propertyOwnership = createPropertyOwnership()
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeDoesNotExist("propertyManagerSummaryCard") }
                }
        }

        // TODO PDJB-2016: delete this class when the ASSOCIATED_PARTIES feature flag is removed
        @Nested
        inner class BeforeAssociatedParties {
            @BeforeEach
            fun disableAssociatedParties() {
                whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(false)
            }

            @Test
            @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
            fun `getPropertyDetails does not add a property manager card`() {
                val propertyOwnership = createPropertyOwnership().apply { propertyManager = createPropertyManager() }
                whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                    .thenReturn(propertyOwnership)
                whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                    .thenReturn(Pair(emptyList(), emptyList()))

                mvc
                    .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                    .andExpect {
                        status { isOk() }
                        model { attributeDoesNotExist("propertyManagerSummaryCard") }
                    }
            }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails includes correct landlord count`() {
            val landlord1 = createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-1"))
            val landlord2 = createIndividualLandlord(baseUser = MockLandlordData.createPrsdbUser("user-2"))
            val propertyOwnership = createPropertyOwnership(landlords = mutableSetOf(landlord1, landlord2))

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("landlordCount", 2) }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails returns 200 for an org landlord`() {
            val orgLandlord = createOrgLandlord()
            val propertyOwnership = createPropertyOwnership(landlords = mutableSetOf(orgLandlord))

            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(orgLandlord)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeExists("landlordSummaryCards") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails returns 200 for a property with mixed individual and org landlords`() {
            val individualLandlord = createIndividualLandlord()
            val orgLandlord = createOrgLandlord()
            val propertyOwnership = createPropertyOwnership(landlords = mutableSetOf(individualLandlord, orgLandlord))

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("landlordCount", 2) }
                    model { attributeExists("landlordSummaryCards") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails includes letting agent panel when an agent exists`() {
            val propertyOwnership = createPropertyOwnership(isOccupied = true, tenancyProvideLater = true)
            val lettingAgentAccess = LettingAgentAccess(UUID.randomUUID(), "agent@example.com", propertyOwnership)

            whenever(propertyOwnershipService.getLettingAgentAccess(propertyOwnership.id))
                .thenReturn(lettingAgentAccess)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("delegatesToLettingAgent", true) }
                    model { attribute("lettingAgentEmail", "agent@example.com") }
                    model { attributeExists("lettingAgentPanelLink") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows delegate panel when no agent assigned`() {
            val propertyOwnership = createPropertyOwnership(isOccupied = true, tenancyProvideLater = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("delegatesToLettingAgent", false) }
                    model { attributeDoesNotExist("lettingAgentEmail") }
                    model { attributeExists("lettingAgentPanelLink") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows the delegate to letting agent link for an occupied property`() {
            val propertyOwnership = createPropertyOwnership(isOccupied = true, tenancyProvideLater = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false)).andExpect {
                status { isOk() }
                model { attributeExists("lettingAgentPanelLink") }
            }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails does not include letting agent panel link when property is unoccupied`() {
            val propertyOwnership = createPropertyOwnership(isOccupied = false)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("propertyIsOccupied", false) }
                    model { attributeDoesNotExist("lettingAgentPanelLink") }
                }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails includes letting agent panel link when property is occupied and not delegated`() {
            val propertyOwnership = createPropertyOwnership(isOccupied = true, tenancyProvideLater = true)

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attribute("propertyIsOccupied", true) }
                    model { attributeExists("lettingAgentPanelLink") }
                }
        }
    }

    @Nested
    inner class GetDeactivatedPropertyDetailsLandlordViewTests {
        private val landlord = createIndividualLandlord()
        private val inactivePropertyOwnership =
            createPropertyOwnership(id = 2, isActive = false, landlords = mutableSetOf(landlord))
        private val path =
            PropertyDetailsController.getPropertyDetailsPath(inactivePropertyOwnership.id, isLocalCouncilView = false)

        @BeforeEach
        fun setUpDeactivated() {
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
            whenever(propertyOwnershipService.retrievePropertyOwnershipById(inactivePropertyOwnership.id)).thenReturn(null)
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails returns the deactivated view for an inactive property of the current landlord`() {
            whenever(propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(inactivePropertyOwnership.id))
                .thenReturn(inactivePropertyOwnership)

            mvc.get(path).andExpect {
                status { isOk() }
                view { name(PropertyDetailsController.PROPERTY_DETAILS_VIEW) }
                model { attribute("isDeactivated", true) }
                model { attribute("isLandlordView", true) }
                model {
                    attribute(
                        "reactivatePropertyUrl",
                        ReactivatePropertyController.getPropertyReactivationPath(inactivePropertyOwnership.id),
                    )
                }
                model { attribute("backUrl", LandlordController.LANDLORD_DASHBOARD_URL) }
                model { attributeExists("propertyDetails", "landlordSummaryCards", "landlordCount") }
                model {
                    attributeDoesNotExist(
                        "notificationBanner",
                        "deregisterPropertyLink",
                        "delegatesToLettingAgent",
                        "lettingAgentPanelLink",
                        "inviteJointLandlordUrl",
                        "switchToIndividualLink",
                        "pendingInvitations",
                        "expiredInvitations",
                    )
                }
            }

            verify(viewModelFactory).create(
                any(),
                any(),
                eq(PropertyDetailsViewType.DEACTIVATED_LANDLORD),
                eq(inactivePropertyOwnership.id),
                anyOrNull(),
            )
            verify(propertyOwnershipService, never()).getLettingAgentAccess(any())
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails returns 404 for an inactive property the current landlord does not own`() {
            whenever(propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(inactivePropertyOwnership.id))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc.get(path).andExpect { status { isNotFound() } }
        }

        // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails does not show the deactivated view when the active inactive flag is off`() {
            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(inactivePropertyOwnership.id))
                .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

            mvc.get(path).andExpect { status { isNotFound() } }

            verify(propertyOwnershipService, never()).getInactivePropertyOwnershipForCurrentLandlord(any())
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetails shows the normal view for an active property`() {
            val activePropertyOwnership = createPropertyOwnership(id = 3, landlords = mutableSetOf(landlord))
            whenever(propertyOwnershipService.retrievePropertyOwnershipById(activePropertyOwnership.id))
                .thenReturn(activePropertyOwnership)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(activePropertyOwnership.id))
                .thenReturn(activePropertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(activePropertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(activePropertyOwnership.id, isLocalCouncilView = false))
                .andExpect {
                    status { isOk() }
                    model { attributeDoesNotExist("isDeactivated") }
                    model { attributeExists("deregisterPropertyLink") }
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
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getPropertyDetailsLocalCouncilView returns 403 for an unauthorized user with only the landlord role`() {
            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isForbidden() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView returns 200 for a valid request from an LocalCouncil user`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(1)))
                .thenReturn(
                    propertyOwnership,
                )
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getPropertyDetailsLocalCouncilView returns 200 for a valid request from an LocalCouncil admin`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(1)))
                .thenReturn(
                    propertyOwnership,
                )
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { status { isOk() } }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView fetches invitations`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(1)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { isOk() }
                model { attributeExists("pendingInvitations") }
                model { attributeExists("expiredInvitations") }
            }

            verify(jointLandlordInvitationService).getPendingAndExpiredInvitations(propertyOwnership)
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView never shows the delegate to letting agent link`() {
            val propertyOwnership = createPropertyOwnership()

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(1)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { isOk() }
                model { attributeDoesNotExist("lettingAgentPanelLink") }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView shows correspondence section`() {
            val propertyOwnership =
                createPropertyOwnership(
                    correspondenceEmail = "correspondence@example.com",
                    correspondenceAddress = createAddress("25 Contact Road, Bristol, BS1 2AB"),
                )

            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(1)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            mvc.get(PropertyDetailsController.getPropertyDetailsPath(1L, isLocalCouncilView = true)).andExpect {
                status { isOk() }
                content { string(containsString("correspondence@example.com")) }
                content { string(containsString("25 Contact Road")) }
                content { string(containsString("Bristol")) }
                content { string(containsString("BS1 2AB")) }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView adds cards built from the property's persisted freeholders`() {
            // Arrange
            val propertyOwnership = createPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY)
            propertyOwnership.freeholders.add(
                createFreeholder(propertyOwnership, name = "Freeholder One", email = "freeholder@example.com"),
            )
            val expectedCards = listOf(SummaryCardViewModel(title = "Freeholder One", summaryList = emptyList()))
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(freeholderSummaryCardHelper.getSummaryCards(propertyOwnership.freeholders)).thenReturn(expectedCards)

            // Act & Assert
            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                .andExpect {
                    status { isOk() }
                    model { attribute("freeholderSummaryCards", expectedCards) }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView adds no freeholder cards when the property has no freeholders`() {
            // Arrange
            val propertyOwnership = createPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(freeholderSummaryCardHelper.getSummaryCards(propertyOwnership.freeholders)).thenReturn(emptyList())

            // Act & Assert
            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                .andExpect {
                    status { isOk() }
                    model { attribute("freeholderSummaryCards", emptyList<SummaryCardViewModel>()) }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView adds cards built from the property's persisted superior landlords`() {
            // Arrange
            val propertyOwnership = createPropertyOwnership(ownershipType = OwnershipType.SHARE_OF_SOLE_FREEHOLD)
            propertyOwnership.superiorLandlords.add(
                createSuperiorLandlord(propertyOwnership, name = "Superior One", email = "superior@example.com"),
            )
            val expectedCards = listOf(SummaryCardViewModel(title = "Superior One", summaryList = emptyList()))
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(superiorLandlordSummaryCardHelper.getSummaryCards(propertyOwnership.superiorLandlords)).thenReturn(expectedCards)

            // Act & Assert
            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                .andExpect {
                    status { isOk() }
                    model { attribute("superiorLandlordSummaryCards", expectedCards) }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView adds no superior landlord cards when the property has no superior landlords`() {
            // Arrange
            val propertyOwnership = createPropertyOwnership(ownershipType = OwnershipType.SHARE_OF_SOLE_FREEHOLD)
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(superiorLandlordSummaryCardHelper.getSummaryCards(propertyOwnership.superiorLandlords))
                .thenReturn(emptyList())

            // Act & Assert
            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                .andExpect {
                    status { isOk() }
                    model { attribute("superiorLandlordSummaryCards", emptyList<SummaryCardViewModel>()) }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView adds the property manager card when the property has a property manager`() {
            // Arrange
            val propertyManager = createPropertyManager()
            val propertyOwnership = createPropertyOwnership().apply { this.propertyManager = propertyManager }
            val expectedCard = SummaryCardViewModel(title = propertyManager.name, summaryList = emptyList())
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))
            whenever(propertyManagerSummaryCardHelper.getSummaryCard(propertyManager)).thenReturn(expectedCard)

            // Act & Assert
            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                .andExpect {
                    status { isOk() }
                    model { attribute("propertyManagerSummaryCard", expectedCard) }
                }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getPropertyDetailsLocalCouncilView does not add a property manager card when the property has no property manager`() {
            // Arrange
            val propertyOwnership = createPropertyOwnership()
            whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                .thenReturn(propertyOwnership)
            whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                .thenReturn(Pair(emptyList(), emptyList()))

            // Act & Assert
            mvc
                .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                .andExpect {
                    status { isOk() }
                    model { attributeDoesNotExist("propertyManagerSummaryCard") }
                }
        }

        // TODO PDJB-2016: delete this class when the ASSOCIATED_PARTIES feature flag is removed
        @Nested
        inner class BeforeAssociatedParties {
            @BeforeEach
            fun disableAssociatedParties() {
                whenever(featureFlagManager.checkFeature(ASSOCIATED_PARTIES)).thenReturn(false)
            }

            @Test
            @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
            fun `getPropertyDetailsLocalCouncilView does not add associated party cards`() {
                // Arrange
                val propertyOwnership =
                    createPropertyOwnership(ownershipType = OwnershipType.LEASEHOLD_ONLY).apply {
                        propertyManager = createPropertyManager()
                    }
                propertyOwnership.freeholders.add(createFreeholder(propertyOwnership))
                propertyOwnership.superiorLandlords.add(createSuperiorLandlord(propertyOwnership))
                whenever(propertyOwnershipService.getPropertyOwnershipIfCurrentUserAuthorized(eq(propertyOwnership.id)))
                    .thenReturn(propertyOwnership)
                whenever(jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership))
                    .thenReturn(Pair(emptyList(), emptyList()))

                // Act & Assert
                mvc
                    .get(PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true))
                    .andExpect {
                        status { isOk() }
                        model { attributeDoesNotExist("freeholderSummaryCards") }
                        model { attributeDoesNotExist("superiorLandlordSummaryCards") }
                        model { attributeDoesNotExist("propertyManagerSummaryCard") }
                    }
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
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `removeExpiredInvite redirects to property details with flash attribute on success`() {
            mvc.get(PropertyDetailsController.getRemoveExpiredInvitePath(1L, 1L)).andExpect {
                status { is3xxRedirection() }
                flash { attribute("inviteRemoved", true) }
            }

            verify(jointLandlordInvitationService).hideExpiredInvitation(eq(1L))
        }
    }
}
