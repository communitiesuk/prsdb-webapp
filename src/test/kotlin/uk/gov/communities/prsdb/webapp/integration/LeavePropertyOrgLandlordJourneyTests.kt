package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.leavePropertyJourneyPages.ConfirmationPageLeaveProperty

@WithOrgLandlordProfile
class LeavePropertyOrgLandlordJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val jointPropertyOwnershipId = 48L

    @Test
    fun `An org landlord can leave a joint property and reach the confirmation page`(page: Page) {
        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)
        assertThat(confirmPage.heading).containsText("Joint Org House")
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageLeaveProperty::class,
                mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()),
            )
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("No longer registered as a landlord for Joint Org House")

        confirmationPage.goToDashboardLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }
}
