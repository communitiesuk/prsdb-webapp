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
        assertThat(joinPropertyStartPage.detailsSummary).containsText("Other ways to confirm you")

        // TODO: PDJB-274 - Click continue button and verify navigation to next step
        assertThat(joinPropertyStartPage.continueButton.locator).isVisible()
    }

    @Test
    fun `the select property page renders correctly with heading and radio buttons`(page: Page) {
        val selectPropertyPage = navigator.skipToSelectPropertyPage()

        // Verify heading
        BaseComponent.assertThat(selectPropertyPage.heading).containsText("Select a property")

        // Verify hint text contains property count info
        assertThat(selectPropertyPage.hintText).containsText("properties found")

        // Verify radio buttons are present (5 addresses match postcode EG1 2AA in seed data)
        assertThat(selectPropertyPage.radioButtons).hasCount(5)

        // Verify search again link is present
        assertThat(selectPropertyPage.searchAgainLink).isVisible()

        // Verify details component for "not listed"
        assertThat(selectPropertyPage.detailsSummary).containsText("The property I'm looking for is not listed")

        // Verify continue button
        assertThat(selectPropertyPage.continueButton.locator).isVisible()
    }

    @Test
    fun `the select property page shows validation error when no property selected`(page: Page) {
        val selectPropertyPage = navigator.skipToSelectPropertyPage()

        // Click continue without selecting a property
        selectPropertyPage.continueButton.clickAndWait()

        // Verify error summary appears
        val errorSummary = page.locator(".govuk-error-summary")
        assertThat(errorSummary).isVisible()
        assertThat(errorSummary).containsText("Select the property you want to join")
    }
}
