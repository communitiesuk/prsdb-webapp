package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.START_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_PROPERTIES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeleteIncompletePropertyRegistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping("/$REGISTER_PROPERTY_JOURNEY_URL")
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
            "/$REGISTER_PROPERTY_JOURNEY_URL/$START_PAGE_PATH_SEGMENT",
        )
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyStartPage"
    }

    @GetMapping("/$START_PAGE_PATH_SEGMENT")
    fun getStart(): String {
        journeyDataServiceFactory.create(REGISTER_PROPERTY_JOURNEY_URL).removeJourneyDataAndContextIdFromSession()
        return "redirect:$TASK_LIST_PATH_SEGMENT"
    }

    @GetMapping("/$RESUME_PAGE_PATH_SEGMENT")
    fun getResume(
        principal: Principal,
        @RequestParam(value = "contextId", required = true) contextId: String,
    ): String {
        val formContext =
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                contextId.toLong(),
                principal.name,
            )
        journeyDataServiceFactory
            .create(
                REGISTER_PROPERTY_JOURNEY_URL,
            ).loadJourneyDataIntoSession(formContext)
        return "redirect:$TASK_LIST_PATH_SEGMENT"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        propertyRegistrationJourneyFactory
            .create(principal.name)
            .getModelAndViewForStep(
                stepName,
                subpage,
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

    @GetMapping("/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT")
    fun deleteIncompletePropertyAreYouSure(
        model: Model,
        principal: Principal,
        @RequestParam(value = "contextId", required = true) contextId: String,
    ): String {
        populateDeleteIncompletePropertyRegistrationModel(model, contextId, principal.name)
        model.addAttribute(
            "deleteIncompletePropertyRegistrationAreYouSureFormModel",
            DeleteIncompletePropertyRegistrationAreYouSureFormModel(),
        )

        return "deleteIncompletePropertyRegistration"
    }

    @PostMapping("/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT")
    fun deleteIncompletePropertyAreYouSure(
        model: Model,
        principal: Principal,
        @RequestParam(value = "contextId", required = true) contextId: String,
        @Valid
        @ModelAttribute
        formModel: DeleteIncompletePropertyRegistrationAreYouSureFormModel,
        bindingResult: BindingResult,
    ): String {
        if (bindingResult.hasErrors()) {
            populateDeleteIncompletePropertyRegistrationModel(model, contextId, principal.name)
            return "deleteIncompletePropertyRegistration"
        }

        if (formModel.wantsToProceed == true) {
            propertyRegistrationService.deleteIncompleteProperty(contextId.toLong(), principal.name)
        }

        return "redirect:$INCOMPLETE_PROPERTIES_URL"
    }

    fun populateDeleteIncompletePropertyRegistrationModel(
        model: Model,
        contextId: String,
        principalName: String,
    ) {
        val formContext =
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                contextId.toLong(),
                principalName,
            )
        val singleLineAddress = propertyRegistrationService.getAddressData(formContext).singleLineAddress

        model.addAttribute(
            "radioOptions",
            listOf(
                RadiosButtonViewModel(
                    value = true,
                    valueStr = "yes",
                    labelMsgKey = "forms.radios.option.yes.label",
                ),
                RadiosButtonViewModel(
                    value = false,
                    valueStr = "no",
                    labelMsgKey = "forms.radios.option.no.label",
                ),
            ),
        )
        model.addAttribute("singleLineAddress", singleLineAddress)
        model.addAttribute(BACK_URL_ATTR_NAME, INCOMPLETE_PROPERTIES_URL)
    }

    companion object {
        const val CONTEXT_ID_URL_PARAMETER = "contextId"

        const val RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE =
            "/$REGISTER_PROPERTY_JOURNEY_URL/$RESUME_PAGE_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER={contextId}"

        const val DELETE_INCOMPLETE_PROPERTY_ROUTE =
            "/$REGISTER_PROPERTY_JOURNEY_URL/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER={contextId}"

        fun getResumePropertyRegistrationPath(contextId: Long): String =
            UriTemplate(RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE).expand(contextId).toASCIIString()

        fun deleteIncompletePropertyPath(contextId: Long): String =
            UriTemplate(DELETE_INCOMPLETE_PROPERTY_ROUTE).expand(contextId).toASCIIString()
    }
}
