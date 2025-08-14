package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL_POVING_YOUR_IDENTITY
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_PRSD
import uk.gov.communities.prsdb.webapp.constants.TENANCY_TYPES_GUIDE_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.security.Principal

@PrsdbController
@RequestMapping(LANDLORD_REGISTRATION_ROUTE)
class RegisterLandlordController(
    private val landlordRegistrationJourneyFactory: LandlordRegistrationJourneyFactory,
    private val identityService: OneLoginIdentityService,
    private val landlordService: LandlordService,
    private val userRolesService: UserRolesService,
) {
    @GetMapping
    fun index(model: Model): CharSequence {
        model.addAttribute(
            "registerAsALandlordInitialStep",
            "$LANDLORD_REGISTRATION_ROUTE/${PRIVACY_NOTICE_PATH_SEGMENT}",
        )
        model.addAttribute("oneLoginInfoUrl", ONE_LOGIN_INFO_URL)
        model.addAttribute("provingYourIdentity", ONE_LOGIN_INFO_URL_POVING_YOUR_IDENTITY)
        model.addAttribute("rentersRightsBillGuideUrl", RENTERS_RIGHTS_BILL_GUIDE_URL)
        model.addAttribute("tenancyTypesGuideUrl", TENANCY_TYPES_GUIDE_URL)
        model.addAttribute("rentersRightsBillPRSD", RENTERS_RIGHTS_BILL_PRSD)
        return "registerAsALandlord"
    }

    @GetMapping("/${PRIVACY_NOTICE_PATH_SEGMENT}")
    fun getPrivacyNotice(principal: Principal): ModelAndView =
        if (userRolesService.getHasLandlordUserRole(principal.name)) {
            ModelAndView("redirect:${LANDLORD_DASHBOARD_URL}")
        } else {
            landlordRegistrationJourneyFactory
                .create()
                .getModelAndViewForStep(PRIVACY_NOTICE_PATH_SEGMENT, null)
        }

    @GetMapping("/${IDENTITY_VERIFICATION_PATH_SEGMENT}")
    fun getVerifyIdentity(
        model: Model,
        principal: Principal,
        @AuthenticationPrincipal oidcUser: OidcUser,
    ): ModelAndView {
        val identity = identityService.getVerifiedIdentityData(oidcUser) ?: mapOf()

        return landlordRegistrationJourneyFactory
            .create()
            .completeStep(
                IDENTITY_VERIFICATION_PATH_SEGMENT,
                identity,
                subPageNumber = null,
                principal,
            )
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
    ): ModelAndView =
        landlordRegistrationJourneyFactory
            .create()
            .getModelAndViewForStep(stepName, subpage, checkingAnswersForStep = checkingAnswersForStep)

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
    ): ModelAndView =
        landlordRegistrationJourneyFactory
            .create()
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
                checkingAnswersForStep = checkingAnswersForStep,
            )

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        model.addAttribute(
            "registrationNumber",
            RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
        )
        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "registerAsALandlordConfirmation"
    }

    companion object {
        const val IDENTITY_VERIFICATION_PATH_SEGMENT = "verify-identity"

        const val LANDLORD_REGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REGISTER_LANDLORD_JOURNEY_URL"

        const val LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE = "$LANDLORD_REGISTRATION_ROUTE/$PRIVACY_NOTICE_PATH_SEGMENT"
    }
}
