package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@WebMvcTest(RegisterPropertyController::class)
class RegisterPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    lateinit var propertyRegistrationJourney: PropertyRegistrationJourney

    @MockBean
    lateinit var propertyOwnershipService: PropertyOwnershipService

    @BeforeEach
    fun setupMocks() {
        whenever(propertyRegistrationJourney.initialStepId).thenReturn(RegisterPropertyStepId.PlaceholderPage)
    }

    @Test
    fun `index returns a redirect for unauthenticated user`() {
        mvc.get("/register-property").andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `index returns 403 for an unauthorised user`() {
        mvc
            .get("/register-property")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `index returns 200 for a landlord user`() {
        mvc
            .get("/register-property")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if a property has been registered`() {
        val propertyOwnershipID = 0L
        val propertyOwnership = createPropertyOwnership()

        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyOwnershipID)).thenReturn(
            propertyOwnership,
        )

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/$REGISTER_PROPERTY_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
                    .sessionAttr(PROPERTY_OWNERSHIP_ID, propertyOwnershipID),
            ).andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 400 if there's no property ownership ID in session`() {
        val propertyOwnershipID = 0L
        val propertyOwnership = createPropertyOwnership()

        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyOwnershipID)).thenReturn(
            propertyOwnership,
        )

        mvc
            .get("/$REGISTER_PROPERTY_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 400 if the property ownership ID in session is not valid`() {
        val propertyOwnershipID = 0L

        whenever(propertyOwnershipService.retrievePropertyOwnership(propertyOwnershipID)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/$REGISTER_PROPERTY_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
                    .sessionAttr(PROPERTY_OWNERSHIP_ID, propertyOwnershipID),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
