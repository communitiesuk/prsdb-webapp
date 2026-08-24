package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.delegateToLettingAgentJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageDelegateToLettingAgent(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        DelegateToLettingAgentController.getDelegateToLettingAgentBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val invitedEmailAddress = page.locator("#invited-email-address")
    val propertyAddress = page.locator("#property-address")
    val goBackToPropertyRecordLink = Link(page.locator("#go-back-to-property-record"))
}
