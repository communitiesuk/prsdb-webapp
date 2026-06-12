package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.DeregistrationCheckInvitationsStep

class CheckInvitationsPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        DeregisterPropertyController.getPropertyDeregistrationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${DeregistrationCheckInvitationsStep.ROUTE_SEGMENT}",
    ) {
    val heading
        get() = page.locator("h1")

    val bodyText
        get() = page.locator("main .govuk-body").first()

    val invitationsHeading
        get() = page.locator("main h2")

    val invitationEmails
        get() = page.locator("main .govuk-body.govuk-\\!-margin-bottom-1")

    val continueButton = Button(page.locator("button.govuk-button"))
    val cancelLink = Link.byText(page, "Cancel")

    fun submitContinue() {
        continueButton.clickAndWait()
    }
}
