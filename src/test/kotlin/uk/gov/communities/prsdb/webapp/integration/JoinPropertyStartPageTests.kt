package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent

class JoinPropertyStartPageTests : IntegrationTest() {
    @Test
    fun `the join property start page renders with correct heading`(page: Page) {
        val joinPropertyStartPage = navigator.goToJoinPropertyStartPage()
        BaseComponent.assertThat(joinPropertyStartPage.heading).containsText("Join a registered property as a joint landlord")
    }

    @Test
    fun `the join property start page displays inset text`(page: Page) {
        val joinPropertyStartPage = navigator.goToJoinPropertyStartPage()
        assertThat(joinPropertyStartPage.insetText).containsText("If a property has multiple landlords")
    }

    @Test
    fun `the join property start page displays the details component`(page: Page) {
        val joinPropertyStartPage = navigator.goToJoinPropertyStartPage()
        assertThat(joinPropertyStartPage.detailsSummary).containsText("Other ways to confirm you're a landlord")
    }

    @Test
    fun `the join property start page has a continue button`(page: Page) {
        val joinPropertyStartPage = navigator.goToJoinPropertyStartPage()
        assertThat(joinPropertyStartPage.continueButton.locator).isVisible()
    }
}
