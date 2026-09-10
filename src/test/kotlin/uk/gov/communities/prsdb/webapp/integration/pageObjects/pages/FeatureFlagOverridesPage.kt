package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.FeatureFlagOverrideController.Companion.FEATURE_FLAG_OVERRIDES_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class FeatureFlagOverridesPage(
    page: Page,
) : BasePage(page, FEATURE_FLAG_OVERRIDES_ROUTE) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
    val saveButton = Button.byText(page, "Save overrides")
    val resetButton = Button.byText(page, "Reset all overrides")
    val buttonGroup: Locator = page.locator(".govuk-button-group")

    fun flagRadios(flagName: String) = Radios(page.locator("#flag-$flagName"))

    fun releaseRadios(releaseName: String) = Radios(page.locator("#release-$releaseName"))

    fun flagHint(flagName: String): Locator = page.locator("#flag-$flagName-hint")
}
