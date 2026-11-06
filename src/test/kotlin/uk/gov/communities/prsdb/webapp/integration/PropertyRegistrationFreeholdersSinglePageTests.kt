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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersInfoFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

class PropertyRegistrationFreeholdersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class ProvideFreeholderDetailsStep {
        @Test
        fun `Joint freeholders see the joint heading`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage().submit()
            val provideDetailsPage = assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)

            assertThat(provideDetailsPage.heading).containsText("Can you provide this joint freeholder’s details?")
        }

        @Test
        fun `Non-joint freeholders see the non-joint heading`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage(isJointFreeholder = false).submit()
            val provideDetailsPage = assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)

            assertThat(provideDetailsPage.heading).containsText("Can you provide this freeholder’s details?")
        }

        @Test
        fun `Submitting without an answer for a joint freeholder returns the joint validation error`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage().submit()
            val provideDetailsPage = assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)

            provideDetailsPage.form.submit()

            assertThat(provideDetailsPage.form.getErrorMessage())
                .containsText("Select if you can provide this joint freeholder’s contact details")
        }

        @Test
        fun `Submitting without an answer for a non-joint freeholder returns the non-joint validation error`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage(isJointFreeholder = false).submit()
            val provideDetailsPage = assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)

            provideDetailsPage.form.submit()

            assertThat(provideDetailsPage.form.getErrorMessage())
                .containsText("Select if you can provide this freeholder’s contact details")
        }

        @Test
        fun `Selecting yes continues to the freeholder name page`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage().submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)
                .submitYes()

            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
        }

        @Test
        fun `Selecting no continues to the freeholders list`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage().submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)
                .submitNo()

            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
        }

        @Test
        fun `Back link returns to the freeholders info page`(page: Page) {
            navigator.skipToPropertyRegistrationFreeholdersInfoPage().submit()
            val provideDetailsPage = assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)

            provideDetailsPage.backLink.clickAndWait()

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholderNameStep {
        @Test
        fun `Joint freeholders see the joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            assertThat(freeholderNamePage.heading).containsText("What is this joint freeholder’s name?")
        }

        @Test
        fun `Non-joint freeholders see the non-joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage(isJointFreeholder = false)
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
        fun `Heading includes the freeholder name`() {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")

            assertThat(emailPage.heading).containsText("What is James Hassan’s email address?")
        }

        @Test
        fun `Submitting an empty email returns the missing email error`() {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")
            emailPage.submitEmail("")

            assertThat(emailPage.form.getErrorMessage()).containsText("Enter an email address")
        }

        @Test
        fun `Submitting an invalid email returns the format error`() {
            val emailPage = navigator.skipToPropertyRegistrationFreeholderEmailPage(freeholderName = "James Hassan")
            emailPage.submitEmail("not-an-email")

            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter an email address in the correct format, like name@example.com")
        }

        @Test
        fun `Submitting an email already used by another freeholder returns the duplicate email error`() {
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
    inner class FreeholderLookupAddressStep {
        @Test
        fun `The heading includes the freeholder name`() {
            val lookupAddressPage = navigator.skipToPropertyRegistrationFreeholderLookupAddressPage(freeholderName = "James Hassan")
            assertThat(lookupAddressPage.heading).containsText("What is James Hassan’s address?")
        }

        @Test
        fun `Back link returns to the freeholder contact type page`(page: Page) {
            val lookupAddressPage = navigator.skipToPropertyRegistrationFreeholderLookupAddressPage()
            lookupAddressPage.backLink.clickAndWait()
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholderSelectAddressStep {
        @Test
        fun `The heading includes the freeholder name`() {
            val selectAddressPage = navigator.skipToPropertyRegistrationFreeholderSelectAddressPage(freeholderName = "James Hassan")
            assertThat(selectAddressPage.heading).containsText("Select James Hassan’s address")
        }
    }

    @Nested
    inner class FreeholderManualAddressStep {
        @Test
        fun `The heading includes the freeholder name`() {
            val manualAddressPage = navigator.skipToPropertyRegistrationFreeholderManualAddressPage(freeholderName = "James Hassan")
            assertThat(manualAddressPage.heading).containsText("What is James Hassan’s address?")
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
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage(isJointFreeholder = false)
            assertThat(freeholdersListPage.heading).containsText("You’ve added 3 freeholders")
        }

        @Test
        fun `The heading uses the singular form when one freeholder has been added`() {
            val freeholdersListPage =
                navigator.skipToPropertyRegistrationFreeholdersListPage(
                    freeholders = mapOf(1 to FreeholderDataModel(detailsKnown = false)),
                )
            assertThat(freeholdersListPage.heading).containsText("You’ve added 1 joint freeholder")
        }

        @Test
        fun `Each freeholder in state is shown as a summary card with their details`() {
            val freeholdersListPage =
                navigator.skipToPropertyRegistrationFreeholdersListPage(
                    freeholders =
                        mapOf(
                            1 to
                                FreeholderDataModel(
                                    detailsKnown = true,
                                    name = "Alice Freeholder",
                                    address = AddressDataModel.fromManualAddressData("2 Example Road", "Exampleton", "EG1 2AB"),
                                ),
                            2 to FreeholderDataModel(detailsKnown = true, name = "Bob Freeholder", email = "bob@example.com"),
                            3 to FreeholderDataModel(detailsKnown = false),
                        ),
                )

            val addressCard = freeholdersListPage.getSummaryCard(0)
            assertThat(addressCard.title).hasText("Alice Freeholder")
            assertThat(addressCard.summaryList).containsText("Postal address")
            assertThat(addressCard.summaryList).containsText("2 Example Road")
            assertThat(addressCard.summaryList).containsText("EG1 2AB")

            val emailCard = freeholdersListPage.getSummaryCard(1)
            assertThat(emailCard.title).hasText("Bob Freeholder")
            assertThat(emailCard.summaryList).containsText("Email address")
            assertThat(emailCard.summaryList).containsText("bob@example.com")

            val unknownCard = freeholdersListPage.getSummaryCard(2)
            assertThat(unknownCard.title).hasText("Unknown freeholder")
            assertThat(unknownCard).containsText("you cannot currently provide their details")
        }

        @Test
        fun `Each freeholder card has change and remove actions`() {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            val card = freeholdersListPage.getSummaryCard(0)
            assertThat(card.getAction("Change")).isVisible()
            assertThat(card.getAction("Remove")).isVisible()
        }

        @Test
        fun `Back link returns to the freeholders page`(page: Page) {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            freeholdersListPage.backLink.clickAndWait()
            // TODO PDJB-1962: update this usage when the real freeholders intro page is added
            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class)
        }
    }
}
