package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController.Companion.FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE

class FeatureFlaggedServiceTestPage(
    page: Page,
) : FeatureFlaggedEndpointBasePage(page, FEATURED_FLAGGED_SERVICE_TEST_URL_ROUTE)
