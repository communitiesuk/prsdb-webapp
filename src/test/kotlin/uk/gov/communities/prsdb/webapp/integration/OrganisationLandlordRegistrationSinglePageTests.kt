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
    inner class OrgMainContactStep {
        @Test
        fun `the main contact page renders the heading and the three field labels`(page: Page) {
            val mainContactPage = navigator.skipToOrgLandlordRegistrationMainContactPage()

            assertThat(mainContactPage.pageHeader).containsText("Who is the main contact for your organisation?")
            assertThat(mainContactPage.pageText).containsText("Full name")
            assertThat(mainContactPage.pageEmail).containsText("Email address")
            assertThat(mainContactPage.pagePhoneNumber).containsText("Phone number")
        }

        @Test
        fun `submitting all fields empty returns missing errors for each field`(page: Page) {
            val mainContactPage = navigator.skipToOrgLandlordRegistrationMainContactPage()

            mainContactPage.submit(name = "", email = "", phoneNumber = "")

            val errorSummary = mainContactPage.page.locator(".govuk-error-summary")
            assertThat(errorSummary).containsText("Enter a full name")
            assertThat(errorSummary).containsText("Enter an email address")
            assertThat(errorSummary).containsText("Enter a phone number")
        }

        @Test
        fun `submitting an invalid email returns an email format error`(page: Page) {
            val mainContactPage = navigator.skipToOrgLandlordRegistrationMainContactPage()

            mainContactPage.submit(name = "Jane Doe", email = "not-an-email", phoneNumber = "07123456789")

            assertThat(mainContactPage.page.locator(".govuk-error-summary"))
                .containsText("Enter an email address in the right format")
        }

        @Test
        fun `submitting an invalid phone number returns a phone format error`(page: Page) {
            val mainContactPage = navigator.skipToOrgLandlordRegistrationMainContactPage()

            mainContactPage.submit(name = "Jane Doe", email = "jane@example.com", phoneNumber = "not-a-phone")

            assertThat(mainContactPage.page.locator(".govuk-error-summary"))
                .containsText("Enter a phone number including the country code")
        }

        @Test
        fun `submitting valid details advances past the main contact step`(page: Page) {
            val mainContactPage = navigator.skipToOrgLandlordRegistrationMainContactPage()

            mainContactPage.submit(name = "Jane Doe", email = "jane@example.com", phoneNumber = "07123456789")

            assertThat(mainContactPage.page.locator("h1"))
                .not()
                .containsText("Who is the main contact for your organisation?")
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
    inner class OrgCompanyNumberStep {
        @Test
        fun `the company number page renders the heading, hint, details and input`(page: Page) {
            val companyNumberPage = navigator.skipToLandlordRegistrationOrgCompanyNumberPage()

            assertThat(companyNumberPage.form.sectionHeader).containsText("Register as a landlord")
            assertThat(companyNumberPage.heading)
                .containsText("What is your organisation’s company number?")
            assertThat(companyNumberPage.hint)
                .containsText("Enter the number as shown on the Companies House register")
            assertThat(companyNumberPage.detailsHeading)
                .containsText("Where do I find the company number")
            assertThat(companyNumberPage.form.companyNumberInput.locator).isVisible()
        }

        @Test
        fun `submitting with no company number returns a missing error`(page: Page) {
            val companyNumberPage = navigator.skipToLandlordRegistrationOrgCompanyNumberPage()

            companyNumberPage.form.submit()

            assertThat(companyNumberPage.form.getErrorMessage())
                .containsText("Enter a company number, like 12345678 or 00123456")
        }

        @Test
        fun `submitting a company number with fewer than 8 characters returns a length error`(page: Page) {
            val companyNumberPage = navigator.skipToLandlordRegistrationOrgCompanyNumberPage()

            companyNumberPage.submitCompanyNumber("1234567")

            assertThat(companyNumberPage.form.getErrorMessage())
                .containsText("Company number must be 8 characters, like 12345678 or 00123456")
        }

        @Test
        fun `submitting a company number with more than 8 characters returns a length error`(page: Page) {
            val companyNumberPage = navigator.skipToLandlordRegistrationOrgCompanyNumberPage()

            companyNumberPage.submitCompanyNumber("123456789")

            assertThat(companyNumberPage.form.getErrorMessage())
                .containsText("Company number must be 8 characters, like 12345678 or 00123456")
        }

        @Test
        fun `submitting a company number with invalid characters returns an invalid characters error`(page: Page) {
            val companyNumberPage = navigator.skipToLandlordRegistrationOrgCompanyNumberPage()

            companyNumberPage.submitCompanyNumber("SC12/*1+")

            assertThat(companyNumberPage.form.getErrorMessage())
                .containsText("Company number must only include numbers and letters A to Z")
        }
    }

    @Nested
    inner class OrgCharityStep {
        @Test
        fun `the org charity page renders the caption, heading, hint and radio options`(page: Page) {
            val orgCharityPage = navigator.skipToOrgLandlordRegistrationCharityPage()

            assertThat(orgCharityPage.page.locator("#section-header")).containsText("Register as a landlord")
            assertThat(orgCharityPage.page.locator("h1")).containsText("Is your organisation a registered charity?")
            assertThat(orgCharityPage.page.locator("#charity-hint"))
                .containsText(
                    "This includes Charity of Commission of England and Wales, " +
                        "Charity Commission of Northern Ireland and Scottish Charity Regulator",
                )
            assertThat(orgCharityPage.page.locator("label[for='charity-yes']")).containsText("Yes")
            assertThat(orgCharityPage.page.locator("label[for='charity-no']")).containsText("No")
        }

        @Test
        fun `submitting with no option selected returns an error`(page: Page) {
            val orgCharityPage = navigator.skipToOrgLandlordRegistrationCharityPage()

            orgCharityPage.form.submit()

            assertThat(orgCharityPage.form.getErrorMessage())
                .containsText("Select yes if your organisation is a registered charity")
        }
    }

    @Nested
    inner class OrgCharityRegisteredWithStep {
        @Test
        fun `the charity registered with page renders the heading`(page: Page) {
            val charityRegisteredWithPage = navigator.skipToOrgLandlordRegistrationCharityRegisteredWithPage()

            assertThat(charityRegisteredWithPage.heading)
                .containsText("Who is your charity registered with?")
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
