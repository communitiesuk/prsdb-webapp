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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.ONE_LOGIN_INFO_URL_POVING_YOUR_IDENTITY
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_PRSD
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TENANCY_TYPES_GUIDE_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.security.Principal

@PrsdbController
@RequestMapping(LANDLORD_REGISTRATION_ROUTE)
class RegisterLandlordController(
    private val landlordRegistrationJourneyFactory: LandlordRegistrationJourneyFactory,
    private val landlordService: LandlordService,
    private val userRolesService: UserRolesService,
) {
    @GetMapping
    fun index(model: Model): CharSequence {
        model.addAttribute("registerAsALandlordStartPageRoute", LANDLORD_REGISTRATION_START_PAGE_ROUTE)
        model.addAttribute("oneLoginInfoUrl", ONE_LOGIN_INFO_URL)
        model.addAttribute("provingYourIdentity", ONE_LOGIN_INFO_URL_POVING_YOUR_IDENTITY)
        model.addAttribute("rentersRightsBillGuideUrl", RENTERS_RIGHTS_BILL_GUIDE_URL)
        model.addAttribute("tenancyTypesGuideUrl", TENANCY_TYPES_GUIDE_URL)
        model.addAttribute("rentersRightsBillPRSD", RENTERS_RIGHTS_BILL_PRSD)
        model.addAttribute("landlordPrivacyNoticeUrl", LANDLORD_PRIVACY_NOTICE_ROUTE)
        return "registerAsALandlord"
    }

    @GetMapping("/$START_PAGE_PATH_SEGMENT")
    fun getStart(model: Model): String {
        model.addAttribute("registerAsALandlordInitialStep", PrivacyNoticeStep.ROUTE_SEGMENT)
        model.addAttribute("backUrl", LANDLORD_REGISTRATION_ROUTE)

        return "registerAsALandlordStartPage"
    }

    @GetMapping("/{stepRouteSegment}")
    fun getJourneyStep(
        @PathVariable stepRouteSegment: String,
        principal: Principal,
    ): ModelAndView =
        if (stepRouteSegment == PrivacyNoticeStep.ROUTE_SEGMENT &&
            userRolesService.getHasLandlordUserRole(principal.name)
        ) {
            ModelAndView("redirect:$LANDLORD_DASHBOARD_URL")
        } else {
            try {
                val journeyMap = landlordRegistrationJourneyFactory.createJourneySteps()
                journeyMap[stepRouteSegment]?.getStepModelAndView()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
            } catch (_: NoSuchJourneyException) {
                val journeyId = landlordRegistrationJourneyFactory.initializeJourneyState(principal)
                val redirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
                ModelAndView("redirect:$redirectUrl")
            }
        }

    @PostMapping("/{stepRouteSegment}")
    fun postJourneyData(
        @PathVariable stepRouteSegment: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = landlordRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepRouteSegment]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = landlordRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepRouteSegment, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

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
        const val LANDLORD_REGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REGISTER_LANDLORD_JOURNEY_URL"

        const val LANDLORD_REGISTRATION_START_PAGE_ROUTE = "$LANDLORD_REGISTRATION_ROUTE/$START_PAGE_PATH_SEGMENT"

        const val LANDLORD_REGISTRATION_CONFIRMATION_ROUTE = "$LANDLORD_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT"
    }
}
