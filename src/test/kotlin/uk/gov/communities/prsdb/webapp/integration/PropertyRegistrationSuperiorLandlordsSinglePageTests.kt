package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AnyoneInOwnershipChainFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordsListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import kotlin.test.assertEquals

class PropertyRegistrationSuperiorLandlordsSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class AnyoneInOwnershipChainStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.form.submit()
            assertThat(anyoneInOwnershipChainPage.form.getErrorMessage())
                .containsText("Select if there is anyone else in the ownership chain")
        }

        // TODO PDJB-1961: update the expected page when the real freehold type page is added
        @Test
        fun `Back link returns to the freehold type page when freeholders were not asked about`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.backLink.clickAndWait()
            assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
        }

        @Test
        fun `Back link returns to the freeholders list page when freeholders were asked about`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage(afterFreeholders = true)
            anyoneInOwnershipChainPage.backLink.clickAndWait()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ProvideSuperiorLandlordDetailsStep {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val provideDetailsPage = navigator.skipToPropertyRegistrationSuperiorLandlordProvideDetailsPage()
            provideDetailsPage.form.submit()
            assertThat(provideDetailsPage.form.getErrorMessage())
                .containsText("Select if you can provide the details of this person in the ownership chain")
        }

        @Test
        fun `Back link returns to the anyone in ownership chain page when adding the first superior landlord`(page: Page) {
            val provideDetailsPage = navigator.skipToPropertyRegistrationSuperiorLandlordProvideDetailsPage()
            provideDetailsPage.backLink.clickAndWait()
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class)
        }

        @Test
        fun `Back link returns to the superior landlords list page when superior landlords have already been added`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.addAnotherButton.click()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).backLink.clickAndWait()
            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
        }

        @Test
        fun `Adding a new superior landlord does not prefill an answer`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.addAnotherButton.click()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
            assertThat(page.locator("input[name='provideDetails']:checked")).hasCount(0)
        }

        @Test
        fun `Returning to the page when adding a superior landlord prefills the answer just given`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.addAnotherButton.click()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).backLink.clickAndWait()

            val provideDetailsPage = assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
            assertEquals("true", provideDetailsPage.form.radios.selectedValue)
        }

        @Test
        fun `Changing a superior landlord prefills their existing answer`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage
                .getSummaryCard(2)
                .getAction("Change")
                .link
                .clickAndWait()

            val provideDetailsPage = assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
            assertEquals("false", provideDetailsPage.form.radios.selectedValue)
        }

        @Test
        fun `Returning to the page when changing a superior landlord prefills the answer just given`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage
                .getSummaryCard(2)
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).backLink.clickAndWait()

            val provideDetailsPage = assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
            assertEquals("true", provideDetailsPage.form.radios.selectedValue)
        }
    }

    @Nested
    inner class SuperiorLandlordNameStep {
        @Test
        fun `The heading asks for the person's name`() {
            val superiorLandlordNamePage = navigator.skipToPropertyRegistrationSuperiorLandlordNamePage()
            assertThat(superiorLandlordNamePage.heading).containsText("What is this person’s name?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val superiorLandlordNamePage = navigator.skipToPropertyRegistrationSuperiorLandlordNamePage()
            superiorLandlordNamePage.submitName("")
            assertThat(superiorLandlordNamePage.form.getErrorMessage()).containsText("Enter a name")
        }

        @Test
        fun `Back link returns to the provide details page`(page: Page) {
            val superiorLandlordNamePage = navigator.skipToPropertyRegistrationSuperiorLandlordNamePage()
            superiorLandlordNamePage.backLink.clickAndWait()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
        }

        @Test
        fun `Adding a new superior landlord does not prefill a name`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.addAnotherButton.click()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()

            val superiorLandlordNamePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            assertThat(superiorLandlordNamePage.form.nameInput).hasValue("")
        }

        // TODO PDJB-1974: check the back link still works when the real contact type page is added
        @Test
        fun `Returning to the page when adding a superior landlord prefills the name just given`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.addAnotherButton.click()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).submitName("New superior landlord")
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class).backLink.clickAndWait()

            val superiorLandlordNamePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            assertThat(superiorLandlordNamePage.form.nameInput).hasValue("New superior landlord")
        }

        @Test
        fun `Changing a superior landlord prefills their existing name`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage
                .getSummaryCard(1)
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()

            val superiorLandlordNamePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            assertThat(superiorLandlordNamePage.form.nameInput).hasValue("Superior Landlord With Email")
        }

        // TODO PDJB-1974: check the back link still works when the real contact type page is added
        @Test
        fun `Returning to the page when changing a superior landlord prefills the name just given`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage
                .getSummaryCard(1)
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).submitName("Changed superior landlord")
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class).backLink.clickAndWait()

            val superiorLandlordNamePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            assertThat(superiorLandlordNamePage.form.nameInput).hasValue("Changed superior landlord")
        }
    }

    @Nested
    inner class SuperiorLandlordsListStep {
        @Test
        fun `Superior landlords list shows the number of people in the ownership chain`() {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            assertThat(superiorLandlordsListPage.heading).containsText("You’ve added 3 people in the ownership chain")
        }

        @Test
        fun `The heading uses the singular form when one person in the ownership chain has been added`() {
            val superiorLandlordsListPage =
                navigator.skipToPropertyRegistrationSuperiorLandlordsListPage(
                    superiorLandlords = mapOf(1 to SuperiorLandlordDataModel(detailsKnown = false)),
                )
            assertThat(superiorLandlordsListPage.heading).containsText("You’ve added 1 person in the ownership chain")
        }

        @Test
        fun `Each superior landlord in state is shown as a summary card with their details`() {
            val superiorLandlordsListPage =
                navigator.skipToPropertyRegistrationSuperiorLandlordsListPage(
                    superiorLandlords =
                        mapOf(
                            1 to
                                SuperiorLandlordDataModel(
                                    detailsKnown = true,
                                    name = "Alice Superior",
                                    address = AddressDataModel.fromManualAddressData("2 Example Road", "Exampleton", "EG1 2AB"),
                                ),
                            2 to SuperiorLandlordDataModel(detailsKnown = true, name = "Bob Superior", email = "bob@example.com"),
                            3 to SuperiorLandlordDataModel(detailsKnown = false),
                        ),
                )

            val addressCard = superiorLandlordsListPage.getSummaryCard(0)
            assertThat(addressCard.title).hasText("Alice Superior")
            assertThat(addressCard.summaryList).containsText("Postal address")
            assertThat(addressCard.summaryList).containsText("2 Example Road")
            assertThat(addressCard.summaryList).containsText("EG1 2AB")

            val emailCard = superiorLandlordsListPage.getSummaryCard(1)
            assertThat(emailCard.title).hasText("Bob Superior")
            assertThat(emailCard.summaryList).containsText("Email address")
            assertThat(emailCard.summaryList).containsText("bob@example.com")

            val unknownCard = superiorLandlordsListPage.getSummaryCard(2)
            assertThat(unknownCard.title).hasText("Unknown person")
            assertThat(unknownCard).containsText("you cannot currently provide their details")
        }

        @Test
        fun `Each superior landlord card has change and remove actions`() {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            val card = superiorLandlordsListPage.getSummaryCard(0)
            assertThat(card.getAction("Change")).isVisible()
            assertThat(card.getAction("Remove")).isVisible()
        }

        @Test
        fun `Back link returns to the anyone in ownership chain page`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            superiorLandlordsListPage.backLink.clickAndWait()
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class)
        }
    }
}
