package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.controllers.NoLongerALandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.noLongerALandlordJourneyPages.ConfirmationPageNoLongerALandlord
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import kotlin.test.assertEquals

class NoLongerALandlordJourneyTests : IntegrationTestWithMutableData("data-mockuser-landlord-with-sole-and-joint-properties.sql") {
    private val jointPropertyOwnershipId = 2L
    private val solePropertyOwnershipId = 1L

    @BeforeEach
    fun enableJointLandlordsFlag() {
        FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `confirming reaches the confirmation page`(page: Page) {
        val confirmPage = navigator.goToNoLongerALandlordConfirmPage(jointPropertyOwnershipId)
        assertThat(confirmPage.heading).containsText("3 Imaginary Street")
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageNoLongerALandlord::class,
                mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()),
            )
        BaseComponent.assertThat(confirmationPage.confirmationBanner)
            .containsText("No longer registered as a landlord for 3 Imaginary Street")

        confirmationPage.goToDashboardButton.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `a landlord on a property without joint landlords receives a 404`() {
        val response = navigator.navigate(NoLongerALandlordController.getNoLongerALandlordPath(solePropertyOwnershipId))
        assertEquals(404, response?.status())
    }
}
