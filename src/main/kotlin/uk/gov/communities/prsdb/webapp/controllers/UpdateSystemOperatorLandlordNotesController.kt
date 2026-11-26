package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorLandlordNotesController.Companion.UPDATE_SYSTEM_OPERATOR_LANDLORD_NOTES_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes.SystemOperatorLandlordNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes.UpdateSystemOperatorLandlordNotesJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_SYSTEM_OPERATOR_LANDLORD_NOTES_ROUTE)
@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
class UpdateSystemOperatorLandlordNotesController(
    private val journeyFactory: UpdateSystemOperatorLandlordNotesJourneyFactory,
    private val landlordService: LandlordService,
) {
    @GetMapping("/{*stepPath}")
    // TODO PDJB-2288: Remove this annotation when the ACT_AS_LANDLORD flag is removed
    @AvailableWhenFeatureEnabled(ACT_AS_LANDLORD)
    fun getUpdateStep(
        principal: Principal,
        @PathVariable id: Long,
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath, id, principal) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    // TODO PDJB-2288: Remove this annotation when the ACT_AS_LANDLORD flag is removed
    @AvailableWhenFeatureEnabled(ACT_AS_LANDLORD)
    fun postUpdateStep(
        principal: Principal,
        @PathVariable id: Long,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath, id, principal) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        landlordId: Long,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(landlordId) },
            initialiseJourney = {
                val landlord =
                    landlordService.retrieveLandlordById(landlordId)
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord $landlordId not found")
                journeyFactory.initializeJourneyState(Pair(landlordId, principal), landlord.getMostRecentlyUpdated())
            },
            dispatch = dispatch,
        )

    companion object {
        const val UPDATE_SYSTEM_OPERATOR_LANDLORD_NOTES_ROUTE = "$LANDLORD_DETAILS_FOR_SYSTEM_OPERATOR_ROUTE/update-notes"

        fun getUpdateNotesStepPath(landlordId: Long): String =
            UriTemplate(UPDATE_SYSTEM_OPERATOR_LANDLORD_NOTES_ROUTE)
                .expand(mapOf("id" to landlordId))
                .toASCIIString() + "/${SystemOperatorLandlordNotesStep.ROUTE_SEGMENT}"
    }
}
