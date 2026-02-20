package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.FindPropertyPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages.JoinPropertyStartPage
import kotlin.test.Test
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat as playwrightAssertThat

class FindPropertyPageTests : IntegrationTestWithImmutableData("data-mockuser-landlord-with-properties.sql") {
    @Test
    fun `the find property page loads for an authenticated landlord`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        assertPageIs(page, FindPropertyPage::class)
    }

    @Test
    fun `the back link navigates to the join property start page`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        assertThat(findPropertyPage.backLink).isVisible()
        findPropertyPage.backLink.clickAndWait()
        assertPageIs(page, JoinPropertyStartPage::class)
    }

    @Test
    fun `the page displays the correct heading and caption`(page: Page) {
        navigator.goToFindPropertyPage()
        playwrightAssertThat(page.locator("h1")).containsText("Find a property")
        playwrightAssertThat(page.locator(".govuk-caption-l")).containsText("Join a registered property as a joint landlord")
    }

    @Test
    fun `the page displays postcode and property name or number input fields`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        assertThat(findPropertyPage.form.postcodeInput).isVisible()
        assertThat(findPropertyPage.form.houseNameOrNumberInput).isVisible()
    }

    @Test
    fun `the page displays the find property button`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        assertThat(findPropertyPage.form.submitButton).containsText("Find property")
    }

    @Test
    fun `the page displays the alternative PRN link`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        assertThat(findPropertyPage.alternativeLink).containsText("Find using the Property Registration Number instead")
    }

    @Test
    fun `submitting the form with empty fields shows validation errors`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        findPropertyPage.form.submitButton.clickAndWait()

        playwrightAssertThat(page.locator(".govuk-error-summary")).isVisible()
        playwrightAssertThat(page.locator(".govuk-error-summary")).containsText("Enter a postcode")
        playwrightAssertThat(page.locator(".govuk-error-summary")).containsText("Enter a property name or number")
    }

    @Test
    fun `submitting the form with only postcode shows validation error for property name`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        findPropertyPage.form.postcodeInput.fill("SW1A 2AA")
        findPropertyPage.form.submitButton.clickAndWait()

        playwrightAssertThat(page.locator(".govuk-error-summary")).isVisible()
        playwrightAssertThat(page.locator(".govuk-error-summary")).containsText("Enter a property name or number")
    }

    @Test
    fun `submitting the form with only property name shows validation error for postcode`(page: Page) {
        val findPropertyPage = navigator.goToFindPropertyPage()
        findPropertyPage.form.houseNameOrNumberInput.fill("15")
        findPropertyPage.form.submitButton.clickAndWait()

        playwrightAssertThat(page.locator(".govuk-error-summary")).isVisible()
        playwrightAssertThat(page.locator(".govuk-error-summary")).containsText("Enter a postcode")
    }
}
