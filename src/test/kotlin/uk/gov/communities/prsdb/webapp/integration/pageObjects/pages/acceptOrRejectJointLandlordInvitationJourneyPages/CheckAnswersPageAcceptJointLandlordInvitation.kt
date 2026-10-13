package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

class CheckAnswersPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${AbstractCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading
        get() =
            if (page.locator(".govuk-summary-card").count() > 0) {
                "Check your organisation registration answers"
            } else {
                "Check your landlord registration answers"
            }

    val form = PostForm(page)

    fun confirmAndSubmit() = form.submit()
}
