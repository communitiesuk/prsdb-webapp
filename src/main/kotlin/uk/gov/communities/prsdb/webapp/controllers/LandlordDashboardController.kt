package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.DASHBOARD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RENTERS_RIGHTS_BILL_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_BASE_URL
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.security.Principal

@PreAuthorize("hasAnyRole('LANDLORD')")
@Controller
@RequestMapping(LANDLORD_BASE_URL, "/")
class LandlordDashboardController(
    private val landlordService: LandlordService,
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

        model.addAttribute("landlordName", landlord.name)
        model.addAttribute("lrn", RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber))

        model.addAttribute("registerPropertyUrl", "/$REGISTER_PROPERTY_JOURNEY_URL")
        model.addAttribute("viewPropertiesUrl", "/$LANDLORD_DETAILS_PATH_SEGMENT#$REGISTERED_PROPERTIES_PATH_SEGMENT")
        model.addAttribute("viewLandlordRecordUrl", "/$LANDLORD_DETAILS_PATH_SEGMENT")

        model.addAttribute("updatesToPilotUrl", "#")
        model.addAttribute("policyUpdatesUrl", "#")
        model.addAttribute("privacyNoticeUrl", "#")
        model.addAttribute("howToLetUrl", "#")
        model.addAttribute(
            "rentersRightsBillUrl",
            RENTERS_RIGHTS_BILL_URL,
        )
        model.addAttribute("keepingPropertyCompliantUrl", "#")

        return "landlordDashboard"
    }

    companion object {
        const val LANDLORD_DASHBOARD_URL = "/$LANDLORD_PATH_SEGMENT/$DASHBOARD_PATH_SEGMENT"
        const val LANDLORD_BASE_URL = "/$LANDLORD_PATH_SEGMENT"
    }
}
