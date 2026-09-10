package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

abstract class AbstractLettingAgentUpdateController(
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @GetMapping("/{*stepPath}")
    fun getUpdateStep(
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath, token) { getStepModelAndView() }

    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @PostMapping("/{*stepPath}")
    fun postUpdateStep(
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath, token) { postStepModelAndView(formData) }

    protected abstract fun createJourneySteps(
        propertyOwnershipId: Long,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator>

    protected abstract fun initialiseJourneyState(
        token: UUID,
        propertyOwnershipId: Long,
    ): String

    protected fun dispatchJourneyStep(
        stepPath: String,
        token: UUID,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView {
        val propertyOwnershipId =
            lettingAgentAccessService.getInvitationByTokenOrNull(token)?.propertyOwnership?.id
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No letting agent access found for token $token")

        propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)

        val returnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)

        return JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { createJourneySteps(propertyOwnershipId, returnUrl) },
            initialiseJourney = { initialiseJourneyState(token, propertyOwnershipId) },
            dispatch = dispatch,
        )
    }
}
