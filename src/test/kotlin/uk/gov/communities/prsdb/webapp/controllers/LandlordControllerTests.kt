package uk.gov.communities.prsdb.webapp.controllers

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_COMPLIANCES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.IncompleteComplianceDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.IncompleteComplianceViewModelBuilder
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord

@WebMvcTest(LandlordController::class)
class LandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @MockitoBean
    private lateinit var journeyDataServiceFactory: JourneyDataServiceFactory

    @MockitoBean
    private lateinit var localAuthorityService: LocalAuthorityService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

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
    fun `landlordIncompleteProperties returns a redirect for unauthenticated user`() {
        mvc
            .get(INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `landlordIncompleteProperties returns 403 for unauthorized user`() {
        mvc
            .get(INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `landlordIncompleteProperties returns 200 for authorised landlord user`() {
        whenever(
            propertyRegistrationService.getIncompletePropertiesForLandlord(
                "user",
            ),
        ).thenReturn(emptyList())
        mvc
            .get(INCOMPLETE_PROPERTIES_URL)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `landlordIncompleteCompliances returns a redirect for unauthenticated user`() {
        mvc
            .get(INCOMPLETE_COMPLIANCES_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `landlordIncompleteCompliances returns 403 for unauthorized user`() {
        mvc
            .get(INCOMPLETE_COMPLIANCES_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], username = "user")
    fun `landlordIncompleteCompliances returns 200 for authorised landlord user`() {
        val incompleteComplianceDataModel =
            IncompleteComplianceDataModel(
                1,
                "123 Example Street, EX",
                "Example Local Authority",
                LocalDate(2025, 6, 7),
                true,
                true,
                true,
                false,
            )
        val incompleteCompliancesViewModel =
            listOf(
                IncompleteComplianceViewModelBuilder.fromDataModel(
                    0,
                    incompleteComplianceDataModel,
                    0,
                ),
            )

        whenever(
            propertyOwnershipService.getIncompleteCompliancesForLandlord("user"),
        ).thenReturn(listOf(incompleteComplianceDataModel))
        mvc
            .get(INCOMPLETE_COMPLIANCES_URL)
            .andExpect {
                status { isOk() }
                model {
                    attribute("incompleteCompliances", incompleteCompliancesViewModel)
                    attribute(
                        "viewRegisteredPropertiesUrl",
                        "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_PATH_SEGMENT",
                    )
                    attribute("backUrl", LANDLORD_DASHBOARD_URL)
                }
            }
    }
}
