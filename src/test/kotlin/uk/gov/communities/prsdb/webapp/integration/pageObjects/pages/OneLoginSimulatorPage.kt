package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page

class OneLoginSimulatorPage(
    private val page: Page,
) {
    val vtrValue: String
        get() = page.locator("input[name='vtr']").inputValue()

    val claimsValue: String
        get() = page.locator("input[name='claims']").inputValue()

    fun submitSubject(subject: String) {
        page.locator("[data-testid='sub']").fill(subject)
        page.locator("button[type='submit']").click()
    }

    fun submitIdentityVerificationFixture(
        coreIdentity: String,
        address: String,
        returnCodes: String,
    ) {
        page.locator("[data-testid='sub']").fill("urn:fdc:gov.uk:2022:UVWXY")
        page.locator("[data-testid='core-identity-vc']").fill(coreIdentity)
        page.locator("[data-testid='postal-address-details']").fill(address)
        page.locator("[data-testid='return-codes']").fill(returnCodes)
        page.locator("button[type='submit']").click()
    }
}
