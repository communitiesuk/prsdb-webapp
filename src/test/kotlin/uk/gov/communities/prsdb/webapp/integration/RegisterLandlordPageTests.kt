package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel
import java.net.URI
import java.time.LocalDate

@Sql("/data-mockuser-not-landlord.sql")
class RegisterLandlordPageTests : IntegrationTest() {
    @Test
    fun `registerAsALandlord page renders`(page: Page) {
        page.navigate("http://localhost:$port/register-as-a-landlord")
        assertThat(page.locator("h1")).containsText("Private Rented Sector Database") // h1 instead
    }

    @Test
    fun `the 'Start Now' button directs an unverified user to the landlord registration email page`(page: Page) {
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)

        page.navigate("http://localhost:$port/register-as-a-landlord")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Start Now")).click()
        assertEquals("/register-as-a-landlord/name", URI(page.url()).path)
    }

    @Test
    fun `the 'Start Now' button directs a verified user to the identity confirmation page`(page: Page) {
        val verifiedIdentityMap =
            mutableMapOf<String, Any?>(
                VerifiedIdentityModel.NAME_KEY to "name",
                VerifiedIdentityModel.BIRTH_DATE_KEY to LocalDate.now(),
            )
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentityMap)

        page.navigate("http://localhost:$port/register-as-a-landlord")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Start Now")).click()
        assertEquals("/register-as-a-landlord/confirm-identity", URI(page.url()).path)
    }

    @Test
    @Sql("/data-local.sql")
    fun `the 'Start Now' button directs a registered landlord to the landlord dashboard page`(page: Page) {
        val startPage = navigator.goToLandlordRegistrationStartPage()
        startPage.startButton.clickAndWait()
        val dashboardPage = assertPageIs(page, LandlordDashboardPage::class)
        BaseComponent.assertThat(dashboardPage.dashboardBannerHeading).containsText("Alexander Smith")
    }
}
