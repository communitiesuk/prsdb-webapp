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
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_LANDLORD_DATE_OF_BIRTH_UPDATE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordDateOfBirthController.Companion.UPDATE_DATE_OF_BIRTH_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.dateOfBirth.UpdateDateOfBirthJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_DATE_OF_BIRTH_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateLandlordDateOfBirthController(
    private val journeyFactory: UpdateDateOfBirthJourneyFactory,
    private val landlordService: LandlordService,
) {
    @GetMapping("{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_DATE_OF_BIRTH_UPDATE)
    fun getUpdateStep(
        principal: Principal,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView {
        redirectIfLandlordIsIdentityVerified(principal)?.let { return it }
        return try {
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    @PostMapping("{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_DATE_OF_BIRTH_UPDATE)
    fun postUpdateStep(
        principal: Principal,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView {
        redirectIfLandlordIsIdentityVerified(principal)?.let { return it }
        return try {
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    private fun redirectIfLandlordIsIdentityVerified(principal: Principal): ModelAndView? {
        val landlord = landlordService.retrieveLandlordByBaseUserId(principal.name)
        return if (landlord == null || landlord.isVerified) {
            ModelAndView("redirect:$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE")
        } else {
            null
        }
    }

    companion object {
        const val UPDATE_DATE_OF_BIRTH_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/update-date-of-birth"
    }
}
