package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.OrgLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

// TODO PDJB-2149: Delete this test class along with the landlord deregistration journey when the ACTIVE_INACTIVE flag is removed
@WithOrgLandlordProfile
class OrganisationalLandlordDeregistrationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun disableActiveInactive() {
        featureFlagManager.disableFeature(ACTIVE_INACTIVE)
    }

    @Test
    fun `the are you sure page renders the heading`() {
        val areYouSurePage = navigator.goToOrgLandlordDeregistrationAreYouSurePage()

        assertThat(areYouSurePage.heading).containsText("Are you sure you want to delete your organisation?")
    }

    @Test
    fun `clicking the back link returns to the landlord details page`(page: Page) {
        val areYouSurePage = navigator.goToOrgLandlordDeregistrationAreYouSurePage()
        areYouSurePage.backLink.clickAndWait()

        assertPageIs(page, OrgLandlordDetailsPage::class)
    }

    @Test
    fun `clicking the cancel link returns to the landlord details page`(page: Page) {
        val areYouSurePage = navigator.goToOrgLandlordDeregistrationAreYouSurePage()
        areYouSurePage.cancelLink.clickAndWait()

        assertPageIs(page, OrgLandlordDetailsPage::class)
    }
}
