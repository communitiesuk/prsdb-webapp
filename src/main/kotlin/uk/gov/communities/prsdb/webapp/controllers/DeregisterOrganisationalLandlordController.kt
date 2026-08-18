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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_ORGANISATIONAL_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController.Companion.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbController
@RequestMapping(ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE)
class DeregisterOrganisationalLandlordController(
    private val organisationalLandlordDeregistrationJourneyFactory: OrganisationalLandlordDeregistrationJourneyFactory,
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val userToLandlordService: UserToLandlordService,
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
            createRoutingMap = { organisationalLandlordDeregistrationJourneyFactory.createJourneySteps() },
            initialiseJourney = { organisationalLandlordDeregistrationJourneyFactory.initializeJourneyState() },
            dispatch = dispatch,
        )

    // No @PreAuthorize: after deregistration the user no longer holds the LANDLORD role, so the
    // success page must remain reachable to an authenticated user without that role.
    @AvailableWhenFeatureEnabled(ORGANISATION_LANDLORD_REGISTRATION)
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(): ModelAndView {
        if (!landlordDeregistrationService.hasOrganisationDeregisteredInThisSession()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Organisational landlord deregistration has not been performed in this session",
            )
        }

        if (userToLandlordService.doesCurrentUserHaveLandlord()) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Organisational landlord deregistration did not complete successfully",
            )
        }

        return ModelAndView(
            "deregisterOrganisationalLandlordConfirmation",
            mapOf(
                "organisationName" to landlordDeregistrationService.getDeregisteredOrganisationNameFromSession(),
            ),
        )
    }

    companion object {
        const val ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_ORGANISATIONAL_LANDLORD_JOURNEY_URL"

        const val ORGANISATIONAL_LANDLORD_DEREGISTRATION_PATH =
            "$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}"
    }
}
