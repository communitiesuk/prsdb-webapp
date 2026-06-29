package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.leavePropertyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.stepConfig.ConfirmStep

class ConfirmPageLeaveProperty(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LeavePropertyController.getLeavePropertyBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ConfirmStep.ROUTE_SEGMENT}",
    ) {
    val heading
        get() = page.locator("h1")

    val bodyText
        get() = page.locator("main .govuk-body").first()

    val warningText
        get() = page.locator(".govuk-warning-text__text")

    val confirmButton = Button(page.locator("button.govuk-button--warning"))
    val cancelLink = Link.byText(page, "Cancel")
    val backLink = BackLink.default(page)

    fun submitConfirm() {
        confirmButton.clickAndWait()
    }
}
