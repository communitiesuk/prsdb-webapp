package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep

class OrgIsRegisteredCharityFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}",
    ) {
    val form = OrgIsRegisteredCharityForm(page)

    fun submitNo() {
        form.charityRadios.selectValue("false")
        form.submit()
    }

    class OrgIsRegisteredCharityForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val charityRadios = Radios(locator)
    }
}
