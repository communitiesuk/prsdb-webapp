package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.featureFlaggedExamplePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ExampleFeatureFlagTestController

class FeatureFourDisabledPage(
    page: Page,
) : FeatureFlaggedEndpointBasePage(page, ExampleFeatureFlagTestController.INVERSE_FEATURE_FLAGGED_ENDPOINT_WITH_RELEASE_DATE)
