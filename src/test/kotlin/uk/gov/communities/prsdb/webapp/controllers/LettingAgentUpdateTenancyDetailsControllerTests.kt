package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.ServletException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@WebMvcTest(LettingAgentUpdateTenancyDetailsController::class)
class LettingAgentUpdateTenancyDetailsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateTenancyDetailsJourneyFactory

    @MockitoBean
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @Test
    fun `getUpdateStep dispatches the journey step for a valid token`() {
        val token = UUID.randomUUID()
        val propertyOwnership = createOccupiedPropertyOwnership()
        stubValidTokenJourney(token, propertyOwnership)
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get(LettingAgentUpdateTenancyDetailsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT))
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `postUpdateStep dispatches the journey step for a valid token`() {
        val token = UUID.randomUUID()
        val propertyOwnership = createOccupiedPropertyOwnership()
        stubValidTokenJourney(token, propertyOwnership)
        val redirectUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(LettingAgentUpdateTenancyDetailsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT)) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "numberOfHouseholds=2"
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
    }

    @Test
    fun `getUpdateStep returns not found when the token is not recognised`() {
        val token = UUID.randomUUID()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token))).thenReturn(null)

        mvc
            .get(LettingAgentUpdateTenancyDetailsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT))
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `getUpdateStep returns not found when the property is not occupied`() {
        val token = UUID.randomUUID()
        val propertyOwnership = createUnoccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        whenever(propertyOwnershipService.getPropertyOwnership(eq(propertyOwnership.id)))
            .thenReturn(propertyOwnership)

        mvc
            .get(LettingAgentUpdateTenancyDetailsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT))
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `getUpdateStep surfaces an error when the journey state propertyId does not match the token's property`() {
        val token = UUID.randomUUID()
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        whenever(propertyOwnershipService.getPropertyOwnership(eq(propertyOwnership.id)))
            .thenReturn(propertyOwnership)
        val propertyDetailsUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        doThrow(PrsdbWebException("Journey state propertyId does not match provided propertyId"))
            .whenever(journeyFactory)
            .createJourneySteps(eq(propertyOwnership.id), eq(propertyDetailsUrl))

        assertThrows<ServletException> {
            mvc.get(LettingAgentUpdateTenancyDetailsController.getRoute(token, HouseholdStep.ROUTE_SEGMENT))
        }
    }

    private fun stubValidTokenJourney(
        token: UUID,
        propertyOwnership: PropertyOwnership,
    ) {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        whenever(propertyOwnershipService.getPropertyOwnership(eq(propertyOwnership.id)))
            .thenReturn(propertyOwnership)
        val propertyDetailsUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnership.id), eq(propertyDetailsUrl)))
            .thenReturn(mapOf(HouseholdStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
