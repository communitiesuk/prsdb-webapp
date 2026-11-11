package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ACCESSIBILITY_STATEMENT_URL
import uk.gov.communities.prsdb.webapp.constants.WITH_BACK_URL_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import java.util.regex.Pattern

class FooterTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the cookies link points to the cookies page`() {
        val dashboard = navigator.goToLandlordDashboard()

        val cookiesHrefPattern = Pattern.compile("^$COOKIES_ROUTE\\?$WITH_BACK_URL_PARAMETER_NAME=\\d+$")

        assertThat(dashboard.footer.cookiesLink).hasAttribute("href", cookiesHrefPattern)
    }

    @Test
    fun `the accessibility statement link points to the accessibility statement`() {
        val dashboard = navigator.goToLandlordDashboard()
        assertThat(dashboard.footer.accessibilityStatementLink).hasAttribute("href", ACCESSIBILITY_STATEMENT_URL)
    }
}
