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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal

@PrsdbController
@RequestMapping(LANDLORD_DEREGISTRATION_ROUTE)
class DeregisterLandlordController(
    private val landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory,
    private val landlordService: LandlordService,
    private val landlordDeregistrationService: LandlordDeregistrationService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }

    private fun dispatchJourneyStep(
        stepPath: String,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { landlordDeregistrationJourneyFactory.createJourneySteps(principal.name) },
            initialiseJourney = { landlordDeregistrationJourneyFactory.initializeJourneyState() },
            dispatch = dispatch,
        )

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(principal: Principal): String {
        if (!landlordDeregistrationService.hasLandlordDeregisteredInThisSession()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Landlord deregistration has not been performed in this session",
            )
        }

        if (landlordService.retrieveLandlordByBaseUserId(principal.name) != null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Landlord with one-login id ${principal.name} was found in the database",
            )
        }

        val landlordHadRegisteredProperties = landlordDeregistrationService.getLandlordHadActivePropertiesFromSession()

        return if (landlordHadRegisteredProperties) {
            "deregisterLandlordWithRegisteredPropertiesConfirmation"
        } else {
            "deregisterLandlordWithNoPropertiesConfirmation"
        }
    }

    companion object {
        const val LANDLORD_DEREGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$DEREGISTER_LANDLORD_JOURNEY_URL"

        const val LANDLORD_DEREGISTRATION_PATH = "$LANDLORD_DEREGISTRATION_ROUTE/${AreYouSureStep.ROUTE_SEGMENT}"
    }
}
