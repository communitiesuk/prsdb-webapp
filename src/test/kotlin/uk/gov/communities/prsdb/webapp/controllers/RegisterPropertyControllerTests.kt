package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_NUMBER
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationConfirmationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

@WebMvcTest(RegisterPropertyController::class)
class RegisterPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyConfirmationService: PropertyRegistrationConfirmationService

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc.get(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `index returns 403 for an unauthorised user`() {
        mvc
            .get(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `index returns 200 for a landlord user`() {
        mvc
            .get(RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if a property has been registered`() {
        val propertyRegistrationNumber = 0L
        val propertyOwnership =
            createPropertyOwnership(
                registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, propertyRegistrationNumber),
            )

        whenever(propertyConfirmationService.getLastPrnRegisteredThisSession()).thenReturn(propertyRegistrationNumber)
        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)).thenReturn(propertyOwnership)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(PROPERTY_REGISTRATION_NUMBER, propertyRegistrationNumber),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("registerPropertyConfirmation"))
            .andExpect(MockMvcResultMatchers.model().attribute("singleLineAddress", propertyOwnership.address.singleLineAddress))
            .andExpect(
                MockMvcResultMatchers.model().attribute(
                    "propertyComplianceUrl",
                    PropertyComplianceController.getPropertyCompliancePath(propertyOwnership.id),
                ),
            ).andExpect(MockMvcResultMatchers.model().attribute("landlordDashboardUrl", LandlordController.LANDLORD_DASHBOARD_URL))
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 400 if there's no property ownership ID in session`() {
        whenever(propertyConfirmationService.getLastPrnRegisteredThisSession()).thenReturn(null)

        mvc
            .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 400 if the property ownership ID in session is not valid`() {
        val propertyRegistrationNumber = 0L

        whenever(propertyConfirmationService.getLastPrnRegisteredThisSession()).thenReturn(propertyRegistrationNumber)
        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(PROPERTY_REGISTRATION_NUMBER, propertyRegistrationNumber),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getResume redirects to task-list with the supplied journey id`() {
        val journeyId = "journey-123"

        mvc
            .get("${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$RESUME_PAGE_PATH_SEGMENT?contextId=$journeyId")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl("$TASK_LIST_PATH_SEGMENT?journeyId=$journeyId")
            }
    }
}
