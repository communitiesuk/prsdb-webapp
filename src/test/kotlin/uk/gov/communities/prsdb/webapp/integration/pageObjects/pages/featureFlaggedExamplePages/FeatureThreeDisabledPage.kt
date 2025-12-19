package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController

class FeatureThreeDisabledPage(
    page: Page,
) : FeatureFlaggedEndpointBasePage(page, ExampleFeatureFlagTestController.INVERSE_FEATURE_FLAGGED_GROUPED_ENDPOINT_FLAG_3_ROUTE)
