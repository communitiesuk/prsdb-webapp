package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_ORGANISATION_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationLandlordController.Companion.ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.OrganisationLandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.stepConfig.AreYouSureStep

@PrsdbController
@RequestMapping(ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE)
class DeregisterOrganisationLandlordController(
    private val organisationLandlordDeregistrationJourneyFactory: OrganisationLandlordDeregistrationJourneyFactory,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @AvailableWhenFeatureEnabled(ORGANISATION_LANDLORD_REGISTRATION)
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
    ): ModelAndView = dispatchJourneyStep(stepPath) { getStepModelAndView() }

    @PreAuthorize("hasRole('LANDLORD')")
    @AvailableWhenFeatureEnabled(ORGANISATION_LANDLORD_REGISTRATION)
    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView = dispatchJourneyStep(stepPath) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { organisationLandlordDeregistrationJourneyFactory.createJourneySteps() },
            initialiseJourney = { organisationLandlordDeregistrationJourneyFactory.initializeJourneyState() },
            dispatch = dispatch,
        )

    // No @PreAuthorize: after deregistration the user no longer holds the LANDLORD role, so the
    // success page must remain reachable to an authenticated user without that role.
    @AvailableWhenFeatureEnabled(ORGANISATION_LANDLORD_REGISTRATION)
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(): String {
        // TODO: PDJB-1484 - Add session/deregistration guards and build the real success page content
        return "deregisterOrganisationLandlordConfirmation"
    }

    companion object {
        const val ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_ORGANISATION_LANDLORD_JOURNEY_URL"

        const val ORGANISATION_LANDLORD_DEREGISTRATION_PATH =
            "$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}"
    }
}
