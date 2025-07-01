package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.getPropertyDeregistrationPath
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@WebMvcTest(DeregisterPropertyController::class)
class DeregisterPropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var propertyDeregistrationJourneyFactory: PropertyDeregistrationJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyService: PropertyService

    @MockitoBean
    private lateinit var propertyDeregistrationService: PropertyDeregistrationService

    private val initialStepIdUrlSegment = PropertyDeregistrationJourney.initialStepId.urlPathSegment

    @Test
    fun `getJourneyStep for the initial step returns a redirect for an unauthenticated user`() {
        mvc
            .get(DeregisterPropertyController.getPropertyDeregistrationPath(1))
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep for the initial step returns 403 for a user who is not a landlord`() {
        mvc
            .get(DeregisterPropertyController.getPropertyDeregistrationPath(1))
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep for the initial step returns 404 for a landlord user who does not own this property`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()

        whenever(propertyDeregistrationJourneyFactory.create(propertyOwnershipId))
            .thenReturn(mock())
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(false)

        // Act, Assert
        mvc
            .get(DeregisterPropertyController.getPropertyDeregistrationPath(1))
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getJourneyStep for the initial step returns 200 for the landlord who owns this property`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()

        whenever(propertyDeregistrationJourneyFactory.create(propertyOwnershipId))
            .thenReturn(mock())
        whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)

        // Act, Assert
        mvc
            .get(DeregisterPropertyController.getPropertyDeregistrationPath(1))
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `getPropertyDegistrationPath returns a path to the initial step of the delete journey for this property`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()

        // Act
        val propertyDeregistrationPath = getPropertyDeregistrationPath(propertyOwnershipId)

        // Assert
        assertEquals(
            DeregisterPropertyController.getPropertyDeregistrationPath(1),
            propertyDeregistrationPath,
        )
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if the property ownership is not in the database`() {
        val propertyOwnershipId = 1.toLong()
        val propertyId = 2.toLong()
        whenever(
            propertyDeregistrationService.getDeregisteredPropertyAndOwnershipIdsFromSession(),
        ).thenReturn(mutableListOf(Pair(propertyOwnershipId, propertyId)))

        mvc
            .get("${DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }

        verify(propertyOwnershipService).retrievePropertyOwnershipById(propertyOwnershipId)
        verify(propertyService).retrievePropertyById(propertyId)
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if no deregistered Pair(propertyOwnershipId, propertyId) are in the session`() {
        val propertyOwnershipId = 1.toLong()

        mvc
            .get("${DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if the propertyOwnershipId is not in the list of deregistered propertyOwnershipIds in the session`() {
        val deregisteredPropertyEntities = mutableListOf(Pair(2.toLong(), 3.toLong()))
        val propertyOwnershipId = 1.toLong()
        whenever(propertyDeregistrationService.getDeregisteredPropertyAndOwnershipIdsFromSession())
            .thenReturn(deregisteredPropertyEntities)

        mvc
            .get("${DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 500 if the property ownership is found in the database`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id
        whenever(
            propertyDeregistrationService.getDeregisteredPropertyAndOwnershipIdsFromSession(),
        ).thenReturn(mutableListOf(Pair(propertyOwnershipId, propertyOwnership.property.id)))
        whenever(propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act, Assert
        mvc
            .get("${DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { is5xxServerError() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 500 if the property is found in the database`() {
        // Arrange
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val propertyOwnershipId = propertyOwnership.id
        val property = propertyOwnership.property

        whenever(
            propertyDeregistrationService.getDeregisteredPropertyAndOwnershipIdsFromSession(),
        ).thenReturn(mutableListOf(Pair(propertyOwnershipId, propertyOwnership.property.id)))
        whenever(propertyService.retrievePropertyById(property.id)).thenReturn(property)

        // Act, Assert
        mvc
            .get("${DeregisterPropertyController.getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { is5xxServerError() }
            }
    }
}
