package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import java.security.Principal

@Controller
@RequestMapping("/${REGISTER_LA_USER_JOURNEY_URL}")
class RegisterLAUserController(
    var laUserRegistrationJourney: LaUserRegistrationJourney,
    var invitationService: LocalAuthorityInvitationService,
    var journeyDataService: JourneyDataService,
) {
    @GetMapping("/")
    fun acceptInvitation(token: String): CharSequence {
        // This is using a CharSequence instead of returning a String to handle an error that otherwise occurs in
        // the LocalAuthorityInvitationService method that creates the invitation url using MvcUriComponentsBuilder.fromMethodName
        // see https://github.com/spring-projects/spring-hateoas/issues/155 for details
        if (invitationService.tokenIsValid(token)) {
            invitationService.storeTokenInSession(token)
            prePopulateJourneyData(token)
            return "redirect:${laUserRegistrationJourney.initialStepId.urlPathSegment}"
        }

        // TODO: This doesn't exist yet!
        return "redirect:/invalid-token"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
    ): String {
        val token = invitationService.getTokenFromSession()
        if (token == null || !invitationService.tokenIsValid(token)) {
            return "redirect:/invalid-token"
        }

        return laUserRegistrationJourney.populateModelAndGetViewName(
            laUserRegistrationJourney.getStepId(stepName),
            model,
            subpage,
        )
    }

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): String =
        laUserRegistrationJourney.updateJourneyDataAndGetViewNameOrRedirect(
            laUserRegistrationJourney.getStepId(stepName),
            formData,
            model,
            subpage,
            principal,
        )

    fun prePopulateJourneyData(token: String) {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val emailStep = laUserRegistrationJourney.steps.singleOrNull { step -> step.id == RegisterLaUserStepId.Email }
        val formData = mutableMapOf<String, String?>("emailAddress" to invitationService.getEmailAddressForToken(token)) as PageData
        emailStep?.updateJourneyData(journeyData, formData, null)

        journeyDataService.setJourneyData(journeyData)
    }
}
