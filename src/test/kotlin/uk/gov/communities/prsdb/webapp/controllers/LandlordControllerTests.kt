package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_MAY2026_REDESIGN
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionViewModelBuilderOld
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord

@WebMvcTest(LandlordController::class)
class LandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var localCouncilService: LocalCouncilService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc
            .get("/$LANDLORD_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @WithMockUser
    @Test
    fun `index returns 403 for unauthorized user`() {
        mvc
            .get("/$LANDLORD_PATH_SEGMENT")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `index returns a redirect for authorised user`() {
        mvc
            .get("/$LANDLORD_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    fun `landlordDashboard returns a redirect for unauthenticated user`() {
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `landlordDashboard returns 403 for unauthorized user`() {
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `landlordDashboard returns 200 for authorised landlord user`() {
        val landlord = createLandlord()
        whenever(landlordService.retrieveLandlordByBaseUserId(anyString())).thenReturn(landlord)
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `landlordDashboard sets privacyNoticeUrl with a backUrl query param so the privacy page renders a back link`() {
        val landlord = createLandlord()
        whenever(landlordService.retrieveLandlordByBaseUserId(anyString())).thenReturn(landlord)
        whenever(backLinkStorageService.storeCurrentUrlReturningKey()).thenReturn(7)
        mvc
            .get(LANDLORD_DASHBOARD_URL)
            .andExpect {
                status { isOk() }
                model { attribute("privacyNoticeUrl", "/landlord/privacy-notice?withBackUrl=7") }
            }
    }

    @Test
    fun `getComplianceActions returns a redirect for unauthenticated user`() {
        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getComplianceActions returns 403 for unauthorized user`() {
        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `getComplianceActions returns 200 for authorised landlord user`() {
        val nonCompliantDataModel =
            ComplianceStatusDataModel(
                2,
                "456 Example Avenue, EX",
                "P-YYYY-YYYY",
                ComplianceCertStatus.EXPIRED,
                ComplianceCertStatus.ADDED,
                ComplianceCertStatus.HAS_FAULTS,
                ComplianceCertStatus.HAS_FAULTS,
                false,
                true,
            )
        whenever(propertyComplianceService.getOldNonCompliantPropertiesForLandlord("user")).thenReturn(listOf(nonCompliantDataModel))

        // Act and Assert
        val expectedComplianceActions =
            listOf(
                ComplianceActionViewModelBuilderOld.fromDataModel(nonCompliantDataModel),
            )

        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isOk() }
                model {
                    attribute("complianceActions", expectedComplianceActions)
                    attribute(
                        "viewRegisteredPropertiesUrl",
                        "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_FRAGMENT",
                    )
                    attribute("backUrl", LANDLORD_DASHBOARD_URL)
                }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `getComplianceActions returns complianceActions view when redesign feature flag is enabled`() {
        whenever(propertyComplianceService.getMay2026RedesignNonCompliantPropertiesForLandlord(eq("user"), any())).thenReturn(
            PageImpl(emptyList()),
        )
        whenever(featureFlagManager.checkFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)).thenReturn(true)

        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isOk() }
                view { name("complianceActionsMay26Redesign") }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `getComplianceActions returns complianceActionsOld view when redesign feature flag is disabled`() {
        whenever(propertyComplianceService.getOldNonCompliantPropertiesForLandlord("user")).thenReturn(emptyList())
        whenever(featureFlagManager.checkFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)).thenReturn(false)

        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isOk() }
                view { name("complianceActionsOld") }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `getComplianceActions redirects to first page when requested page exceeds total pages`() {
        whenever(featureFlagManager.checkFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)).thenReturn(true)
        whenever(propertyComplianceService.getMay2026RedesignNonCompliantPropertiesForLandlord(eq("user"), any())).thenReturn(
            PageImpl(emptyList(), PageRequest.of(5, 10), 10),
        )

        mvc
            .get("$COMPLIANCE_ACTIONS_URL?page=6")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `getComplianceActions includes paginationViewModel when redesign feature flag is enabled`() {
        whenever(featureFlagManager.checkFeature(COMPLIANCE_ACTIONS_MAY2026_REDESIGN)).thenReturn(true)
        whenever(propertyComplianceService.getMay2026RedesignNonCompliantPropertiesForLandlord(eq("user"), any())).thenReturn(
            PageImpl(emptyList(), PageRequest.of(0, 10), 20),
        )

        mvc
            .get(COMPLIANCE_ACTIONS_URL)
            .andExpect {
                status { isOk() }
                model { attributeExists("paginationViewModel") }
            }
    }
}
