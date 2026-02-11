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
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.NewPropertyRegistrationJourneyFactory
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_REGISTRATION_ROUTE)
class NewRegisterPropertyController(
    private val propertyRegistrationJourneyFactory: NewPropertyRegistrationJourneyFactory,
) {
    @GetMapping
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_REGISTRATION)
    fun index(model: Model): String {
        model.addAttribute(
            "registerPropertyInitialStep",
            "$PROPERTY_REGISTRATION_ROUTE/$TASK_LIST_PATH_SEGMENT",
        )
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyStartPage"
    }

    @GetMapping("/$RESUME_PAGE_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_REGISTRATION)
    fun getResume(
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) journeyId: String,
    ): String {
        val redirectUrl = JourneyStateService.urlWithJourneyState(TASK_LIST_PATH_SEGMENT, journeyId)
        return "redirect:$redirectUrl"
    }

    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_REGISTRATION)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_REGISTRATION)
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
}
