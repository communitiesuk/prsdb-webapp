package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.SWITCHED_TO_INDIVIDUAL_PROPERTY_ID
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController.Companion.getSwitchToIndividualBasePath
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController.Companion.getSwitchToIndividualFirstStepPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.SwitchToIndividualCheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@WebMvcTest(SwitchToIndividualController::class)
class SwitchToIndividualControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var switchToIndividualJourneyFactory: SwitchToIndividualJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `returns a redirect for an unauthenticated user`() {
            mvc
                .get(getSwitchToIndividualFirstStepPath(1))
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser
        fun `returns 403 for a user who is not a landlord`() {
            mvc
                .get(getSwitchToIndividualFirstStepPath(1))
                .andExpect {
                    status { isForbidden() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `returns 404 for a landlord who does not own this property`() {
            val propertyOwnershipId = 1L
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(false)

            mvc
                .get(getSwitchToIndividualFirstStepPath(propertyOwnershipId))
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `returns 200 for the landlord who owns this property`() {
            val propertyOwnershipId = 1L
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
            whenever(switchToIndividualJourneyFactory.createJourneySteps(propertyOwnershipId))
                .thenReturn(mapOf(SwitchToIndividualCheckPendingInvitationsStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView())
                .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

            mvc
                .get(getSwitchToIndividualFirstStepPath(propertyOwnershipId))
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `redirects to initialize journey when no journey state exists`() {
            val propertyOwnershipId = 1L
            val journeyId = "test-journey-id"
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
            whenever(switchToIndividualJourneyFactory.createJourneySteps(propertyOwnershipId))
                .thenThrow(NoSuchJourneyException())
            whenever(switchToIndividualJourneyFactory.initializeJourneyState(propertyOwnershipId))
                .thenReturn(journeyId)

            mvc
                .get(getSwitchToIndividualFirstStepPath(propertyOwnershipId))
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `redirects to initialize journey when property ownership mismatch`() {
            val propertyOwnershipId = 1L
            val journeyId = "test-journey-id"
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
            whenever(switchToIndividualJourneyFactory.createJourneySteps(propertyOwnershipId))
                .thenThrow(PropertyOwnershipMismatchException("mismatch"))
            whenever(switchToIndividualJourneyFactory.initializeJourneyState(propertyOwnershipId))
                .thenReturn(journeyId)

            mvc
                .get(getSwitchToIndividualFirstStepPath(propertyOwnershipId))
                .andExpect {
                    status { is3xxRedirection() }
                }
        }
    }

    @Nested
    inner class GetSuccess {
        @Test
        fun `returns a redirect for an unauthenticated user`() {
            mvc
                .get("${getSwitchToIndividualBasePath(1)}/confirmation")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `returns 404 for a landlord who does not own this property`() {
            val propertyOwnershipId = 1L
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(false)

            mvc
                .get("${getSwitchToIndividualBasePath(propertyOwnershipId)}/confirmation")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `returns 404 when session property id does not match`() {
            val propertyOwnershipId = 1L
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)

            mvc
                .perform(
                    MockMvcRequestBuilders
                        .get("${getSwitchToIndividualBasePath(propertyOwnershipId)}/confirmation")
                        .sessionAttr(SWITCHED_TO_INDIVIDUAL_PROPERTY_ID, 999L),
                ).andExpect(MockMvcResultMatchers.status().isNotFound)
        }

        @Test
        @WithMockUser(roles = ["LANDLORD"])
        fun `returns 200 with address and property details url when session matches`() {
            val propertyOwnershipId = 1L
            val address = "123 Test Street, AB1 2CD"
            val propertyOwnership =
                MockLandlordData.createPropertyOwnership(
                    id = propertyOwnershipId,
                    address = MockLandlordData.createAddress(singleLineAddress = address),
                )
            whenever(propertyOwnershipService.getIsPrimaryLandlord(eq(propertyOwnershipId), anyString())).thenReturn(true)
            whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)

            mvc
                .perform(
                    MockMvcRequestBuilders
                        .get("${getSwitchToIndividualBasePath(propertyOwnershipId)}/confirmation")
                        .sessionAttr(SWITCHED_TO_INDIVIDUAL_PROPERTY_ID, propertyOwnershipId),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.model().attribute("address", address))
                .andExpect(
                    MockMvcResultMatchers
                        .model()
                        .attribute("propertyDetailsUrl", PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)),
                )
        }
    }
}
