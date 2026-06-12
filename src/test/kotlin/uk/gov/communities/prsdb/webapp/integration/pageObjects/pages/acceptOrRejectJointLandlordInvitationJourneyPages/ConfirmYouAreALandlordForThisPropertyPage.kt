package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ConfirmYouAreALandlordForThisPropertyStep

class ConfirmYouAreALandlordForThisPropertyPage(
    page: Page,
) : BasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ConfirmYouAreALandlordForThisPropertyStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
    val propertyAddress = page.locator("main p:has(span)")
    val successBanner = page.locator(".govuk-notification-banner--success")
}
