package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration

@Sql("/data-local.sql")
class LandlordRegistrationJourneyTests : IntegrationTest() {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @Nested
    inner class LandlordRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`(page: Page) {
            val namePage = navigator.goToLandlordRegistrationNameFormPage()
            namePage.nameInput.fill("name")
            namePage.form.submit()
            assertPageIs(page, DateOfBirthFormPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.goToLandlordRegistrationNameFormPage()
            namePage.nameInput.fill("")
            namePage.form.submit()
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LandlordRegistrationStepDateOfBirth {
        @ParameterizedTest
        @CsvSource(
            "12,11,1990",
            "29,02,2004",
        )
        fun `Submitting a valid date of birth redirects to the next step`(
            day: String,
            month: String,
            year: String,
            page: Page,
        ) {
            val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
            dateOfBirthPage.dayInput.fill(day)
            dateOfBirthPage.monthInput.fill(month)
            dateOfBirthPage.yearInput.fill(year)
            dateOfBirthPage.form.submit()
            assertPageIs(page, EmailFormPageLandlordRegistration::class)
        }

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
            val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
            dateOfBirthPage.dayInput.fill(day)
            dateOfBirthPage.monthInput.fill(month)
            dateOfBirthPage.yearInput.fill(year)
            dateOfBirthPage.form.submit()
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
            val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
            dateOfBirthPage.dayInput.fill(day)
            dateOfBirthPage.monthInput.fill(month)
            dateOfBirthPage.yearInput.fill(year)
            dateOfBirthPage.form.submit()
            assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }

        @Nested
        inner class AgeValidation {
            val dateTimeHelper = DateTimeHelper()

            val currentDate = dateTimeHelper.getNowAsLocalDate()

            @Test
            fun `Submitting a valid date of birth for the minimum age redirects to the next page`(page: Page) {
                val date = currentDate.minus(DatePeriod(years = 18))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.dayInput.fill(date.dayOfMonth.toString())
                dateOfBirthPage.monthInput.fill((date.month.number).toString())
                dateOfBirthPage.yearInput.fill(date.year.toString())
                dateOfBirthPage.form.submit()
                println(page.content())
                assertPageIs(page, EmailFormPageLandlordRegistration::class)
            }

            @Test
            fun `Submitting a valid date of birth for the maximum age redirects to the next page`(page: Page) {
                val date = currentDate.minus(DatePeriod(years = 121)).plus(DatePeriod(days = 1))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.dayInput.fill(date.dayOfMonth.toString())
                dateOfBirthPage.monthInput.fill((date.month.number).toString())
                dateOfBirthPage.yearInput.fill(date.year.toString())
                dateOfBirthPage.form.submit()
                println(page.content())
                assertPageIs(page, EmailFormPageLandlordRegistration::class)
            }

            @Test
            fun `Submitting any invalid date for the minimum age returns an error`() {
                val date = currentDate.minus(DatePeriod(years = 18)).plus(DatePeriod(days = 1))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.dayInput.fill(date.dayOfMonth.toString())
                dateOfBirthPage.monthInput.fill((date.month.number).toString())
                dateOfBirthPage.yearInput.fill(date.year.toString())
                dateOfBirthPage.form.submit()
                assertThat(dateOfBirthPage.form.getErrorMessage()).containsText(
                    "The minimum age to register as a landlord is 18",
                )
            }

            @Test
            fun `Submitting any invalid date for the maximum age returns an error`() {
                val date = currentDate.minus(DatePeriod(years = 121))
                val dateOfBirthPage = navigator.goToLandlordRegistrationDateOfBirthFormPage()
                dateOfBirthPage.dayInput.fill(date.dayOfMonth.toString())
                dateOfBirthPage.monthInput.fill((date.month.number).toString())
                dateOfBirthPage.yearInput.fill(date.year.toString())
                dateOfBirthPage.form.submit()
                assertThat(dateOfBirthPage.form.getErrorMessage()).containsText("You must enter a valid date of birth")
            }
        }
    }

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting a valid email address redirects to the next step`(page: Page) {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.emailInput.fill("test@example.com")
            emailPage.form.submit()
            assertPageIs(page, PhoneNumberFormPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.form.submit()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.form.submit()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @Test
        fun `Submitting correct UK numbers without a country code redirects to the next step`(page: Page) {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            val number = phoneNumberUtil.getExampleNumber("GB")
            phoneNumPage.phoneNumberInput.fill("${number.countryCode}${number.nationalNumber}")
            phoneNumPage.form.submit()
            assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        }

        @ParameterizedTest
        @ValueSource(
            strings = ["GB", "US", "ES", "SN", "AU", "VG"],
        )
        fun `Submitting correct UK and international numbers with country codes redirects to the next step`(
            regionCode: String,
            page: Page,
        ) {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            val number = phoneNumberUtil.getExampleNumber(regionCode)
            phoneNumPage.phoneNumberInput.fill("+${number.countryCode}${number.nationalNumber}")
            phoneNumPage.form.submit()
            assertPageIs(page, CountryOfResidenceFormPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting an empty phone number returns an error`() {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            phoneNumPage.phoneNumberInput.fill("")
            phoneNumPage.form.submit()
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
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            phoneNumPage.phoneNumberInput.fill(invalidPhoneNumber)
            phoneNumPage.form.submit()
            assertThat(
                phoneNumPage.form.getErrorMessage(),
            ).containsText("Enter a phone number including the country code for international numbers")
        }
    }
}
