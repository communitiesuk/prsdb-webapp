package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController

class FeatureThreeEnabledPage(
    page: Page,
) : FeatureFlaggedEndpointBasePage(page, ExampleFeatureFlagTestController.FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
