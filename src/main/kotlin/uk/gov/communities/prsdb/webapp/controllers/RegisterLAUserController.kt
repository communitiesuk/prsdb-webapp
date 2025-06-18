package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LaUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.security.Principal

@PrsdbController
@RequestMapping("/${REGISTER_LA_USER_JOURNEY_URL}")
class RegisterLAUserController(
    private val laUserRegistrationJourneyFactory: LaUserRegistrationJourneyFactory,
    private val invitationService: LocalAuthorityInvitationService,
    private val localAuthorityDataService: LocalAuthorityDataService,
    private val userRolesService: UserRolesService,
) {
    @GetMapping
    fun acceptInvitation(
        @RequestParam(value = "token", required = true) token: String,
    ): CharSequence {
        // This is using a CharSequence instead of returning a String to handle an error that otherwise occurs in
        // the LocalAuthorityInvitationService method that creates the invitation url using MvcUriComponentsBuilder.fromMethodName
        // see https://github.com/spring-projects/spring-hateoas/issues/155 for details
        if (invitationService.tokenIsValid(token)) {
            invitationService.storeTokenInSession(token)
            return "redirect:${REGISTER_LA_USER_JOURNEY_URL}/${RegisterLaUserStepId.LandingPage.urlPathSegment}"
        }

        return "redirect:$INVALID_LINK_PAGE_PATH_SEGMENT"
    }

    @GetMapping("/$LANDING_PAGE_PATH_SEGMENT")
    fun getLandingPage(
        model: Model,
        principal: Principal,
    ): ModelAndView {
        val token = getValidTokenFromSessionOrNull()
        if (token == null) {
            invitationService.clearTokenFromSession()
            return ModelAndView("redirect:$INVALID_LINK_PAGE_PATH_SEGMENT")
        }

        val invitation = invitationService.getInvitationFromToken(token)

        if (userRolesService.getHasLocalAuthorityRole(principal.name)) {
            invitationService.deleteInvitation(invitation)
            invitationService.clearTokenFromSession()
            return ModelAndView("redirect:$LOCAL_AUTHORITY_DASHBOARD_URL")
        }

        return laUserRegistrationJourneyFactory
            .create(invitation)
            .getModelAndViewForStep(
                LANDING_PAGE_PATH_SEGMENT,
                subPageNumber = null,
            )
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        principal: Principal,
    ): ModelAndView {
        val token = getValidTokenFromSessionOrNull()
        if (token == null) {
            invitationService.clearTokenFromSession()
            return ModelAndView("redirect:$INVALID_LINK_PAGE_PATH_SEGMENT")
        }

        return laUserRegistrationJourneyFactory
            .create(invitationService.getInvitationFromToken(token))
            .getModelAndViewForStep(
                stepName,
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
    ): ModelAndView {
        val token = getValidTokenFromSessionOrNull()
        if (token == null) {
            invitationService.clearTokenFromSession()
            return ModelAndView("redirect:$INVALID_LINK_PAGE_PATH_SEGMENT")
        }

        return laUserRegistrationJourneyFactory
            .create(invitationService.getInvitationFromToken(token))
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
            )
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val localAuthorityUserID =
            localAuthorityDataService.getLastUserIdRegisteredThisSession()
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No registered LA user was found in the session",
                )

        val localAuthorityUser =
            localAuthorityDataService.getLocalAuthorityUserOrNull(localAuthorityUserID)
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No LA user with ID $localAuthorityUserID was found in the database",
                )

        model.addAttribute("localAuthority", localAuthorityUser.localAuthority.name)
        model.addAttribute("dashboardUrl", LOCAL_AUTHORITY_DASHBOARD_URL)

        return "registerLAUserSuccess"
    }

    @GetMapping("/$INVALID_LINK_PAGE_PATH_SEGMENT")
    fun invalidToken(model: Model): String = "invalidLaInvitationLink"

    private fun getValidTokenFromSessionOrNull(): String? {
        val token = invitationService.getTokenFromSession()
        return if (token == null || !invitationService.tokenIsValid(token)) {
            null
        } else {
            token
        }
    }
}
