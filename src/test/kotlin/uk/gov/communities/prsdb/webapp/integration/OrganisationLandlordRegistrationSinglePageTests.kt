package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat

class OrganisationLandlordRegistrationSinglePageTests : IntegrationTestWithImmutableData("data-mockuser-not-landlord.sql") {
    @BeforeEach
    fun enableOrgLandlordFlag() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Nested
    inner class LandlordTypeStep {
        @Test
        fun `the landlord type page renders the caption, heading, partnership details and radio options`(page: Page) {
            val landlordTypePage = navigator.skipToLandlordRegistrationLandlordTypePage()

            assertThat(landlordTypePage.page.locator("#section-header")).containsText("Register as a landlord")
            assertThat(landlordTypePage.page.locator("h1"))
                .containsText("Are you registering as an individual or an organisation?")
            assertThat(landlordTypePage.page.locator(".govuk-details__summary-text"))
                .containsText("Registering a partnership")
            assertThat(landlordTypePage.page.locator("label[for='landlordType-INDIVIDUAL']")).containsText("An individual")
            assertThat(landlordTypePage.page.locator("#landlordType-INDIVIDUAL-hint"))
                .containsText("You rent out a property as an individual, sole trader or joint landlord")
            assertThat(landlordTypePage.page.locator("label[for='landlordType-ORGANISATION']")).containsText("An organisation")
            assertThat(landlordTypePage.page.locator("#landlordType-ORGANISATION-hint"))
                .containsText("You rent out a property as a company, charity or trust")
        }

        @Test
        fun `the legend text is not shown as a header when there is no error`(page: Page) {
            val landlordTypePage = navigator.skipToLandlordRegistrationLandlordTypePage()

            assertThat(landlordTypePage.page.locator(".govuk-fieldset__legend")).hasCount(0)
            assertThat(landlordTypePage.page.locator(".govuk-error-message")).hasCount(0)
        }

        @Test
        fun `submitting with no landlord type selected returns an error`(page: Page) {
            val landlordTypePage = navigator.skipToLandlordRegistrationLandlordTypePage()

            landlordTypePage.form.submit()

            assertThat(landlordTypePage.form.getErrorMessage())
                .containsText("Select if you are registering as an individual or an organisation")
        }
    }

    @Nested
    inner class OrgNameStep {
        @Test
        fun `submitting an empty organisation name returns an error`(page: Page) {
            val orgNamePage = navigator.skipToOrgLandlordRegistrationOrgNamePage()
            orgNamePage.submitName("")
            assertThat(orgNamePage.form.getErrorMessage()).containsText("Enter an organisation name")
        }
    }

    @Nested
    inner class OrgEmailStep {
        @Test
        fun `the org email page renders the heading as a label`(page: Page) {
            val orgEmailPage = navigator.skipToOrgLandlordRegistrationEmailPage()

            assertThat(orgEmailPage.page.locator("h1 label")).containsText("What is your organisation’s email address?")
        }

        @Test
        fun `submitting an empty email address returns an error`(page: Page) {
            val orgEmailPage = navigator.skipToOrgLandlordRegistrationEmailPage()
            orgEmailPage.submitEmail("")
            assertThat(orgEmailPage.form.getErrorMessage())
                .containsText("Enter a valid email address to continue")
        }

        @Test
        fun `submitting an invalid email address returns an error`(page: Page) {
            val orgEmailPage = navigator.skipToOrgLandlordRegistrationEmailPage()
            orgEmailPage.submitEmail("not-an-email")
            assertThat(orgEmailPage.form.getErrorMessage())
                .containsText("Enter an email address in the right format")
        }
    }

    @Nested
    inner class OrgCharityStep {
        @Test
        fun `the org charity page renders the caption, heading, hint and radio options`(page: Page) {
            val orgCharityPage = navigator.skipToOrgLandlordRegistrationOrgCharityPage()

            assertThat(orgCharityPage.form.sectionHeader).containsText("Register as a landlord")
            assertThat(orgCharityPage.form.fieldsetHeading).containsText("Is your organisation a registered charity?")
            assertThat(orgCharityPage.form.hint)
                .containsText(
                    "This includes Charity of Commission of England and Wales, " +
                        "Charity Commission of Northern Ireland and Scottish Charity Regulator",
                )
            assertThat(orgCharityPage.form.yesRadioLabel).containsText("Yes")
            assertThat(orgCharityPage.form.noRadioLabel).containsText("No")
        }

        @Test
        fun `submitting with no option selected returns an error`(page: Page) {
            val orgCharityPage = navigator.skipToOrgLandlordRegistrationOrgCharityPage()

            orgCharityPage.form.submit()

            assertThat(orgCharityPage.form.getErrorMessage())
                .containsText("Select yes if your organisation is a registered charity")
        }
    }
}
