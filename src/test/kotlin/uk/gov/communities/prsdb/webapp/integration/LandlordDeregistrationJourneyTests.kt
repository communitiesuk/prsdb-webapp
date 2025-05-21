package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.JourneyTestWithSeedData.NestedJourneyTestWithSeedData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.ConfirmationPageLandlordDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.ReasonFormPageLandlordDeregistration

class LandlordDeregistrationJourneyTests : IntegrationTest() {
    @Nested
    inner class LandlordWithProperties : NestedJourneyTestWithSeedData("data-mockuser-landlord-with-properties.sql") {
        @Test
        fun `User with properties can navigate the whole journey`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            assertThat(areYouSurePage.form.fieldsetHeading)
                .containsText("Are you sure you want to delete your account and all your properties on the database?")
            areYouSurePage.submitWantsToProceed()

            val reasonPage = assertPageIs(page, ReasonFormPageLandlordDeregistration::class)
            assertThat(reasonPage.form.fieldsetHeading)
                .containsText("Why are you deleting your account? (optional)")
            reasonPage.form.submit()

            val confirmationPage = assertPageIs(page, ConfirmationPageLandlordDeregistration::class)
            assertThat(confirmationPage.confirmationBanner).containsText("You have deleted your account from the database")
            assertTrue(
                confirmationPage.page
                    .content()
                    .contains("You have deleted your landlord information and all your properties from the database"),
            )

            // Check they can no longer access the landlord dashboard
            val landlordDashboard = navigator.goToLandlordDashboard()
            assertTrue(landlordDashboard.page.content().contains("You do not have permission to access this page"))
        }
    }

    @Nested
    inner class LandlordWithoutProperties : NestedJourneyTestWithSeedData("data-unverified-landlord.sql") {
        @Test
        fun `User with no properties can navigate the whole journey`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            assertThat(areYouSurePage.form.fieldsetHeading).containsText("Are you sure you want to delete your account from the database?")
            areYouSurePage.submitWantsToProceed()

            val confirmationPage = assertPageIs(page, ConfirmationPageLandlordDeregistration::class)
            assertThat(confirmationPage.confirmationBanner).containsText("You have deleted your account from the database")
            assertTrue(confirmationPage.page.content().contains("You have deleted your account from the database"))

            // Check they can no longer access the landlord dashboard
            val landlordDashboard = navigator.goToLandlordDashboard()
            assertTrue(landlordDashboard.page.content().contains("You do not have permission to access this page"))
        }
    }
}
