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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController.Companion.LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController.Companion.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.NewLocalCouncilUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.security.Principal

@PrsdbController
@RequestMapping(LOCAL_COUNCIL_USER_REGISTRATION_ROUTE)
class NewRegisterLocalCouncilUserController(
    private val localCouncilUserRegistrationJourneyFactory: NewLocalCouncilUserRegistrationJourneyFactory,
    private val invitationService: LocalCouncilInvitationService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val userRolesService: UserRolesService,
) {
    @GetMapping
    @AvailableWhenFeatureEnabled(MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION)
    fun acceptInvitation(
        @RequestParam(value = TOKEN, required = true) token: String,
        principal: Principal,
    ): CharSequence {
        val invitation = invitationService.getInvitationOrNull(token)

        return if (invitation == null) {
            "redirect:$LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE"
        } else if (invitationService.getInvitationHasExpired(invitation)) {
            invitationService.deleteInvitation(invitation)
            "redirect:$LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE"
        } else {
            // Check if user already has local council role
            if (userRolesService.getHasLocalCouncilRole(principal.name)) {
                invitationService.deleteInvitation(invitation)
                return "redirect:$LOCAL_COUNCIL_DASHBOARD_URL"
            }

            invitationService.storeTokenInSession(token)
            val journeyId = localCouncilUserRegistrationJourneyFactory.initializeJourneyState(invitation)
            val redirectUrl =
                JourneyStateService.urlWithJourneyState(
                    "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$LANDING_PAGE_PATH_SEGMENT",
                    journeyId,
                )
            "redirect:$redirectUrl"
        }
    }

    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView {
        val token = getValidTokenFromSessionOrNull()
        if (token == null) {
            invitationService.clearTokenFromSession()
            return ModelAndView("redirect:$LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE")
        }

        return try {
            val journeyMap = localCouncilUserRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val invitation = invitationService.getInvitationFromToken(token)
            val journeyId = localCouncilUserRegistrationJourneyFactory.initializeJourneyState(invitation)
            val redirectUrl =
                JourneyStateService.urlWithJourneyState(
                    "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$stepName",
                    journeyId,
                )
            ModelAndView("redirect:$redirectUrl")
        }
    }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION)
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        val token = getValidTokenFromSessionOrNull()
        if (token == null) {
            invitationService.clearTokenFromSession()
            return ModelAndView("redirect:$LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE")
        }

        return try {
            val journeyMap = localCouncilUserRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val invitation = invitationService.getInvitationFromToken(token)
            val journeyId = localCouncilUserRegistrationJourneyFactory.initializeJourneyState(invitation)
            val redirectUrl =
                JourneyStateService.urlWithJourneyState(
                    "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$stepName",
                    journeyId,
                )
            ModelAndView("redirect:$redirectUrl")
        }
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION)
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val localCouncilUserID =
            localCouncilDataService.getLastUserIdRegisteredThisSession()
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No registered Local Council user was found in the session",
                )

        val localCouncilUser =
            localCouncilDataService.getLocalCouncilUserOrNull(localCouncilUserID)
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No Local Council user with ID $localCouncilUserID was found in the database",
                )

        model.addAttribute("localCouncil", localCouncilUser.localCouncil.name)
        model.addAttribute("dashboardUrl", LOCAL_COUNCIL_DASHBOARD_URL)

        return "registerLocalCouncilUserSuccess"
    }

    @GetMapping("/$INVALID_LINK_PAGE_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION)
    fun invalidToken(model: Model): String = "invalidLocalCouncilInvitationLink"

    private fun getValidTokenFromSessionOrNull(): String? {
        val token = invitationService.getTokenFromSession()
        return if (token == null || !invitationService.tokenIsValid(token)) {
            null
        } else {
            token
        }
    }
}
