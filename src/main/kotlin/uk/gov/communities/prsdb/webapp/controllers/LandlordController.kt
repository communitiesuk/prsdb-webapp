package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Primary
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.COMPLIANCE_ACTIONS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_BASE_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.ComplianceActionViewModelBuilder
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.LandlordDashboardNotificationBannerViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasAnyRole('LANDLORD')")
@PrsdbController
@RequestMapping(LANDLORD_BASE_URL, "/")
class LandlordController(
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val backUrlStorageService: BackUrlStorageService,
    private val incompletePropertiesStrategy: NumberOfIncompletePropertiesFeatureStrategy,
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
                numberOfIncompleteProperties = incompletePropertiesStrategy.numberOfIncompleteProperties(landlord),
                numberOfComplianceActions = numberOfComplianceActions,
            )

        model.addAttribute("landlordDashboardNotificationBannerViewModel", landlordDashboardNotificationBannerViewModel)
        model.addAttribute("landlordName", landlord.name)
        model.addAttribute("lrn", RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber))

        model.addAttribute("registerPropertyUrl", RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        model.addAttribute("viewIncompletePropertiesUrl", INCOMPLETE_PROPERTIES_URL)
        model.addAttribute("joinPropertyUrl", "#")
        model.addAttribute(
            "viewPropertiesUrl",
            "${LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE}#$REGISTERED_PROPERTIES_FRAGMENT",
        )
        model.addAttribute("viewLandlordRecordUrl", LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
        model.addAttribute("addComplianceUrl", COMPLIANCE_ACTIONS_URL)

        model.addAttribute("privacyNoticeUrl", LANDLORD_PRIVACY_NOTICE_ROUTE)
        model.addAttribute("rentersRightsBillUrl", RENTERS_RIGHTS_BILL_URL)
        model.addAttribute("registerLandlordUrl", RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE)

        return "landlordDashboard"
    }

    @GetMapping("/$COMPLIANCE_ACTIONS_PATH_SEGMENT")
    fun getComplianceActions(
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

    companion object {
        const val LANDLORD_DASHBOARD_URL = "/$LANDLORD_PATH_SEGMENT/$DASHBOARD_PATH_SEGMENT"
        const val LANDLORD_BASE_URL = "/$LANDLORD_PATH_SEGMENT"
        const val INCOMPLETE_PROPERTIES_URL = "/$LANDLORD_PATH_SEGMENT/$INCOMPLETE_PROPERTIES_PATH_SEGMENT"
        const val COMPLIANCE_ACTIONS_URL = "/$LANDLORD_PATH_SEGMENT/$COMPLIANCE_ACTIONS_PATH_SEGMENT"
    }
}

interface NumberOfIncompletePropertiesFeatureStrategy {
    @PrsdbFlip(MIGRATE_PROPERTY_REGISTRATION, alterBean = "newNumberOfIncompletePropertiesProvider")
    fun numberOfIncompleteProperties(landlord: Landlord): Int
}

@PrsdbWebService("oldNumberOfIncompletePropertiesProvider")
@Primary
class OldNumberOfIncompletePropertiesStrategy(
    private val incompletePropertyForLandlordService: IncompletePropertyForLandlordService,
) : NumberOfIncompletePropertiesFeatureStrategy {
    override fun numberOfIncompleteProperties(landlord: Landlord): Int =
        incompletePropertyForLandlordService.getIncompletePropertiesForLandlord(landlord.baseUser.id).size
}

@PrsdbWebService("newNumberOfIncompletePropertiesProvider")
class NewNumberOfIncompletePropertiesStrategy : NumberOfIncompletePropertiesFeatureStrategy {
    override fun numberOfIncompleteProperties(landlord: Landlord): Int = landlord.incompleteProperties.size
}
