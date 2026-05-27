package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.AcceptOrRejectStep

class AcceptOrRejectPage(
    page: Page,
) : BasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${AcceptOrRejectStep.ROUTE_SEGMENT}") {
    fun acceptInvitation() {
        form.radios.selectValue("true")
        form.submit()
    }

    fun rejectInvitation() {
        form.radios.selectValue("false")
        form.submit()
    }

    val form = FormWithRadios(page)
}
