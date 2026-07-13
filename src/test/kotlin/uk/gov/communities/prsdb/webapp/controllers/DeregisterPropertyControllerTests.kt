package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.getPropertyDeregistrationBasePath
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController.Companion.getPropertyDeregistrationPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.CheckCanDeregisterStep
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
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
    private lateinit var propertyDeregistrationService: PropertyDeregistrationService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @Test
    fun `getPropertyDeregistrationPath returns a path to the initial deregister step`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()

        // Act
        val propertyDeregistrationPath = getPropertyDeregistrationPath(propertyOwnershipId)

        // Assert
        assertEquals(
            "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_PROPERTY_JOURNEY_URL/$propertyOwnershipId/${CheckCanDeregisterStep.ROUTE_SEGMENT}",
            propertyDeregistrationPath,
        )
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep for the initial step returns 200 for the landlord who owns this property`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()

        whenever(propertyOwnershipService.getIsLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
        whenever(
            propertyDeregistrationJourneyFactory.createJourneySteps(propertyOwnershipId),
        ).thenReturn(mapOf(CheckCanDeregisterStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        // Act, Assert
        mvc
            .get(getPropertyDeregistrationPath(1))
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()
        val journeyId = "test-journey-id"

        whenever(propertyOwnershipService.getIsLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
        whenever(propertyDeregistrationJourneyFactory.createJourneySteps(propertyOwnershipId))
            .thenThrow(NoSuchJourneyException())
        whenever(propertyDeregistrationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        // Act, Assert
        mvc
            .get(getPropertyDeregistrationPath(1))
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(CheckCanDeregisterStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when property ownership does not match`() {
        // Arrange
        val propertyOwnershipId = 1.toLong()
        val journeyId = "test-journey-id"

        whenever(propertyOwnershipService.getIsLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
        whenever(propertyDeregistrationJourneyFactory.createJourneySteps(propertyOwnershipId))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(propertyDeregistrationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        // Act, Assert
        mvc
            .get(getPropertyDeregistrationPath(1))
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(CheckCanDeregisterStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if the property ownership was deregistered in the session`() {
        val propertyOwnershipId = 1.toLong()
        whenever(
            propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession(),
        ).thenReturn(mutableListOf(propertyOwnershipId))
        whenever(propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(null)

        mvc
            .get("${getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if no deregistered property ownerships are in the session`() {
        val propertyOwnershipId = 1.toLong()

        mvc
            .get("${getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if the propertyOwnershipId is not in the list of deregistered propertyOwnershipIds in the session`() {
        val propertyOwnershipId = 1.toLong()
        whenever(propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession())
            .thenReturn(mutableListOf(2, 3))

        mvc
            .get("${getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
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
            propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession(),
        ).thenReturn(mutableListOf(propertyOwnershipId))
        whenever(propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(propertyOwnership)

        // Act, Assert
        mvc
            .get("${getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { is5xxServerError() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns the confirmation view with the address`() {
        val propertyOwnershipId = 1.toLong()
        whenever(
            propertyDeregistrationService.getDeregisteredPropertyOwnershipIdsFromSession(),
        ).thenReturn(mutableListOf(propertyOwnershipId))
        whenever(propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)).thenReturn(null)
        whenever(propertyDeregistrationService.getDeregisteredPropertyAddress(propertyOwnershipId))
            .thenReturn("1, Example Road, EG")

        mvc
            .get("${getPropertyDeregistrationBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
                view { name("deregisterPropertyConfirmation") }
                model { attribute("address", "1, Example Road, EG") }
            }
    }
}
