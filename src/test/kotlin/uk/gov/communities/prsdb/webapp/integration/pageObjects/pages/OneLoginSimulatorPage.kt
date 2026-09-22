package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class OneLoginSimulatorPage(
    private val page: Page,
) {
    val vtrValue: String
        get() = getAuthorizationRequestParameter("vtr")

    val claimsValue: String
        get() = getAuthorizationRequestParameter("claims")

    fun submitSubject(subject: String) {
        page.locator("[data-testid='sub']").fill(subject)
        page.locator("button[name='continue']").click()
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
        page.locator("button[name='continue']").click()
    }

    private fun getAuthorizationRequestParameter(name: String): String {
        val query = URI.create(page.url()).rawQuery ?: error("Simulator URL has no authorization request parameters")
        return query
            .split("&")
            .map { it.split("=", limit = 2) }
            .firstOrNull { it[0] == name }
            ?.getOrNull(1)
            ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8) }
            ?: error("Simulator authorization request has no $name parameter")
    }
}
