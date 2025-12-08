package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class FeatureFlaggedServiceTestPage(
    page: Page,
) : BasePage(page, FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE) {
    val heading = Heading(page.locator(".govuk-heading-l"))
}
