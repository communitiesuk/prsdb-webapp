package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

class PropertyRegistrationFreeholdersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class FreeholderNameStep {
        @Test
        fun `Joint freeholders see the joint freeholder heading`() {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage(isJointFreeholder = true)
            assertThat(freeholderNamePage.heading).containsText("What is this joint freeholder’s name?")
        }

        @Test
        fun `Leasehold only landlords see the single freeholder heading`() {
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
    inner class FreeholderDetailsRouting {
        @Test
        fun `Answering no to providing details skips the freeholder details`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersPage()
            freeholdersPage.submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class)
                .submitProvideDetails(YesOrNo.NO)

            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Email contact type routes to email and then the freeholders list`(page: Page) {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.submitName("Jane Smith")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
                .submitContactType(FreeholderContactType.EMAIL)
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submit()

            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
        }

        @Test
        fun `Postal address contact type routes to address and then the freeholders list`(page: Page) {
            val freeholderNamePage = navigator.skipToPropertyRegistrationFreeholderNamePage()
            freeholderNamePage.submitName("Jane Smith")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
                .submitContactType(FreeholderContactType.POSTAL_ADDRESS)
            assertPageIs(page, FreeholderAddressFormPagePropertyRegistration::class).submit()

            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
        }
    }
}
