package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.getPropertyDeregistrationPath
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import kotlin.test.assertEquals

@WebMvcTest(DeregisterPropertyController::class)
class DeregisterPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    private lateinit var propertyDeregistrationJourney: PropertyDeregistrationJourney

    @MockBean
    private lateinit var propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory

    @MockBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `getJourneyStep for the initial step returns a redirect for an unauthenticated user`() {
        mvc
            .get("/deregister-property/1/are-you-sure")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep for the initial step returns 403 for a user who is not a landlord`() {
        mvc
            .get("/deregister-property/1/are-you-sure")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep for the initial step returns 403 for a landlord user who does not own this property`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()
        whenever(propertyDeregistrationJourney.initialStepId)
            .thenReturn(DeregisterPropertyStepId.AreYouSure)
        whenever(propertyDeregistrationJourneyFactory.create(propertyOwnershipId))
            .thenReturn(propertyDeregistrationJourney)
        whenever(propertyOwnershipService.getIsAuthorizedToDeleteRecord(eq(propertyOwnershipId), anyString()))
            .thenReturn(false)

        // Act, Assert
        mvc
            .get("/deregister-property/$propertyOwnershipId/are-you-sure")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep for the initial step returns 200 for the landlord who owns this property`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()
        whenever(propertyDeregistrationJourney.initialStepId)
            .thenReturn(DeregisterPropertyStepId.AreYouSure)
        whenever(propertyDeregistrationJourneyFactory.create(propertyOwnershipId))
            .thenReturn(propertyDeregistrationJourney)
        whenever(propertyOwnershipService.getIsAuthorizedToDeleteRecord(eq(propertyOwnershipId), anyString()))
            .thenReturn(true)

        // Act, Assert
        mvc
            .get("/deregister-property/$propertyOwnershipId/are-you-sure")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `getPropertyDegistrationPath returns a path to the initial step of the delete journey for this property`() {
        // Arrange

        val propertyOwnershipId = 1.toLong()
        whenever(propertyDeregistrationJourney.initialStepId)
            .thenReturn(DeregisterPropertyStepId.AreYouSure)
        whenever(propertyDeregistrationJourneyFactory.create(propertyOwnershipId))
            .thenReturn(propertyDeregistrationJourney)

        // Act
        val propertyDeregistrationPath = getPropertyDeregistrationPath(propertyOwnershipId, propertyDeregistrationJourneyFactory)

        // Assert
        assertEquals(
            "/$DEREGISTER_PROPERTY_JOURNEY_URL/$propertyOwnershipId/${DeregisterPropertyStepId.AreYouSure.urlPathSegment}",
            propertyDeregistrationPath,
        )
    }
}
