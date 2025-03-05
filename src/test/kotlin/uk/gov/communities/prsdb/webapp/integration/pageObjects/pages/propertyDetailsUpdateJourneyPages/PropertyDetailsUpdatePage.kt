package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsUpdatePage(
    page: Page,
) : PropertyDetailsBasePage(
        page,
        "${PropertyDetailsController.getUpdatePropertyDetailsPath(1)}/${UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment}",
    ) {
    val heading = Heading(page.locator("html").locator("main div.moj-page-header-actions h1.govuk-heading-l"))
    val submitButton = Button.byText(page, "TODO: PRSD-355 Confirmation page for update property details")
}
