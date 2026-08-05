package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

class OrgCompanyNumberFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgCompanyNumberStep.ROUTE_SEGMENT}") {
    val form = OrgCompanyNumberForm(page)

    fun submitCompanyNumber(companyNumber: String) {
        form.companyNumberInput.fill(companyNumber)
        form.submit()
    }

    class OrgCompanyNumberForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val companyNumberInput = TextInput.textByFieldName(locator, "companyNumber")
    }
}
