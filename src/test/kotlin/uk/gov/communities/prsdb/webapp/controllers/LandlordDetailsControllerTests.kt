package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OrganisationGoverningBodyMemberService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.Test

@WebMvcTest(LandlordDetailsController::class)
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
    private lateinit var organisationGoverningBodyMemberService: OrganisationGoverningBodyMemberService

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

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUserLandlordDetails returns 200 for a valid request from a landlord`() {
            val landlord = MockLandlordData.createIndividualLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(
                propertyOwnershipService.getRegisteredPropertiesForLandlordUser(
                    landlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                model { attributeExists("landlord") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUserLandlordDetails returns the redesigned view when the org landlord flag is enabled`() {
            val landlord = MockLandlordData.createIndividualLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)).thenReturn(true)
            whenever(
                propertyOwnershipService.getRegisteredPropertiesForLandlordUser(
                    landlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("individualLandlordDetailsView") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUserLandlordDetails returns the old view when the org landlord flag is disabled`() {
            val landlord = MockLandlordData.createIndividualLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(landlord)
            whenever(featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)).thenReturn(false)
            whenever(
                propertyOwnershipService.getRegisteredPropertiesForLandlordUser(
                    landlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("individualLandlordDetailsViewBeforePdjb1492") }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUserLandlordDetails returns the org details view with shell attributes for an organisation landlord`() {
            val orgLandlord = MockLandlordData.createOrgLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(orgLandlord)
            whenever(featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)).thenReturn(true)
            whenever(organisationGoverningBodyMemberService.getGoverningBodyMembers(orgLandlord)).thenReturn(emptyList())
            whenever(
                propertyOwnershipService.getRegisteredPropertiesForLandlordUser(
                    orgLandlord,
                    currentUrlFragment = REGISTERED_PROPERTIES_FRAGMENT,
                ),
            ).thenReturn(emptyList())

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isOk() }
                view { name("orgLandlordDetailsView") }
                model {
                    attribute("deleteLandlordRecordUrl", DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
                    attribute("registeredPropertiesTabId", REGISTERED_PROPERTIES_FRAGMENT)
                    attributeExists(
                        "orgLandlord",
                        "governingBodyMembers",
                        "registeredPropertiesList",
                        "registerPropertyUrl",
                        "backUrl",
                    )
                }
            }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `getUserLandlordDetails returns 404 for an organisation landlord when the org landlord flag is disabled`() {
            val orgLandlord = MockLandlordData.createOrgLandlord()
            whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(orgLandlord)
            whenever(featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)).thenReturn(false)

            mvc.get(LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE).andExpect {
                status { isNotFound() }
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
                model { attributeExists("landlord") }
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
}
