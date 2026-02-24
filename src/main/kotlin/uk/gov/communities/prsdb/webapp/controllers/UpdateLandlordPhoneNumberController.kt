package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_LANDLORD_PHONE_NUMBER_UPDATE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordPhoneNumberController.Companion.UPDATE_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber.UpdatePhoneNumberJourneyFactory
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_PHONE_NUMBER_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateLandlordPhoneNumberController(
    private val updatePhoneNumberJourneyFactory: UpdatePhoneNumberJourneyFactory,
) {
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_PHONE_NUMBER_UPDATE)
    fun getJourneyStep(
        @PathVariable stepName: String,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        return try {
            val journeySteps = updatePhoneNumberJourneyFactory.createJourneySteps()
            journeySteps[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = updatePhoneNumberJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_PHONE_NUMBER_UPDATE)
    fun postJourneyStep(
        @PathVariable stepName: String,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        return try {
            val journeySteps = updatePhoneNumberJourneyFactory.createJourneySteps()
            journeySteps[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = updatePhoneNumberJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    companion object {
        const val UPDATE_PHONE_NUMBER_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/update-phone-number"
    }
}
