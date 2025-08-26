package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_BASE_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeleteIncompletePropertyRegistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionViewModelBuilder
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.IncompletePropertyViewModelBuilder
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.LandlordDashboardNotificationBannerViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import java.security.Principal

@PreAuthorize("hasAnyRole('LANDLORD')")
@PrsdbController
@RequestMapping(LANDLORD_BASE_URL, "/")
class LandlordController(
    private val landlordService: LandlordService,
    private val propertyRegistrationService: PropertyRegistrationService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val backUrlStorageService: BackUrlStorageService,
) {
    @GetMapping
    fun index(): CharSequence = "redirect:$LANDLORD_DASHBOARD_URL"

    @GetMapping("/$DASHBOARD_PATH_SEGMENT")
    fun landlordDashboard(
        model: Model,
        principal: Principal,
    ): String {
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(principal.name)
                ?: throw PrsdbWebException("User ${principal.name} is not registered as a landlord")

        val numberOfComplianceActions =
            propertyOwnershipService.getNumberOfIncompleteCompliancesForLandlord(principal.name) +
                propertyComplianceService.getNumberOfNonCompliantPropertiesForLandlord(principal.name)

        val landlordDashboardNotificationBannerViewModel =
            LandlordDashboardNotificationBannerViewModel(
                numberOfIncompleteProperties =
                    propertyRegistrationService.getNumberOfIncompletePropertyRegistrationsForLandlord(principal.name),
                numberOfComplianceActions = numberOfComplianceActions,
            )

        model.addAttribute("landlordDashboardNotificationBannerViewModel", landlordDashboardNotificationBannerViewModel)
        model.addAttribute("landlordName", landlord.name)
        model.addAttribute("lrn", RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber))

        model.addAttribute("registerPropertyUrl", RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        model.addAttribute("viewIncompletePropertiesUrl", INCOMPLETE_PROPERTIES_URL)
        model.addAttribute(
            "viewPropertiesUrl",
            "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_FRAGMENT",
        )
        model.addAttribute("viewLandlordRecordUrl", LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
        model.addAttribute("addComplianceUrl", ADD_COMPLIANCE_URL)

        model.addAttribute("privacyNoticeUrl", LANDLORD_PRIVACY_NOTICE_ROUTE)
        model.addAttribute("rentersRightsBillUrl", RENTERS_RIGHTS_BILL_URL)
        model.addAttribute("registerLandlordUrl", RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)

        return "landlordDashboard"
    }

    @GetMapping("/$INCOMPLETE_PROPERTIES_PATH_SEGMENT")
    fun landlordIncompleteProperties(
        model: Model,
        principal: Principal,
    ): String {
        val incompleteProperties =
            propertyRegistrationService.getIncompletePropertiesForLandlord(principal.name)

        val incompletePropertyViewModels =
            incompleteProperties.mapIndexed { index, dataModel ->
                IncompletePropertyViewModelBuilder.fromDataModel(index, dataModel, backUrlStorageService.storeCurrentUrlReturningKey())
            }

        model.addAttribute("incompleteProperties", incompletePropertyViewModels)
        model.addAttribute("registerPropertyUrl", RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        model.addAttribute(
            "viewRegisteredPropertiesUrl",
            "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_FRAGMENT",
        )

        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "incompletePropertiesView"
    }

    @GetMapping("/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT")
    fun deleteIncompletePropertyAreYouSure(
        model: Model,
        principal: Principal,
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) contextId: Long,
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
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) contextId: Long,
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
            propertyRegistrationService.deleteIncompleteProperty(contextId, principal.name)
        }

        return "redirect:$INCOMPLETE_PROPERTIES_URL"
    }

    @GetMapping("/$ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT")
    fun addComplianceInformation(
        model: Model,
        principal: Principal,
    ): String {
        val incompleteComplianceProperties = propertyOwnershipService.getIncompleteCompliancesForLandlord(principal.name)
        val nonCompliantProperties = propertyComplianceService.getNonCompliantPropertiesForLandlord(principal.name)

        val complianceActions =
            (incompleteComplianceProperties + nonCompliantProperties).map {
                ComplianceActionViewModelBuilder.fromDataModel(it, backUrlStorageService.storeCurrentUrlReturningKey())
            }

        model.addAttribute("complianceActions", complianceActions)
        model.addAttribute(
            "viewRegisteredPropertiesUrl",
            "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_FRAGMENT",
        )
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "complianceActions"
    }

    fun populateDeleteIncompletePropertyRegistrationModel(
        model: Model,
        contextId: Long,
        principalName: String,
    ) {
        val formContext =
            propertyRegistrationService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                contextId,
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
        const val LANDLORD_DASHBOARD_URL = "/$LANDLORD_PATH_SEGMENT/$DASHBOARD_PATH_SEGMENT"
        const val LANDLORD_BASE_URL = "/$LANDLORD_PATH_SEGMENT"
        const val INCOMPLETE_PROPERTIES_URL = "/$LANDLORD_PATH_SEGMENT/$INCOMPLETE_PROPERTIES_PATH_SEGMENT"
        const val ADD_COMPLIANCE_URL = "/$LANDLORD_PATH_SEGMENT/$ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT"

        private const val DELETE_INCOMPLETE_PROPERTY_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$DELETE_INCOMPLETE_PROPERTY_PATH_SEGMENT?$CONTEXT_ID_URL_PARAMETER={contextId}"

        fun deleteIncompletePropertyPath(contextId: Long): String =
            UriTemplate(DELETE_INCOMPLETE_PROPERTY_ROUTE).expand(contextId).toASCIIString()
    }
}
