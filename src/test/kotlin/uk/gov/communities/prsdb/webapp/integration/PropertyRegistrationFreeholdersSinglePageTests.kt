package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnyFreeholdersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

class PropertyRegistrationFreeholdersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class FreeholderNameStep {
        @Test
        fun `Joint freeholders see the joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            assertThat(freeholderNamePage.heading).containsText("What is this joint freeholder’s name?")
        }

        @Test
        fun `Non-joint freeholders see the non-joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage(jointFreeholders = false)
            assertThat(freeholderNamePage.heading).containsText("What is this freeholder’s name?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.submitName("")
            assertThat(freeholderNamePage.form.getErrorMessage()).containsText("Enter a name")
        }

        @Test
        fun `Back link returns to the provide details page`(page: Page) {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.backLink.clickAndWait()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholderContactTypeStep {
        @Test
        fun `The heading includes the freeholder name`() {
            val contactTypePage = navigator.skipToPropertyRegistrationFreeholderContactTypePage(freeholderName = "Test Freeholder")
            assertThat(contactTypePage.heading)
                .containsText("Which contact information can you provide for Test Freeholder?")
        }

        @Test
        fun `Submitting with no option selected returns an error`() {
            val contactTypePage = navigator.skipToPropertyRegistrationFreeholderContactTypePage()
            contactTypePage.form.submit()
            assertThat(contactTypePage.form.getErrorMessage())
                .containsText("Select which contact information you can provide")
        }

        @Test
        fun `Back link returns to the freeholder name page`(page: Page) {
            val contactTypePage = navigator.skipToPropertyRegistrationFreeholderContactTypePage()
            contactTypePage.backLink.clickAndWait()
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholderEmailStep {
        @Test
        fun `heading includes the freeholder name`() {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")

            assertThat(emailPage.heading).containsText("What is James Hassan’s email address?")
        }

        @Test
        fun `submitting an empty email returns the missing email error`() {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")
            emailPage.submitEmail("")

            assertThat(emailPage.form.getErrorMessage()).containsText("Enter an email address")
        }

        @Test
        fun `submitting an invalid email returns the format error`() {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")
            emailPage.submitEmail("not-an-email")

            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter an email address in the correct format, like name@example.com")
        }

        @Test
        fun `submitting an email already used by another freeholder returns the duplicate email error`() {
            val emailPage =
                navigator.skipToPropertyRegistrationFreeholderEmailPage(
                    freeholderName = "James Hassan",
                    existingFreeholders = mapOf(1 to FreeholderDataModel(detailsKnown = true, email = "james@example.com")),
                )
            emailPage.submitEmail("james@example.com")

            assertThat(emailPage.form.getErrorMessage())
                .containsText(
                    "You’ve already used this email address for another freeholder - enter a different email address",
                )
        }

        @Test
        fun `Back link returns to the freeholder contact type page`(page: Page) {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")
            emailPage.backLink.clickAndWait()
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholdersListStep {
        @Test
        fun `Joint freeholders see the joint freeholders heading`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            assertThat(freeholdersListPage.heading).containsText("You’ve added 3 joint freeholders")
        }

        @Test
        fun `Non-joint freeholders see the non-joint freeholders heading`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage(jointFreeholders = false)
            assertThat(freeholdersListPage.heading).containsText("You’ve added 3 freeholders")
        }

        // TODO PDJB-1969: update the expected page when has any freeholders becomes an internal step
        @Test
        fun `Back link returns to the has any freeholders page`(page: Page) {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            freeholdersListPage.backLink.clickAndWait()
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class)
        }
    }
}
