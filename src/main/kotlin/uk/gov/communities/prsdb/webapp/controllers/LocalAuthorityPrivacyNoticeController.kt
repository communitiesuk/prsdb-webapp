package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.COMPLAINTS_PROCEDURE_URL
import uk.gov.communities.prsdb.webapp.constants.DATA_PROTECTION_COMMUNITIES_EMAILS
import uk.gov.communities.prsdb.webapp.constants.DPO_COMMUNITIES_EMAILS
import uk.gov.communities.prsdb.webapp.constants.INFORMATION_COMMISSIONERS_OFFICE_URL
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityPrivacyNoticeController.Companion.LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE

@PrsdbController
@RequestMapping(LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE)
class LocalAuthorityPrivacyNoticeController {
    @GetMapping
    fun getPrivacyNoticePage(model: Model): String {
        model.addAttribute("complaintsProcedureUrl", COMPLAINTS_PROCEDURE_URL)
        model.addAttribute("backUrl", LOCAL_AUTHORITY_DASHBOARD_URL)
        model.addAttribute("informationCommissionersOfficeUrl", INFORMATION_COMMISSIONERS_OFFICE_URL)
        model.addAttribute("dataProtectionCommunitiesEmails", DATA_PROTECTION_COMMUNITIES_EMAILS)
        model.addAttribute("dataProtectionOfficerEmail", DPO_COMMUNITIES_EMAILS)
        return "localAuthorityPrivacyNotice"
    }

    companion object {
        const val LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE = "/$LOCAL_AUTHORITY_PATH_SEGMENT/$PRIVACY_NOTICE_PATH_SEGMENT"
    }
}
