package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgTypeFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}") {
    val form = OrgTypeForm(page)

    fun submitCompany() {
        form.orgTypeCheckboxes.checkCheckbox(OrgType.COMPANY.toString())
        form.submit()
    }

    class OrgTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val orgTypeCheckboxes = Checkboxes(locator)
    }
}
