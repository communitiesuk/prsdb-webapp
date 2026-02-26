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
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_LANDLORD_NAME_UPDATE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordNameController.Companion.UPDATE_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.name.UpdateNameJourneyFactory
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_NAME_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateLandlordNameController(
    private val journeyFactory: UpdateNameJourneyFactory,
) {
    @GetMapping("{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_NAME_UPDATE)
    fun getUpdateStep(
        principal: Principal,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView {
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
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_NAME_UPDATE)
    fun postUpdateStep(
        principal: Principal,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView {
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

    companion object {
        const val UPDATE_NAME_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/update-name"
    }
}
