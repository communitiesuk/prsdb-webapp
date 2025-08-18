package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.COMPLAINTS_PROCEDURE_URL
import uk.gov.communities.prsdb.webapp.constants.DATA_PROTECTION_COMMUNITIES_EMAILS
import uk.gov.communities.prsdb.webapp.constants.INFORMATION_COMMISSIONERS_OFFICE_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE

@PrsdbController
@RequestMapping(LANDLORD_PRIVACY_NOTICE_ROUTE)
class LandlordPrivacyNoticeController {
    @GetMapping
    fun getPrivacyNoticePage(model: Model): String {
        model.addAttribute("complaintsProcedureUrl", COMPLAINTS_PROCEDURE_URL)
        model.addAttribute("dataProtectionEmail", DATA_PROTECTION_COMMUNITIES_EMAILS)
        model.addAttribute("informationCommissionersOfficeUrl", INFORMATION_COMMISSIONERS_OFFICE_URL)
        return "landlordPrivacyNotice"
    }

    companion object {
        const val LANDLORD_PRIVACY_NOTICE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PRIVACY_NOTICE_PATH_SEGMENT"
    }
}
