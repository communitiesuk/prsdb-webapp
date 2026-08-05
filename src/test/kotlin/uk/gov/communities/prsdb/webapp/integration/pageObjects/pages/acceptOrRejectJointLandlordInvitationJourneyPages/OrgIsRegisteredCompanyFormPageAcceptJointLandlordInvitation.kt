package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

class OrgIsRegisteredCompanyFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}") {
    val form = OrgIsRegisteredCompanyForm(page)

    fun submitYes() {
        form.companiesHouseRadios.selectValue("true")
        form.submit()
    }

    class OrgIsRegisteredCompanyForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val companiesHouseRadios = Radios(locator)
    }
}
