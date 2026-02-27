package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeleteIncompletePropertyRegistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.IncompletePropertyViewModelBuilder
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationConfirmationService
import java.security.Principal

@PreAuthorize("hasAnyRole('LANDLORD')")
@PrsdbController
@RequestMapping(LandlordController.LANDLORD_BASE_URL, "/")
class IncompletePropertiesController(
    private val propertyRegistrationService: PropertyRegistrationConfirmationService,
    private val incompletePropertyForLandlordService: IncompletePropertyForLandlordService,
    private val backUrlStorageService: BackUrlStorageService,
) {
    @GetMapping("/${INCOMPLETE_PROPERTIES_PATH_SEGMENT}")
    fun landlordIncompleteProperties(
        model: Model,
        principal: Principal,
    ): String {
        val incompleteProperties =
            incompletePropertyForLandlordService.getIncompletePropertiesForLandlord(principal.name)

        val incompletePropertyViewModels =
            incompleteProperties.mapIndexed { index, dataModel ->
                IncompletePropertyViewModelBuilder.fromDataModel(
                    index,
                    dataModel,
                    backUrlStorageService.storeCurrentUrlReturningKey(),
                )
            }

        model.addAttribute("incompleteProperties", incompletePropertyViewModels)
        model.addAttribute("registerPropertyUrl", RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        model.addAttribute(
            "viewRegisteredPropertiesUrl",
            "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#${REGISTERED_PROPERTIES_FRAGMENT}",
        )

        model.addAttribute("backUrl", LandlordController.LANDLORD_DASHBOARD_URL)

        return "incompletePropertiesView"
    }

    @GetMapping("/${DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT}")
    fun deleteIncompletePropertyAreYouSure(
        model: Model,
        principal: Principal,
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) journeyId: String,
    ): String {
        populateDeleteIncompletePropertyRegistrationModel(model, journeyId, principal.name)
        model.addAttribute(
            "deleteIncompletePropertyRegistrationAreYouSureFormModel",
            DeleteIncompletePropertyRegistrationAreYouSureFormModel(),
        )

        return "deleteIncompletePropertyRegistration"
    }

    @PostMapping("/${DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT}")
    fun deleteIncompletePropertyAreYouSure(
        model: Model,
        principal: Principal,
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) contextId: String,
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
            incompletePropertyForLandlordService.deleteIncompleteProperty(contextId, principal.name)
            propertyRegistrationService.addIncompletePropertyFormContextsDeletedThisSession(contextId)
            return "redirect:${getDeleteIncompletePropertyConfirmationPath(contextId)}"
        }

        return "redirect:${LandlordController.INCOMPLETE_PROPERTIES_URL}"
    }

    @GetMapping("/${DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT}/${CONFIRMATION_PATH_SEGMENT}")
    fun deleteIncompletePropertyConfirmation(
        model: Model,
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) journeyId: String,
        principal: Principal,
    ): String {
        if (!propertyRegistrationService.wasIncompletePropertyDeletedThisSession(journeyId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Journey with id $journeyId was not found in the list of cancelled incomplete property registrations in the session",
            )
        }

        if (incompletePropertyForLandlordService.isIncompletePropertyAvailable(journeyId, principal.name)) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Incomplete property registration with id $journeyId is still in the database",
            )
        }

        model.addAttribute("incompletePropertiesUrl", LandlordController.INCOMPLETE_PROPERTIES_URL)

        return "deleteIncompletePropertyConfirmation"
    }

    fun populateDeleteIncompletePropertyRegistrationModel(
        model: Model,
        journeyId: String,
        principalName: String,
    ) {
        val singleLineAddress = incompletePropertyForLandlordService.getAddressData(journeyId, principalName)

        model.addAttribute("radioOptions", RadiosViewModel.yesOrNoRadios())
        model.addAttribute("singleLineAddress", singleLineAddress)
        model.addAttribute(BACK_URL_ATTR_NAME, LandlordController.INCOMPLETE_PROPERTIES_URL)
    }

    companion object {
        private const val DELETE_INCOMPLETE_PROPERTY_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT?$CONTEXT_ID_URL_PARAMETER={contextId}"

        private const val DELETE_INCOMPLETE_PROPERTY_CONFIRMATION_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT/" +
                "$CONFIRMATION_PATH_SEGMENT?$CONTEXT_ID_URL_PARAMETER={contextId}"

        fun getDeleteIncompletePropertyPath(journeyId: String): String =
            UriTemplate(DELETE_INCOMPLETE_PROPERTY_ROUTE).expand(journeyId).toASCIIString()

        fun getDeleteIncompletePropertyConfirmationPath(journeyId: String): String =
            UriTemplate(DELETE_INCOMPLETE_PROPERTY_CONFIRMATION_ROUTE).expand(journeyId).toASCIIString()
    }
}
