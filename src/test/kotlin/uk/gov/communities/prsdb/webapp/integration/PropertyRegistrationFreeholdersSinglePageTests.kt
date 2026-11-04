package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersInfoFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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
