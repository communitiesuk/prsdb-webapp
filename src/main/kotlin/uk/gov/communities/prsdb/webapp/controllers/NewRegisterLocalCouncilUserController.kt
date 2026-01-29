package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_LOCAL_COUNCIL_USER_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LOCAL_COUNCIL_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.NewLocalCouncilUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.security.Principal

@PrsdbController
@RequestMapping(NewRegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE)
class NewRegisterLocalCouncilUserController(
    private val journeyFactory: NewLocalCouncilUserRegistrationJourneyFactory,
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
        // This is using a CharSequence instead of returning a String to handle an error that otherwise occurs in
        // the LocalCouncilInvitationService method that creates the invitation url using MvcUriComponentsBuilder.fromMethodName
        // see https://github.com/spring-projects/spring-hateoas/issues/155 for details
        val invitation = invitationService.getInvitationOrNull(token)

        return if (invitation == null) {
            "redirect:$LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE"
        } else if (invitationService.getInvitationHasExpired(invitation)) {
            invitationService.deleteInvitation(invitation)
            "redirect:$LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE"
        } else {
            invitationService.storeTokenInSession(token)

            // Check if user already has local council role
            if (userRolesService.getHasLocalCouncilRole(principal.name)) {
                invitationService.deleteInvitation(invitation)
                invitationService.clearTokenFromSession()
                return "redirect:$LOCAL_COUNCIL_DASHBOARD_URL"
            }

            val journeyId = journeyFactory.initializeJourneyState(invitation)
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
        validateToken()

        return try {
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val invitation = getInvitationFromSession()
            val journeyId = journeyFactory.initializeJourneyState(invitation)
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
        validateToken()

        return try {
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val invitation = getInvitationFromSession()
            val journeyId = journeyFactory.initializeJourneyState(invitation)
            val redirectUrl =
                JourneyStateService.urlWithJourneyState(
                    "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$stepName",
                    journeyId,
                )
            ModelAndView("redirect:$redirectUrl")
        }
    }

    private fun validateToken() {
        if (getValidTokenFromSessionOrNull() == null) {
            invitationService.clearTokenFromSession()
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or missing invitation token")
        }
    }

    private fun getInvitationFromSession(): LocalCouncilInvitation {
        val token =
            getValidTokenFromSessionOrNull()
                ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token")
        return invitationService.getInvitationFromToken(token)
    }

    private fun getValidTokenFromSessionOrNull(): String? {
        val token = invitationService.getTokenFromSession()
        return if (token == null || !invitationService.tokenIsValid(token)) {
            null
        } else {
            token
        }
    }

    companion object {
        const val LOCAL_COUNCIL_USER_REGISTRATION_ROUTE = "/$LOCAL_COUNCIL_PATH_SEGMENT/$REGISTER_LOCAL_COUNCIL_USER_JOURNEY_URL"

        const val LOCAL_COUNCIL_USER_REGISTRATION_INVALID_LINK_ROUTE =
            "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$INVALID_LINK_PAGE_PATH_SEGMENT"
    }
}
