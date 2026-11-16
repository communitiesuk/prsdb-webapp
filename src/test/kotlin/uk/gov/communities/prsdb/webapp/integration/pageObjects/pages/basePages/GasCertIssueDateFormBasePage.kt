package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

open class GasCertIssueDateFormBasePage(
    page: Page,
    urlSegment: String,
) : DateFormPage(page, urlSegment) {
    // TODO PDJB-1926: Use a fixed expectedTitleHeading when GAS_CERTIFICATE_14_MONTH_EXPIRY is permanently enabled.
    override val expectedTitleHeading: String?
        get() =
            when (page.locator("h1").innerText()) {
                "When was this certificate issued?" -> "When was the gas safety certificate issued?"
                else -> null
            }
}
