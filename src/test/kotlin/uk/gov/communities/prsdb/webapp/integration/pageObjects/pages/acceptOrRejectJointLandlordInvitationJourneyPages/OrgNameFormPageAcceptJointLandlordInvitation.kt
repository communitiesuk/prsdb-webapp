package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep

class OrgNameFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgNameStep.ROUTE_SEGMENT}") {
    val form = OrgNameForm(page)

    fun submitName(name: String) {
        form.orgNameInput.fill(name)
        form.submit()
    }

    class OrgNameForm(
        page: Page,
    ) : PostForm(page) {
        val orgNameInput = TextInput.textByFieldName(locator, "orgName")
    }
}
