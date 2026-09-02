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
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController.Companion.LETTING_AGENT_UPDATE_TENANCY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.tenancyDetails.UpdateTenancyDetailsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_UPDATE_TENANCY_DETAILS_ROUTE)
class LettingAgentUpdateTenancyDetailsController(
    private val journeyFactory: UpdateTenancyDetailsJourneyFactory,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("{stepName}")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun getUpdateStep(
        principal: Principal?,
        @PathVariable token: UUID,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView {
        val propertyOwnershipId = resolveOccupiedPropertyOwnershipId(token)
        val propertyDetailsUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        return try {
            val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId, propertyDetailsUrl)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    @PostMapping("{stepName}")
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    fun postUpdateStep(
        model: Model,
        principal: Principal?,
        @PathVariable token: UUID,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        val propertyOwnershipId = resolveOccupiedPropertyOwnershipId(token)
        val propertyDetailsUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        return try {
            val journeyMap = journeyFactory.createJourneySteps(propertyOwnershipId, propertyDetailsUrl)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(propertyOwnershipId, principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
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
        const val LETTING_AGENT_UPDATE_TENANCY_DETAILS_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}/update-tenancy-details"

        fun getBaseRoute(token: UUID): String = UriTemplate(LETTING_AGENT_UPDATE_TENANCY_DETAILS_ROUTE).expand(token).toASCIIString()

        fun getRoute(
            token: UUID,
            stepSegment: String,
        ): String = "${getBaseRoute(token)}/$stepSegment"
    }
}
