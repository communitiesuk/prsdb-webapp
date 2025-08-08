package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CheckAnswersPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ConfirmIdentityFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.IdentityNotVerifiedFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NoAddressFoundFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.NoContactAddressFoundFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.local.api.MockOSPlacesAPIResponses
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel
import uk.gov.communities.prsdb.webapp.testHelpers.extensions.getFormattedInternationalPhoneNumber
import java.time.LocalDate

class LandlordRegistrationSinglePageTests : SinglePageTestWithSeedData("data-mockuser-not-landlord.sql") {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @Nested
    inner class LandlordRegistrationStartPage {
        @Test
        fun `registerAsALandlord page renders`(page: Page) {
            val landlordRegistrationStartPage = navigator.goToLandlordRegistrationStartPage()
            BaseComponent.assertThat(landlordRegistrationStartPage.heading).containsText("Private Rented Sector (PRS) Database")
        }

        @Test
        fun `the 'Start Now' button directs an unverified user to the landlord registration identity not verified page`(page: Page) {
            whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)
            val landlordRegistrationStartPage = navigator.goToLandlordRegistrationStartPage()
            landlordRegistrationStartPage.startButton.clickAndWait()
            assertPageIs(page, IdentityNotVerifiedFormPageLandlordRegistration::class)
        }

        @Test
        fun `the 'Start Now' button directs a verified user to the identity confirmation page`(page: Page) {
            val verifiedIdentityMap =
                mutableMapOf<String, Any?>(
                    VerifiedIdentityModel.NAME_KEY to "name",
                    VerifiedIdentityModel.BIRTH_DATE_KEY to LocalDate.now(),
                )
            whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentityMap)

            val landlordRegistrationStartPage = navigator.goToLandlordRegistrationStartPage()
            landlordRegistrationStartPage.startButton.clickAndWait()
            assertPageIs(page, ConfirmIdentityFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class AlreadyRegistered : NestedSinglePageTestWithSeedData("data-local.sql") {
        @Test
        fun `the 'Start Now' button directs a registered landlord to the landlord dashboard page`(page: Page) {
            val startPage = navigator.goToLandlordRegistrationStartPage()
            startPage.startButton.clickAndWait()
            val dashboardPage = assertPageIs(page, LandlordDashboardPage::class)
            BaseComponent.assertThat(dashboardPage.dashboardBannerHeading).containsText("Alexander Smith")
        }
    }

    @Nested
    inner class LandlordRegistrationStepVerifyIdentity : NestedSinglePageTestWithSeedData("data-local.sql") {
        @Test
        fun `Navigating here as a registered landlord redirects to the landlord dashboard page`(page: Page) {
            navigator.navigateToLandlordRegistrationVerifyIdentityPage()
            val dashboardPage = assertPageIs(page, LandlordDashboardPage::class)
            BaseComponent.assertThat(dashboardPage.dashboardBannerHeading).containsText("Alexander Smith")
        }
    }

    @Nested
    inner class LandlordRegistrationStepName {
        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.skipToLandlordRegistrationNamePage()
            namePage.submitName("")
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LandlordRegistrationStepDateOfBirth {
        @ParameterizedTest
        @CsvSource(
            "'','','',Enter a date",
            "'',11,1990,You must include a day",
            "12,'',1990,You must include a month",
            "12,11,'',You must include a year",
            "'','',1990,You must include a day and a month",
            "12,'','',You must include a month and a year",
            "'',11,'',You must include a day and a year",
            "'',0,190,You must include a day",
            "0,'',190,You must include a month",
            "0,0,'',You must include a year",
            "'','',190,You must include a day and a month",
            "0,'','',You must include a month and a year",
        )
        fun `Submitting any empty fields returns an error`(
            day: String,
            month: String,
            year: String,
            expectedErrorMessage: String,
        ) {
            val dateOfBirthPage = navigator.skipToLandlordRegistrationDateOfBirthPage()
            dateOfBirthPage.submitDate(day, month, year)
            assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        @ParameterizedTest
        @CsvSource(
            "32,11,1990,Day must be a whole number between 1 and 31",
            "0,11,1990,Day must be a whole number between 1 and 31",
            "ABC,11,1990,Day must be a whole number between 1 and 31",
            "12,13,1990,Month must be a whole number between 1 and 12",
            "12,0,1990,Month must be a whole number between 1 and 12",
            "12,ABC,1990,Month must be a whole number between 1 and 12",
            "12,11,190,Year must be a whole number greater than 1899",
            "12,11,ABC,Year must be a whole number greater than 1899",
            "0,0,1990,Day must be a whole number between 1 and 31. Month must be a whole number between 1 and 12",
            "0,11,190,Day must be a whole number between 1 and 31. Year must be a whole number greater than 1899",
            "1,0,190,Month must be a whole number between 1 and 12. Year must be a whole number greater than 1899",
            "0,0,190,Day must be a whole number between 1 and 31. Month must be a whole number between 1 and 12. " +
                "Year must be a whole number greater than 1899",
        )
        fun `Submitting invalid day, month, or year returns an error`(
            day: String,
            month: String,
            year: String,
            expectedErrorMessage: String,
        ) {
            val dateOfBirthPage = navigator.skipToLandlordRegistrationDateOfBirthPage()
            dateOfBirthPage.submitDate(day, month, year)
            assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        @Nested
        inner class AgeValidation {
            private val dateTimeHelper = DateTimeHelper()

            private val currentDate = dateTimeHelper.getCurrentDateInUK()

            @Test
            fun `Submitting a valid date of birth for the minimum age redirects to the next page`(page: Page) {
                val date = currentDate.minus(DatePeriod(years = 18))
                val dateOfBirthPage = navigator.skipToLandlordRegistrationDateOfBirthPage()
                dateOfBirthPage.submitDate(date)
                assertPageIs(page, EmailFormPageLandlordRegistration::class)
            }

            @Test
            fun `Submitting a valid date of birth for the maximum age redirects to the next page`(page: Page) {
                val date = currentDate.minus(DatePeriod(years = 121)).plus(DatePeriod(days = 1))
                val dateOfBirthPage = navigator.skipToLandlordRegistrationDateOfBirthPage()
                dateOfBirthPage.submitDate(date)
                assertPageIs(page, EmailFormPageLandlordRegistration::class)
            }

            @Test
            fun `Submitting any invalid date for the minimum age returns an error`() {
                val date = currentDate.minus(DatePeriod(years = 18)).plus(DatePeriod(days = 1))
                val dateOfBirthPage = navigator.skipToLandlordRegistrationDateOfBirthPage()
                dateOfBirthPage.submitDate(date)
                assertThat(dateOfBirthPage.form.getErrorMessage()).containsText("The minimum age to register as a landlord is 18")
            }

            @Test
            fun `Submitting any invalid date for the maximum age returns an error`() {
                val date = currentDate.minus(DatePeriod(years = 121))
                val dateOfBirthPage = navigator.skipToLandlordRegistrationDateOfBirthPage()
                dateOfBirthPage.submitDate(date)
                assertThat(dateOfBirthPage.form.getErrorMessage()).containsText("You must enter a valid date of birth")
            }
        }
    }

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting an empty e-mail address returns an error`(page: Page) {
            val emailPage = navigator.skipToLandlordRegistrationEmailPage()
            emailPage.submitEmail("")
            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.skipToLandlordRegistrationEmailPage()
            emailPage.submitEmail("")
            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @ParameterizedTest
        @ValueSource(
            strings = ["GB", "US", "ES", "SN", "AU", "VG"],
        )
        fun `Submitting correct UK and international numbers with country codes redirects to the next step`(
            regionCode: String,
            page: Page,
        ) {
            val phoneNumPage = navigator.skipToLandlordRegistrationPhoneNumberPage()
            phoneNumPage.submitPhoneNumber(phoneNumberUtil.getFormattedInternationalPhoneNumber(regionCode))
            assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting an empty phone number returns an error`() {
            val phoneNumPage = navigator.skipToLandlordRegistrationPhoneNumberPage()
            phoneNumPage.submitPhoneNumber("")
            assertThat(phoneNumPage.form.getErrorMessage()).containsText("Enter a phone number")
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "notaphonenumber",
                "0",
                // International phone number without a country code
                "0355501234",
            ],
        )
        fun `Submitting an invalid phone number returns an error`(invalidPhoneNumber: String) {
            val phoneNumPage = navigator.skipToLandlordRegistrationPhoneNumberPage()
            phoneNumPage.submitPhoneNumber(invalidPhoneNumber)
            assertThat(phoneNumPage.form.getErrorMessage())
                .containsText("Enter a phone number including the country code for international numbers")
        }
    }

    @Nested
    inner class LandlordRegistrationStepCountryOfResidence {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val countryOfResidencePage = navigator.skipToLandlordRegistrationCountryOfResidencePage()
            countryOfResidencePage.form.submit()
            assertThat(countryOfResidencePage.form.getErrorMessage()).containsText("Select an option")
        }

        @Test
        fun `Submitting the no radio with no country selected returns an error`(page: Page) {
            val countryOfResidencePage = navigator.skipToLandlordRegistrationCountryOfResidencePage()
            countryOfResidencePage.form.selectNonUk()
            countryOfResidencePage.form.submit()
            assertThat(countryOfResidencePage.form.getErrorMessage())
                .containsText("Select the country or territory you are currently living in")
        }
    }

    @Nested
    inner class LandlordRegistrationStepLookupAddressAndNoAddressFound {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.skipToLandlordRegistrationLookupAddressPage()
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }

        @Test
        fun `If no addresses are returned, user can search again or enter address manually via the No Address Found step`(page: Page) {
            // Lookup address finds no results
            val houseNumber = "15"
            val postcode = "AB1 2CD"
            whenever(osPlacesClient.search(houseNumber, postcode, false)).thenReturn(MockOSPlacesAPIResponses.createResponseOfSize(0))
            val lookupAddressPage = navigator.skipToLandlordRegistrationLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // redirect to noAddressFoundPage
            val noAddressFoundPage = assertPageIs(page, NoAddressFoundFormPageLandlordRegistration::class)
            BaseComponent
                .assertThat(noAddressFoundPage.heading)
                .containsText("No matching address found for $postcode and $houseNumber")

            // Search Again
            noAddressFoundPage.searchAgain.clickAndWait()
            val lookupAddressPageAgain = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
            lookupAddressPageAgain.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // Submit no address found page
            val noAddressFoundPageAgain = assertPageIs(page, NoAddressFoundFormPageLandlordRegistration::class)
            noAddressFoundPageAgain.form.submit()
            assertPageIs(page, ManualAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepSelectAddress {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectAddressPage = navigator.skipToLandlordRegistrationSelectAddressPage()
            selectAddressPage.form.submit()
            assertThat(selectAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectAddressPage = navigator.skipToLandlordRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepManualAddress {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.skipToLandlordRegistrationManualAddressPage()
            manualAddressPage.form.submit()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class LandlordRegistrationStepNonEnglandOrWalesAddress {
        @Test
        fun `Submitting with no address returns an error`(page: Page) {
            val nonEnglandOrWalesAddressPage = navigator.skipToLandlordRegistrationNonEnglandOrWalesAddressPage()
            nonEnglandOrWalesAddressPage.form.submit()
            assertThat(nonEnglandOrWalesAddressPage.form.getErrorMessage()).containsText("You must include an address")
        }

        @Test
        fun `Submitting with a too long address returns an error`(page: Page) {
            val nonEnglandOrWalesAddressPage = navigator.skipToLandlordRegistrationNonEnglandOrWalesAddressPage()
            nonEnglandOrWalesAddressPage.submitAddress("too long address".repeat(1001))
            assertThat(nonEnglandOrWalesAddressPage.form.getErrorMessage().nth(0)).containsText("Address must be 1000 characters or fewer")
        }
    }

    @Nested
    inner class LandlordRegistrationStepLookupContactAddressAndNoAddressFound {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupContactAddressPage = navigator.skipToLandlordRegistrationLookupContactAddressPage()
            lookupContactAddressPage.form.submit()
            assertThat(lookupContactAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupContactAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }

        @Test
        fun `If no addresses are returned, user can search again or enter address manually via the No Address Found step`(page: Page) {
            // Lookup address finds no results
            val houseNumber = "15"
            val postcode = "AB1 2CD"
            whenever(osPlacesClient.search(houseNumber, postcode, false)).thenReturn(MockOSPlacesAPIResponses.createResponseOfSize(0))
            val lookupAddressPage = navigator.skipToLandlordRegistrationLookupContactAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // redirect to noAddressFoundPage
            val noAddressFoundPage =
                assertPageIs(page, NoContactAddressFoundFormPageLandlordRegistration::class)
            BaseComponent
                .assertThat(noAddressFoundPage.heading)
                .containsText("No matching address found for $postcode and $houseNumber")

            // Search Again
            noAddressFoundPage.searchAgain.clickAndWait()
            val lookupAddressPageAgain =
                assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
            lookupAddressPageAgain.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // Submit no address found page
            val noAddressFoundPageAgain =
                assertPageIs(page, NoContactAddressFoundFormPageLandlordRegistration::class)
            noAddressFoundPageAgain.form.submit()
            assertPageIs(page, ManualContactAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepSelectContactAddress {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectContactAddressPage = navigator.skipToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.form.submit()
            assertThat(selectContactAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectContactAddressPage = navigator.skipToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.searchAgain.clickAndWait()
            assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepManualContactAddress {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualContactAddressPage = navigator.skipToLandlordRegistrationManualContactAddressPage()
            manualContactAddressPage.form.submit()
            assertThat(manualContactAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualContactAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualContactAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class LandlordRegistrationStepDeclaration {
        @Test
        fun `Submitting without checking the checkbox returns an error`(page: Page) {
            val declarationPage = navigator.skipToLandlordRegistrationDeclarationPage()
            declarationPage.form.submit()
            assertThat(declarationPage.form.getErrorMessage()).containsText("You must agree to the declaration to continue")
        }
    }

    @Nested
    inner class LandlordRegistrationConfirmation {
        @Test
        fun `Navigating here with an incomplete form returns a 500 error page`(page: Page) {
            navigator.navigateToLandlordRegistrationConfirmationPage()
            val errorPage = assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }

    @Nested
    inner class LandlordRegistrationStepCheckAnswers {
        @Test
        fun `After changing an answer, submitting or going back returns to the CYA page`(page: Page) {
            var checkAnswersPage = navigator.skipToLandlordRegistrationCheckAnswersPage()
            checkAnswersPage.form.summaryList.emailRow.actions.actionLink
                .clickAndWait()
            var emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)

            emailPage.submitEmail("New@email.com")
            checkAnswersPage = assertPageIs(page, CheckAnswersPageLandlordRegistration::class)

            checkAnswersPage.form.summaryList.emailRow.actions.actionLink
                .clickAndWait()
            emailPage = assertPageIs(page, EmailFormPageLandlordRegistration::class)

            emailPage.backLink.clickAndWait()
            assertPageIs(page, CheckAnswersPageLandlordRegistration::class)
        }
    }
}
