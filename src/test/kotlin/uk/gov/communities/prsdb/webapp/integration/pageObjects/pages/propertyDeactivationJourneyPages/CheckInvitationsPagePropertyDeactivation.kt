package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckPendingInvitationsStep

class CheckInvitationsPagePropertyDeactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        DeactivatePropertyController.getPropertyDeactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CheckPendingInvitationsStep.ROUTE_SEGMENT}",
    ) {
    val heading
        get() = page.locator("h1")

    val invitationsHeading
        get() = page.locator("main h2")

    val invitationsBody
        get() = page.locator("main h2 + p")

    val invitationEmails
        get() = page.locator("main .govuk-body.govuk-\\!-margin-bottom-1")

    val continueButton = Button(page.locator("button.govuk-button"))
    val cancelLink = Link.byText(page, "Cancel")
    val backLink = BackLink.default(page)

    fun submitContinue() {
        continueButton.clickAndWait()
    }
}
