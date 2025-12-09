package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController

class FeatureFourEnabledPage(
    page: Page,
) : FeatureFlaggedEndpointBasePage(page, ExampleFeatureFlagTestController.FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE)
