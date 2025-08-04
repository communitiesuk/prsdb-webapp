package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages.updatePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class KeepPropertySafePagePropertyComplianceUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getReviewPropertyComplianceStepPath(
            urlArguments["propertyOwnershipId"]!!.toLong(),
            PropertyComplianceStepId.KeepPropertySafe,
        ),
    ) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
    val returnToRecordButton = Button.byText(page, "Return to property record")
}
