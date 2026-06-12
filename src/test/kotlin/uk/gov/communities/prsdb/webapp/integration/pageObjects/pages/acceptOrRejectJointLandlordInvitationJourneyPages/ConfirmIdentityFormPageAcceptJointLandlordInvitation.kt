package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep

class ConfirmIdentityFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ConfirmIdentityStep.ROUTE_SEGMENT}",
    ) {
    val form = PostForm(page)

    fun confirm() = form.submit()
}
