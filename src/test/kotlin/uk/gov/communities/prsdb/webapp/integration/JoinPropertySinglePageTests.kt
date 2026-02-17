package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent

class JoinPropertySinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the join property start page renders correctly with all expected content`(page: Page) {
        val joinPropertyStartPage = navigator.goToJoinPropertyStartPage()

        // Verify heading
        BaseComponent.assertThat(joinPropertyStartPage.heading).containsText("Join a registered property as a joint landlord")

        // Verify inset text
        assertThat(joinPropertyStartPage.insetText).containsText("If a property has multiple landlords")

        // Verify details component
        assertThat(joinPropertyStartPage.detailsSummary).containsText("Other ways to confirm you’re a landlord")

        // TODO: PDJB-274 - Click continue button and verify navigation to next step
        assertThat(joinPropertyStartPage.continueButton.locator).isVisible()
    }
}
