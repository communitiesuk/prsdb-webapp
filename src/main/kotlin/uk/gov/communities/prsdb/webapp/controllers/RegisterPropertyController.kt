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
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CHANGE_ANSWER_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_REGISTRATION_ROUTE)
class RegisterPropertyController(
    private val propertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute(
            "registerPropertyInitialStep",
            "$PROPERTY_REGISTRATION_ROUTE/$START_PAGE_PATH_SEGMENT",
        )
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyStartPage"
    }

    @GetMapping("/$START_PAGE_PATH_SEGMENT")
    fun getStart(): String {
        journeyDataServiceFactory.create(PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY).removeJourneyDataAndContextIdFromSession()
        return "redirect:$TASK_LIST_PATH_SEGMENT"
    }

    @GetMapping("/$RESUME_PAGE_PATH_SEGMENT")
    fun getResume(
        principal: Principal,
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) contextId: String,
    ): String {
        val formContext =
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                contextId.toLong(),
                principal.name,
            )
        journeyDataServiceFactory
            .create(
                PropertyRegistrationJourneyFactory.JOURNEY_DATA_KEY,
            ).loadJourneyDataIntoSession(formContext)
        return "redirect:$TASK_LIST_PATH_SEGMENT"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerFor: String? = null,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        propertyRegistrationJourneyFactory
            .create(principal.name)
            .getModelAndViewForStep(
                stepName,
                subpage,
                changingAnswersForStep = changingAnswerFor,
            )

    @GetMapping("/$TASK_LIST_PATH_SEGMENT")
    fun getTaskList(principal: Principal): ModelAndView =
        propertyRegistrationJourneyFactory
            .create(principal.name)
            .getModelAndViewForTaskList()

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerFor: String? = null,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        propertyRegistrationJourneyFactory
            .create(principal.name)
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
                changingAnswerFor,
            )

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val propertyRegistrationNumber =
            propertyRegistrationService.getLastPrnRegisteredThisSession()
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No registered property was found in the session",
                )
        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No property ownership with registration number $propertyRegistrationNumber was found in the database",
                )

        model.addAttribute("singleLineAddress", propertyOwnership.property.address.singleLineAddress)
        model.addAttribute(
            "prn",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
        )
        model.addAttribute("isOccupied", propertyOwnership.isOccupied)
        model.addAttribute("propertyComplianceUrl", PropertyComplianceController.getPropertyCompliancePath(propertyOwnership.id))
        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyConfirmation"
    }

    companion object {
        const val PROPERTY_REGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REGISTER_PROPERTY_JOURNEY_URL"

        const val RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE =
            "$PROPERTY_REGISTRATION_ROUTE/$RESUME_PAGE_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER={contextId}"

        fun getResumePropertyRegistrationPath(contextId: Long): String =
            UriTemplate(RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE).expand(contextId).toASCIIString()
    }
}
