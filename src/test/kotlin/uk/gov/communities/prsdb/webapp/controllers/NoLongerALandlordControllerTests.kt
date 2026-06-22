package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.NO_LONGER_A_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.NoLongerALandlordController.Companion.getNoLongerALandlordBasePath
import uk.gov.communities.prsdb.webapp.controllers.NoLongerALandlordController.Companion.getNoLongerALandlordPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.NoLongerALandlordJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.stepConfig.ConfirmStep
import uk.gov.communities.prsdb.webapp.services.NoLongerALandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@WebMvcTest(NoLongerALandlordController::class)
class NoLongerALandlordControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var noLongerALandlordJourneyFactory: NoLongerALandlordJourneyFactory

    @MockitoBean
    private lateinit var noLongerALandlordService: NoLongerALandlordService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val testPropertyOwnershipId = 1L

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc
            .get(getNoLongerALandlordPath(testPropertyOwnershipId))
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc
            .get(getNoLongerALandlordPath(testPropertyOwnershipId))
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for a landlord who cannot leave this property`() {
        whenever(
            noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "not eligible"))

        mvc
            .get(getNoLongerALandlordPath(testPropertyOwnershipId))
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord who can leave this property`() {
        whenever(
            noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(
            noLongerALandlordJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"),
        ).thenReturn(mapOf(ConfirmStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get(getNoLongerALandlordPath(testPropertyOwnershipId))
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        whenever(
            noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(
            noLongerALandlordJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"),
        ).thenReturn(mapOf(ConfirmStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .get("${getNoLongerALandlordBasePath(testPropertyOwnershipId)}/unknown-step")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(
            noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(noLongerALandlordJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"))
            .thenThrow(NoSuchJourneyException())
        whenever(noLongerALandlordJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .get(getNoLongerALandlordPath(testPropertyOwnershipId))
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(ConfirmStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when property ownership does not match`() {
        val journeyId = "test-journey-id"

        whenever(
            noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(noLongerALandlordJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(noLongerALandlordJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .get(getNoLongerALandlordPath(testPropertyOwnershipId))
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(ConfirmStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if the property was left in the session`() {
        whenever(noLongerALandlordService.getLeftPropertyOwnershipsFromSession())
            .thenReturn(mutableMapOf(testPropertyOwnershipId to "1 Example Road, EG1 1AA"))

        mvc
            .get("${getNoLongerALandlordBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if no properties were left in the session`() {
        whenever(noLongerALandlordService.getLeftPropertyOwnershipsFromSession())
            .thenReturn(mutableMapOf())

        mvc
            .get("${getNoLongerALandlordBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if this propertyOwnershipId was not left in the session`() {
        whenever(noLongerALandlordService.getLeftPropertyOwnershipsFromSession())
            .thenReturn(mutableMapOf((2L to ""), (3L to "")))

        mvc
            .get("${getNoLongerALandlordBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `getNoLongerALandlordPath returns a path to the confirm step`() {
        assertEquals(
            "/$LANDLORD_PATH_SEGMENT/$NO_LONGER_A_LANDLORD_JOURNEY_URL/$testPropertyOwnershipId/${ConfirmStep.ROUTE_SEGMENT}",
            getNoLongerALandlordPath(testPropertyOwnershipId),
        )
    }
}
