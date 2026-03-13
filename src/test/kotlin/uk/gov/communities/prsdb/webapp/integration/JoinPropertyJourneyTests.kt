package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages.FindPropertyPageJoinProperty
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.joinPropertyJourneyPages.SelectPropertyPage

class JoinPropertyJourneyTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `User can navigate from start page to select property page`(page: Page) {
        // Start page
        val joinPropertyStartPage = navigator.goToJoinPropertyStartPage()
        BaseComponent.assertThat(joinPropertyStartPage.heading).containsText("Join a registered property as a joint landlord")
        assertThat(joinPropertyStartPage.insetText).containsText("If a property has multiple landlords")
        assertThat(joinPropertyStartPage.detailsSummary).containsText("Other ways to confirm you")

        // Navigate to find property page
        joinPropertyStartPage.continueButton.clickAndWait()
        val findPropertyPage = assertPageIs(page, FindPropertyPageJoinProperty::class)

        // Search for a property
        findPropertyPage.form.postcodeInput.fill("EG1 2AA")
        findPropertyPage.form.houseNameOrNumberInput.fill("1")
        findPropertyPage.form.submit()
        val selectPropertyPage = assertPageIs(page, SelectPropertyPage::class)

        // Verify select property page content
        BaseComponent.assertThat(selectPropertyPage.heading).containsText("Select a property")
        assertThat(selectPropertyPage.hintText).containsText("properties found")
        assertThat(selectPropertyPage.radioButtons).hasCount(5)
        assertThat(selectPropertyPage.searchAgainLink).isVisible()
        assertThat(selectPropertyPage.detailsSummary).containsText("The property I'm looking for is not listed")
    }
}
