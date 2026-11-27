package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ACT_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.SYSTEM_OPERATOR_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorPropertyNotesController.Companion.UPDATE_SYSTEM_OPERATOR_PROPERTY_NOTES_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.propertyNotes.UpdateSystemOperatorPropertyNotesJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_SYSTEM_OPERATOR_PROPERTY_NOTES_ROUTE)
@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
class UpdateSystemOperatorPropertyNotesController(
    private val journeyFactory: UpdateSystemOperatorPropertyNotesJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @GetMapping("/{*stepPath}")
    // TODO PDJB-2288: Remove this annotation when the ACT_AS_LANDLORD flag is removed
    @AvailableWhenFeatureEnabled(ACT_AS_LANDLORD)
    fun getUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath, propertyOwnershipId, principal) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    // TODO PDJB-2288: Remove this annotation when the ACT_AS_LANDLORD flag is removed
    @AvailableWhenFeatureEnabled(ACT_AS_LANDLORD)
    fun postUpdateStep(
        principal: Principal,
        @PathVariable propertyOwnershipId: Long,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath, propertyOwnershipId, principal) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps(propertyOwnershipId) },
            initialiseJourney = {
                val propertyOwnership = propertyOwnershipService.retrievePropertyOwnershipByIdRegardlessOfActiveStatus(propertyOwnershipId)
                journeyFactory.initializeJourneyState(Pair(propertyOwnershipId, principal), propertyOwnership.getMostRecentlyUpdated())
            },
            dispatch = dispatch,
        )

    companion object {
        const val UPDATE_SYSTEM_OPERATOR_PROPERTY_NOTES_ROUTE = "$SYSTEM_OPERATOR_PROPERTY_DETAILS_ROUTE/update-notes"

        fun getUpdateNotesStepPath(propertyOwnershipId: Long): String =
            UriTemplate(UPDATE_SYSTEM_OPERATOR_PROPERTY_NOTES_ROUTE)
                .expand(mapOf("propertyOwnershipId" to propertyOwnershipId))
                .toASCIIString() + "/${AbstractSystemOperatorNotesStep.ROUTE_SEGMENT}"
    }
}
