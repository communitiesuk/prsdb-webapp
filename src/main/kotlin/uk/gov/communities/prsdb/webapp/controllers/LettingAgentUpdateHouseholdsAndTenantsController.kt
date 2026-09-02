package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController.Companion.LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE)
class LettingAgentUpdateHouseholdsAndTenantsController(
    private val journeyFactory: UpdateHouseholdsAndTenantsJourneyFactory,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @GetMapping("/{*stepPath}")
    fun getUpdateStep(
        principal: Principal?,
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(token, stepPath, principal) { getStepModelAndView() }

    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @PostMapping("/{*stepPath}")
    fun postUpdateStep(
        model: Model,
        principal: Principal?,
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(token, stepPath, principal) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        token: UUID,
        stepPath: String,
        principal: Principal?,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView {
        val propertyOwnershipId = resolveOccupiedPropertyOwnershipId(token)
        val propertyDetailsUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        return JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(propertyOwnershipId, propertyDetailsUrl) },
            initialiseJourney = { journeyFactory.initializeJourneyState(propertyOwnershipId, principal) },
            dispatch = dispatch,
        )
    }

    private fun resolveOccupiedPropertyOwnershipId(token: UUID): Long {
        val lettingAgentAccess =
            lettingAgentAccessService.getInvitationByTokenOrNull(token)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No letting agent access found for token $token")

        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(lettingAgentAccess.propertyOwnership.id)

        if (!propertyOwnership.isOccupied) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership ${propertyOwnership.id} is not occupied so cannot be updated by a letting agent",
            )
        }

        return propertyOwnership.id
    }

    companion object {
        const val LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}/update-households-and-tenants"

        fun getBaseRoute(token: UUID): String = UriTemplate(LETTING_AGENT_UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE).expand(token).toASCIIString()

        fun getRoute(
            token: UUID,
            stepSegment: String,
        ): String = "${getBaseRoute(token)}/$stepSegment"
    }
}
