package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.DEACTIVATED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.DeactivatedPropertyLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.OrgLandlordViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.OrganisationalLandlordContactsViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OrganisationPermissionsProvider
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.Test

@WebMvcTest(LandlordDetailsController::class)
@Import(MessageSourceConfig::class)
class LandlordDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var organisationPermissionsProvider: OrganisationPermissionsProvider

    // TODO PDJB-2149: Remove this mock when the ACTIVE_INACTIVE feature flag is removed
    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @Nested
    inner class GetUserLandlordDetailsTests {
        @Test
        fun `getUserLandlordDetails returns a redirect for an unauthenticated user`() {
            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getUserLandlordDetails returns 403 for an unauthorized user`() {
            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isForbidden() }
            }
        }

        // TODO PDJB-2149: Update to stub ACTIVE_PROPERTIES_FRAGMENT when the ACTIVE_INACTIVE feature flag is removed
        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getUserLandlordDetails returns 200 with the current view for a valid request from a landlord`() {
            val landlord = MockLandlordData.createIndividualLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(
                propertyOwnershipService.getActivePropertiesForLandlordUser(
                    landlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("individualLandlordDetailsView") }
                model { attributeExists("landlord") }
            }
        }

        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getUserLandlordDetails adds active and deactivated properties attributes when ACTIVE_INACTIVE is enabled`() {
            // TODO PDJB-2149: Remove this stub when the ACTIVE_INACTIVE feature flag is removed
            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
            val landlord = MockLandlordData.createIndividualLandlord()
            val deactivatedProperties = listOf(DeactivatedPropertyLandlordViewModel("6 Mythical Place", "P-XXXX-XXXX"))
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(
                propertyOwnershipService.getActivePropertiesForLandlordUser(
                    landlord,
                    currentUrlFragment = ACTIVE_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())
            whenever(propertyOwnershipService.getDeactivatedPropertiesForLandlordUser(landlord))
                .thenReturn(deactivatedProperties)

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                model {
                    attribute("isActiveInactiveEnabled", true)
                    attribute("activePropertiesTabId", ACTIVE_PROPERTIES_FRAGMENT)
                    attribute("deactivatedPropertiesTabId", DEACTIVATED_PROPERTIES_FRAGMENT)
                    attribute("deactivatedPropertiesList", deactivatedProperties)
                    attribute("activePropertiesList", emptyList<Any>())
                }
            }
            verify(backLinkStorageService).storeCurrentUrlReturningKey(ACTIVE_PROPERTIES_FRAGMENT)
        }

        // TODO PDJB-2149: Delete this test when the ACTIVE_INACTIVE feature flag is removed
        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getUserLandlordDetails keeps the registered properties attributes when ACTIVE_INACTIVE is disabled`() {
            whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)
            val landlord = MockLandlordData.createIndividualLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(
                propertyOwnershipService.getActivePropertiesForLandlordUser(
                    landlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                model {
                    attribute("isActiveInactiveEnabled", false)
                    attribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)
                    attributeDoesNotExist("deactivatedPropertiesList", "activePropertiesTabId", "deactivatedPropertiesTabId")
                }
            }
            verify(propertyOwnershipService, never()).getDeactivatedPropertiesForLandlordUser(any())
            verify(backLinkStorageService).storeCurrentUrlReturningKey(REGISTERED_PROPERTIES_FRAGMENT)
        }

        // TODO PDJB-2149: Update to stub ACTIVE_PROPERTIES_FRAGMENT and assert activePropertiesTabId when the
        //  ACTIVE_INACTIVE feature flag is removed
        @Test
        @WithMockUser(roles = ["INDIVIDUAL_LANDLORD"])
        fun `getUserLandlordDetails returns the org details view with shell attributes for an organisation landlord`() {
            val orgLandlord = MockLandlordData.createOrgLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(orgLandlord)
            whenever(
                propertyOwnershipService.getActivePropertiesForLandlordUser(
                    orgLandlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("orgLandlordDetailsView") }
                model {
                    attribute(
                        "deleteLandlordRecordUrl",
                        DeregisterOrganisationalLandlordController.ORGANISATIONAL_LANDLORD_DEREGISTRATION_PATH,
                    )
                    attribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)
                    attribute("isLandlordView", true)
                    attributeExists(
                        "orgLandlord",
                        "orgLandlordContacts",
                        "activePropertiesList",
                        "registerPropertyUrl",
                        "backUrl",
                        "showDeleteLandlordRecordLink",
                    )
                }
            }
        }

        // TODO PDJB-2149: Update to stub ACTIVE_PROPERTIES_FRAGMENT when the ACTIVE_INACTIVE feature flag is removed
        @Test
        @WithMockUser(roles = ["ORG_ADMIN"])
        fun `getUserLandlordDetails shows change links and the delete link for an org admin`() {
            val orgLandlord = MockLandlordData.createOrgLandlord(registrantRole = OrganisationalLandlordUserRole.ADMIN)
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(orgLandlord)
            whenever(
                propertyOwnershipService.getActivePropertiesForLandlordUser(
                    orgLandlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())
            whenever(organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions()).thenReturn(true)

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("orgLandlordDetailsView") }
                model {
                    attribute("showDeleteLandlordRecordLink", true)
                    attribute(
                        "orgLandlord",
                        Matchers.hasProperty<OrgLandlordViewModel>(
                            "organisationDetails",
                            Matchers.hasItem(
                                Matchers.hasProperty<SummaryListRowViewModel>("hasActions", Matchers.`is`(true)),
                            ),
                        ),
                    )
                    attribute(
                        "orgLandlordContacts",
                        Matchers.hasProperty<OrganisationalLandlordContactsViewModel>(
                            "mainContactCard",
                            Matchers.hasProperty<SummaryCardViewModel>("actions", Matchers.not(Matchers.empty<Any>())),
                        ),
                    )
                }
            }
        }

        // TODO PDJB-2149: Update to stub ACTIVE_PROPERTIES_FRAGMENT when the ACTIVE_INACTIVE feature flag is removed
        @Test
        @WithMockUser(roles = ["ORG_EDITOR"])
        fun `getUserLandlordDetails hides change links and the delete link for an org editor`() {
            val orgLandlord = MockLandlordData.createOrgLandlord(registrantRole = OrganisationalLandlordUserRole.EDITOR)
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(orgLandlord)
            whenever(
                propertyOwnershipService.getActivePropertiesForLandlordUser(
                    orgLandlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())
            whenever(organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions()).thenReturn(false)

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("orgLandlordDetailsView") }
                model {
                    attribute("showDeleteLandlordRecordLink", false)
                    attribute(
                        "orgLandlord",
                        Matchers.hasProperty<OrgLandlordViewModel>(
                            "organisationDetails",
                            Matchers.everyItem(
                                Matchers.hasProperty<SummaryListRowViewModel>("hasActions", Matchers.`is`(false)),
                            ),
                        ),
                    )
                    attribute(
                        "orgLandlordContacts",
                        Matchers.hasProperty<OrganisationalLandlordContactsViewModel>(
                            "mainContactCard",
                            Matchers.hasProperty<SummaryCardViewModel>("actions", Matchers.empty<Any>()),
                        ),
                    )
                    attribute(
                        "orgLandlordContacts",
                        Matchers.hasProperty<OrganisationalLandlordContactsViewModel>(
                            "showGoverningBodyMembersLink",
                            Matchers.`is`(false),
                        ),
                    )
                }
            }
        }
    }

    @Nested
    inner class GetLandlordDetailsAsLcUserTests {
        private val landlord = MockLandlordData.createIndividualLandlord()

        @BeforeEach
        fun setUp() {
            whenever(landlordService.retrieveLandlordById(landlord.id)).thenReturn(landlord)
            whenever(
                propertyOwnershipService.getRegisteredPropertiesForLandlord(
                    landlord.id,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())
        }

        @Test
        fun `getLandlordDetails returns a redirect for an unauthenticated user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { is3xxRedirection() }
            }
        }

        @Test
        @WithMockUser
        fun `getLandlordDetails returns 403 for an unauthorized user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getLandlordDetails returns 200 for a valid request from an LC user`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { isOk() }
                view { name("localCouncilLandlordDetailsView") }
                model { attributeExists("landlord") }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getLandlordDetails returns 404 when the landlord does not exist`() {
            val unknownLandlordId = landlord.id + 1
            whenever(landlordService.retrieveLandlordById(unknownLandlordId)).thenReturn(null)

            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(unknownLandlordId)).andExpect {
                status { isNotFound() }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getLandlordDetails returns 200 for a valid request from an LC admin`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id)).andExpect {
                status { isOk() }
                model { attributeExists("landlord") }
            }
        }
    }

    @Nested
    inner class GetOrgLandlordDetailsAsLcUserTests {
        private val orgLandlord = MockLandlordData.createOrgLandlord()

        @BeforeEach
        fun setUp() {
            whenever(landlordService.retrieveLandlordById(orgLandlord.id)).thenReturn(orgLandlord)
            whenever(
                propertyOwnershipService.getRegisteredPropertiesForLandlord(
                    orgLandlord.id,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_USER"])
        fun `getLandlordDetails returns the LC org details view for an organisation landlord`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(orgLandlord.id)).andExpect {
                status { isOk() }
                view { name("orgLandlordDetailsView") }
                model {
                    attribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)
                    attribute("isLandlordView", false)
                    attribute("showDeleteLandlordRecordLink", false)
                    attributeExists("orgLandlord", "orgLandlordContacts", "registeredPropertiesList", "backUrl")
                    attributeDoesNotExist("deleteLandlordRecordUrl", "lastModifiedDate")
                }
            }
        }

        @Test
        @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
        fun `getLandlordDetails returns the LC org details view for an LC admin`() {
            mvc.get(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(orgLandlord.id)).andExpect {
                status { isOk() }
                view { name("orgLandlordDetailsView") }
            }
        }
    }
}
