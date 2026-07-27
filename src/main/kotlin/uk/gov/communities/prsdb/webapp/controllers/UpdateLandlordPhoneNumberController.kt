package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordPhoneNumberController.Companion.UPDATE_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber.UpdatePhoneNumberJourneyFactory
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_PHONE_NUMBER_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateLandlordPhoneNumberController(
    private val updatePhoneNumberJourneyFactory: UpdatePhoneNumberJourneyFactory,
) {
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    fun postJourneyStep(
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
            createRoutingMap = { updatePhoneNumberJourneyFactory.createJourneySteps() },
            initialiseJourney = { updatePhoneNumberJourneyFactory.initializeJourneyState(principal) },
            dispatch = dispatch,
        )

    companion object {
        const val UPDATE_PHONE_NUMBER_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/update-phone-number"
    }
}
