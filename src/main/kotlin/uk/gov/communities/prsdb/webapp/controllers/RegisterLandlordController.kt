package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REGISTRATION_SURVEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.LandlordRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbController
@RequestMapping(LANDLORD_REGISTRATION_ROUTE)
class RegisterLandlordController(
    private val landlordRegistrationJourneyFactory: LandlordRegistrationJourneyFactory,
    private val userToLandlordService: UserToLandlordService,
    private val userRolesService: UserRolesService,
) {
    @GetMapping
    fun redirectToStart(): String = "redirect:$LANDLORD_REGISTRATION_START_PAGE_ROUTE"

    @GetMapping("/$START_PAGE_PATH_SEGMENT")
    fun getStart(model: Model): CharSequence {
        model.addAttribute("registerAsALandlordFirstStepRoute", "$LANDLORD_REGISTRATION_ROUTE/${PrivacyNoticeStep.ROUTE_SEGMENT}")
        return "registerAsALandlord"
    }

    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        principal: Principal,
    ): ModelAndView =
        if (stepPath.trimStart('/') == PrivacyNoticeStep.ROUTE_SEGMENT &&
            userRolesService.getHasLandlordUserRole(principal.name)
        ) {
            ModelAndView("redirect:$LANDLORD_DASHBOARD_URL")
        } else {
            dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }
        }

    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(model: Model): String {
        val landlord = userToLandlordService.getCurrentLandlordForUser()

        model.addAttribute(
            "registrationNumber",
            RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
        )
        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)
        model.addAttribute("showSurveyLink", landlord is IndividualLandlord)
        model.addAttribute("landlordRegistrationSurveyUrl", LANDLORD_REGISTRATION_SURVEY_URL)

        return "registerAsALandlordConfirmation"
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            stepPath,
            { landlordRegistrationJourneyFactory.createJourneySteps() },
            { landlordRegistrationJourneyFactory.initializeJourneyState(principal) },
            dispatch,
        )

    companion object {
        const val LANDLORD_REGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REGISTER_LANDLORD_JOURNEY_URL"

        const val LANDLORD_REGISTRATION_START_PAGE_ROUTE = "$LANDLORD_REGISTRATION_ROUTE/$START_PAGE_PATH_SEGMENT"

        const val LANDLORD_REGISTRATION_CONFIRMATION_ROUTE = "$LANDLORD_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT"
    }
}
