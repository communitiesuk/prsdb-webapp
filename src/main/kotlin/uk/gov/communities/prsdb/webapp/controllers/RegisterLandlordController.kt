package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import java.security.Principal

@Controller
@RequestMapping("/${REGISTER_LANDLORD_JOURNEY_URL}")
class RegisterLandlordController(
    var landlordRegistrationJourney: LandlordRegistrationJourney,
    val identityService: OneLoginIdentityService,
    val landlordService: LandlordService,
) {
    @GetMapping
    fun index(model: Model): CharSequence {
        model.addAttribute(
            "registerAsALandlordInitialStep",
            "/${REGISTER_LANDLORD_JOURNEY_URL}/${START_PAGE_PATH_SEGMENT}",
        )
        return "registerAsALandlord"
    }

    @GetMapping("/${START_PAGE_PATH_SEGMENT}")
    fun getStart(): String = "redirect:${IDENTITY_VERIFICATION_PATH_SEGMENT}"

    @GetMapping("/${IDENTITY_VERIFICATION_PATH_SEGMENT}")
    fun getVerifyIdentity(
        model: Model,
        principal: Principal,
        @AuthenticationPrincipal oidcUser: OidcUser,
    ): String {
        var identity = identityService.getVerifiedIdentityData(oidcUser) ?: mutableMapOf()

        return landlordRegistrationJourney.updateJourneyDataAndGetViewNameOrRedirect(
            landlordRegistrationJourney.getStepId(IDENTITY_VERIFICATION_PATH_SEGMENT),
            identity,
            model,
            null,
            principal,
        )
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
    ): String =
        landlordRegistrationJourney.populateModelAndGetViewName(
            landlordRegistrationJourney.getStepId(stepName),
            model,
            subpage,
        )

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): String =
        landlordRegistrationJourney.updateJourneyDataAndGetViewNameOrRedirect(
            landlordRegistrationJourney.getStepId(stepName),
            formData,
            model,
            subpage,
            principal,
        )

    @GetMapping("/$CONFIRMATION_PAGE_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        model.addAttribute("registrationNumber", RegistrationNumberDataModel.toString(landlord.registrationNumber))

        return "registerAsALandlordConfirmation"
    }

    companion object {
        const val START_PAGE_PATH_SEGMENT = "start"
        const val IDENTITY_VERIFICATION_PATH_SEGMENT = "verify-identity"
        const val CONFIRMATION_PAGE_PATH_SEGMENT = "confirmation"
    }
}
