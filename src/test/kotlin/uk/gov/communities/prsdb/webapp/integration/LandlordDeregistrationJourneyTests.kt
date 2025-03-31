package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages.ReasonFormPageLandlordDeregistration

class LandlordDeregistrationJourneyTests : IntegrationTest() {
    @Nested
    @Sql("/data-mockuser-landlord-with-properties.sql")
    inner class LandlordWithProperties {
        @Test
        fun `User with properties can navigate the whole journey`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            assertThat(areYouSurePage.form.fieldsetHeading)
                .containsText("Are you sure you want to delete your account and all your properties on the database?")
            areYouSurePage.submitWantsToProceed()

            val reasonPage = assertPageIs(page, ReasonFormPageLandlordDeregistration::class)
            reasonPage.form.submit()

            // TODO PRSD-707 - redirect to confirmation page
            assertTrue(
                areYouSurePage.page
                    .url()
                    .toString()
                    .contains("register-as-a-landlord"),
            )
        }

        @Test
        fun `User is returned to the landlord details page if they submit No`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.submitDoesNotWantToProceed()

            assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `User is returned to the landlord details page if they click the back link`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.backLink.clickAndWait()
            assertPageIs(page, LandlordDetailsPage::class)
        }

        @Test
        fun `Submitting with no option selected returns an error`() {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.form.submit()
            assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete your landlord record and properties")
        }
    }

    @Nested
    @Sql("/data-unverified-landlord.sql")
    inner class LandlordWithoutProperties {
        @Test
        fun `User with no properties can navigate the whole journey`(page: Page) {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            assertThat(areYouSurePage.form.fieldsetHeading).containsText("Are you sure you want to delete your account from the database?")
            areYouSurePage.submitWantsToProceed()

            // TODO PRSD-705 - redirect to confirmation page if user with no properties selects "yes" val nextPage =
            assertTrue(
                areYouSurePage.page
                    .url()
                    .toString()
                    .contains("register-as-a-landlord"),
            )

            // Check they can no longer access the landlord dashboard
            val landlordDashboard = navigator.goToLandlordDashboard()
            assertTrue(landlordDashboard.page.content().contains("You do not have permission to access this page"))
        }

        @Test
        fun `Submitting with no option selected returns an error`() {
            val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
            areYouSurePage.form.submit()
            assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
                .containsText("Select whether you want to delete your account from the database")
        }
    }
}
