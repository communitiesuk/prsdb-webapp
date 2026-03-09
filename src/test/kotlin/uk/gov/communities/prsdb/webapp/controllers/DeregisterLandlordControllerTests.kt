package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@WebMvcTest(DeregisterLandlordController::class)
class DeregisterLandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory

    @MockitoBean
    private lateinit var landlordService: LandlordService

    @MockitoBean
    private lateinit var landlordDeregistrationJourney: LandlordDeregistrationJourney

    @MockitoBean
    private lateinit var landlordDeregistrationService: LandlordDeregistrationService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `checkForRegisteredProperties returns a redirect for an unauthenticated user`() {
        mvc
            .get(DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `checkForRegisteredProperties returns 403 for a user who is not a landlord`() {
        mvc
            .get(DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if the current user is not in the landlord database`() {
        whenever(landlordDeregistrationService.getLandlordHadActivePropertiesFromSession()).thenReturn(false)

        mvc
            .get("${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isOk() } }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 500 if the current user is still in the landlord database`() {
        whenever(landlordService.retrieveLandlordByBaseUserId("user"))
            .thenReturn(MockLandlordData.createLandlord())

        mvc
            .get("${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { is5xxServerError() } }
    }
}
