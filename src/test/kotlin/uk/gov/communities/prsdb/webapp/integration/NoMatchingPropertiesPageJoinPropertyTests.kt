package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages.FindPropertyPageJoinProperty
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages.NoMatchingPropertiesPageJoinProperty
import kotlin.test.Test
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat as playwrightAssertThat

class NoMatchingPropertiesPageJoinPropertyTests : IntegrationTestWithImmutableData("data-mockuser-landlord-with-properties.sql") {
    @Test
    fun `the no matching properties page loads for an authenticated landlord`(page: Page) {
        val noMatchingPage = navigator.goToNoMatchingPropertiesPageJoinProperty()
        assertPageIs(page, NoMatchingPropertiesPageJoinProperty::class)
    }

    @Test
    fun `the page displays the correct heading`(page: Page) {
        navigator.goToNoMatchingPropertiesPageJoinProperty()
        playwrightAssertThat(page.locator("h1")).containsText("We could not find any matching properties")
    }

    @Test
    fun `the page displays the postcode and house name from the search`(page: Page) {
        navigator.goToNoMatchingPropertiesPageJoinProperty()
        playwrightAssertThat(page.locator("#page-content")).containsText("ZZ99 9ZZ")
        playwrightAssertThat(page.locator("#page-content")).containsText("1")
    }

    @Test
    fun `the search again link navigates back to the find property page`(page: Page) {
        val noMatchingPage = navigator.goToNoMatchingPropertiesPageJoinProperty()
        assertThat(noMatchingPage.searchAgainLink).isVisible()
        noMatchingPage.searchAgainLink.clickAndWait()
        assertPageIs(page, FindPropertyPageJoinProperty::class)
    }

    @Test
    fun `the find by PRN link is visible`(page: Page) {
        val noMatchingPage = navigator.goToNoMatchingPropertiesPageJoinProperty()
        assertThat(noMatchingPage.findByPrnLink).isVisible()
    }

    @Test
    fun `the back link navigates to the find property page`(page: Page) {
        val noMatchingPage = navigator.goToNoMatchingPropertiesPageJoinProperty()
        assertThat(noMatchingPage.backLink).isVisible()
        noMatchingPage.backLink.clickAndWait()
        assertPageIs(page, FindPropertyPageJoinProperty::class)
    }
}
