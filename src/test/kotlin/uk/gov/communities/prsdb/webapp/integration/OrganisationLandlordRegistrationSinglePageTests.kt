package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
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
    inner class OrgTypeStep {
        @Test
        fun `submitting with nothing selected returns an error`(page: Page) {
            val orgTypePage = navigator.skipToLandlordRegistrationOrganisationTypePage()

            orgTypePage.form.submit()

            assertThat(orgTypePage.form.getErrorMessage())
                .containsText("Select the types of organisation that apply, or select ‘None of these’")
        }

        @Test
        fun `submitting None with another option returns an error`(page: Page) {
            val orgTypePage = navigator.skipToLandlordRegistrationOrganisationTypePage()

            orgTypePage.selectCompany()
            orgTypePage.selectNoneOfThese()
            orgTypePage.form.submit()

            assertThat(orgTypePage.form.getErrorMessage())
                .containsText("Select the types of organisation that apply, or select ‘None of these’")
        }
    }

    @Nested
    inner class OrgCompaniesHouseStep {
        @Test
        fun `the companies house page renders the heading and yes no radio options`(page: Page) {
            val companiesHousePage = navigator.skipToLandlordRegistrationOrganisationCompaniesHousePage()

            assertThat(companiesHousePage.form.fieldsetHeading)
                .containsText("Is your organisation registered with Companies House?")
            assertThat(companiesHousePage.form.yesLabel).containsText("Yes")
            assertThat(companiesHousePage.form.noLabel).containsText("No")
        }

        @Test
        fun `submitting with no option selected returns a validation error`(page: Page) {
            val companiesHousePage = navigator.skipToLandlordRegistrationOrganisationCompaniesHousePage()

            companiesHousePage.form.submit()

            assertThat(companiesHousePage.form.getErrorMessage())
                .containsText("Select yes if your organisation is registered with Companies House")
        }
    }

    @Nested
    inner class OrgCharityRegisteredWithStep {
        @Test
        fun `the charity registered with page renders the heading, details component and radio options`(page: Page) {
            val charityRegisteredWithPage = navigator.skipToOrgLandlordRegistrationCharityRegisteredWithPage()

            assertThat(charityRegisteredWithPage.heading)
                .containsText("Who is your charity registered with?")
            assertThat(charityRegisteredWithPage.detailsSummary)
                .containsText("Your organisation is registered with more than one charity regulator")
            assertThat(charityRegisteredWithPage.detailsText)
                .containsText("You only need to provide one charity registration number")
            assertThat(charityRegisteredWithPage.getRadioLabel(CharityRegulator.ENGLAND_AND_WALES))
                .containsText("Charities Commission of England and Wales")
            assertThat(charityRegisteredWithPage.getRadioLabel(CharityRegulator.NORTHERN_IRELAND))
                .containsText("Charities Commission of Northern Ireland")
            assertThat(charityRegisteredWithPage.getRadioLabel(CharityRegulator.SCOTLAND))
                .containsText("Scottish Charity Regulator")
            assertThat(charityRegisteredWithPage.radiosDivider)
                .containsText("or")
            assertThat(charityRegisteredWithPage.getRadioLabel(CharityRegulator.NONE))
                .containsText("None of these")
        }

        @Test
        fun `submitting with no option selected returns an error`(page: Page) {
            val charityRegisteredWithPage = navigator.skipToOrgLandlordRegistrationCharityRegisteredWithPage()

            charityRegisteredWithPage.form.submit()

            assertThat(charityRegisteredWithPage.form.getErrorMessage())
                .containsText("Select the charity commission your organisation is registered with or")
        }
    }
}
