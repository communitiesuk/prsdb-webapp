package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.DateOfBirthFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.InternationalAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.LookupContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.ManualContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SelectContactAddressFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.SummaryPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.models.formModels.VerifiedIdentityModel
import java.time.LocalDate

@Sql("/data-local.sql")
class LandlordRegistrationJourneyTests : IntegrationTest() {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @BeforeEach
    fun setup() {
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)
        whenever(
            osPlacesClient.search(any(), any()),
        ).thenReturn(
            "{'results':[{'DPA':{'ADDRESS':'1, Example Road, EG1 2AB'," +
                "'LOCAL_CUSTODIAN_CODE':100,'UPRN':'1','BUILDING_NUMBER':1,'POSTCODE':'EG1 2AB'}}]}",
        )
    }

    // TODO PRSD-622: Add the steps before and after the address section of the journey
    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        val countryOfResidencePage = navigator.goToLandlordRegistrationCountryOfResidencePage()
        countryOfResidencePage.radios.selectValue("true")
        countryOfResidencePage.form.submit()

        val lookupAddressPage = assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        lookupAddressPage.postcodeInput.fill("EG1 2AB")
        lookupAddressPage.houseNameOrNumberInput.fill("1")
        lookupAddressPage.form.submit()

        val selectAddressPage = assertPageIs(page, SelectAddressFormPageLandlordRegistration::class)
        selectAddressPage.radios.selectValue("1, Example Road, EG1 2AB")
        selectAddressPage.form.submit()

        assertPageIs(page, SummaryPageLandlordRegistration::class)
    }

    @Test
    fun `User can navigate the international landlord address journey if pages are correctly filled in`(page: Page) {
        val countryOfResidencePage = navigator.goToLandlordRegistrationCountryOfResidencePage()
        countryOfResidencePage.radios.selectValue("false")
        countryOfResidencePage.select.autocompleteInput.fill("Fr")
        countryOfResidencePage.select.selectValue("France")
        countryOfResidencePage.form.submit()

        val internationalAddressPage = assertPageIs(page, InternationalAddressFormPageLandlordRegistration::class)
        internationalAddressPage.textAreaInput.fill("international address")
        internationalAddressPage.form.submit()

        val lookupContactAddressPage = assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
        lookupContactAddressPage.postcodeInput.fill("EG1 2AB")
        lookupContactAddressPage.houseNameOrNumberInput.fill("1")
        lookupContactAddressPage.form.submit()

        val selectContactAddressPage = assertPageIs(page, SelectContactAddressFormPageLandlordRegistration::class)
        selectContactAddressPage.radios.selectValue("1, Example Road, EG1 2AB")
        selectContactAddressPage.form.submit()

        assertPageIs(page, SummaryPageLandlordRegistration::class)
    }

    @Nested
    inner class LandlordRegistrationStepConfirmIdentity {
        @Test
        fun `Submitting a valid name redirects to the next step`(page: Page) {
            // Arrange
            val verifiedIdentityMap =
                mutableMapOf<String, Any?>(
                    VerifiedIdentityModel.NAME_KEY to "name",
                    VerifiedIdentityModel.BIRTH_DATE_KEY to LocalDate.now(),
                )
            whenever(identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentityMap)

            // Act
            val confirmIdentityPage = navigator.goToLandlordRegistrationConfirmIdentityFormPage()
            confirmIdentityPage.form.submit()

            // Assert
            assertPageIs(page, EmailFormPageLandlordRegistration::class)
        }
    }

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

    @Nested
    inner class LandlordRegistrationStepCountryOfResidence {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val countryOfResidencePage = navigator.goToLandlordRegistrationCountryOfResidencePage()
            countryOfResidencePage.form.submit()
            assertThat(countryOfResidencePage.form.getErrorMessage()).containsText("Select an option")
        }

        @Test
        fun `Submitting the no radio with no country selected returns an error`(page: Page) {
            val countryOfResidencePage = navigator.goToLandlordRegistrationCountryOfResidencePage()
            countryOfResidencePage.radios.selectValue("false")
            countryOfResidencePage.form.submit()
            assertThat(countryOfResidencePage.form.getErrorMessage())
                .containsText("Select the country or territory you are currently living in")
        }
    }

    @Nested
    inner class LandlordRegistrationStepLookupAddress {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.goToLandlordRegistrationLookupAddressPage()
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }
    }

    @Nested
    inner class LandlordRegistrationStepSelectAddress {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectAddressPage = navigator.goToLandlordRegistrationSelectAddressPage()
            selectAddressPage.form.submit()
            assertThat(selectAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectAddressPage = navigator.goToLandlordRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.click()
            assertPageIs(page, LookupAddressFormPageLandlordRegistration::class)
        }

        @Test
        fun `Selecting the manual option navigates to the ManualAddress step`(page: Page) {
            val selectAddressPage = navigator.goToLandlordRegistrationSelectAddressPage()
            selectAddressPage.radios.selectValue(MANUAL_ADDRESS_CHOSEN)
            selectAddressPage.form.submit()
            assertPageIs(page, ManualAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepManualAddress {
        @Test
        fun `Submitting valid data redirects to the next step`(page: Page) {
            val manualAddressPage = navigator.goToLandlordRegistrationManualAddressPage()
            manualAddressPage.addressLineOneInput.fill("address line one")
            manualAddressPage.addressLineTwoInput.fill("address line two")
            manualAddressPage.townOrCityInput.fill("town")
            manualAddressPage.countyInput.fill("county")
            manualAddressPage.postcodeInput.fill("EG1 2AB")
            manualAddressPage.form.submit()
            assertPageIs(page, SummaryPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.goToLandlordRegistrationManualAddressPage()
            manualAddressPage.form.submit()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class LandlordRegistrationStepInternationalAddress {
        @Test
        fun `Submitting with no address returns an error`(page: Page) {
            val internationalAddressPage = navigator.goToLandlordRegistrationInternationalAddressPage()
            internationalAddressPage.form.submit()
            assertThat(internationalAddressPage.form.getErrorMessage()).containsText("You must include an address")
        }

        @Test
        fun `Submitting with a too long address returns an error`(page: Page) {
            val internationalAddressPage = navigator.goToLandlordRegistrationInternationalAddressPage()
            internationalAddressPage.textAreaInput.fill("too long address".repeat(1001))
            internationalAddressPage.form.submit()
            assertThat(internationalAddressPage.form.getErrorMessage()).containsText("Address must be 1000 characters or fewer")
        }
    }

    @Nested
    inner class LandlordRegistrationStepLookupContactAddress {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupContactAddressPage = navigator.goToLandlordRegistrationLookupContactAddressPage()
            lookupContactAddressPage.form.submit()
            assertThat(lookupContactAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupContactAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }
    }

    @Nested
    inner class LandlordRegistrationStepSelectContactAddress {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectContactAddressPage = navigator.goToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.form.submit()
            assertThat(selectContactAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectContactAddressPage = navigator.goToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.searchAgain.click()
            assertPageIs(page, LookupContactAddressFormPageLandlordRegistration::class)
        }

        @Test
        fun `Selecting the manual option navigates to the ManualContactAddress step`(page: Page) {
            val selectContactAddressPage = navigator.goToLandlordRegistrationSelectContactAddressPage()
            selectContactAddressPage.radios.selectValue(MANUAL_ADDRESS_CHOSEN)
            selectContactAddressPage.form.submit()
            assertPageIs(page, ManualContactAddressFormPageLandlordRegistration::class)
        }
    }

    @Nested
    inner class LandlordRegistrationStepManualContactAddress {
        @Test
        fun `Submitting valid data redirects to the next step`(page: Page) {
            val manualContactAddressPage = navigator.goToLandlordRegistrationManualContactAddressPage()
            manualContactAddressPage.addressLineOneInput.fill("address line one")
            manualContactAddressPage.addressLineTwoInput.fill("address line two")
            manualContactAddressPage.townOrCityInput.fill("town")
            manualContactAddressPage.countyInput.fill("county")
            manualContactAddressPage.postcodeInput.fill("EG1 2AB")
            manualContactAddressPage.form.submit()
            assertPageIs(page, SummaryPageLandlordRegistration::class)
        }

        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualContactAddressPage = navigator.goToLandlordRegistrationManualContactAddressPage()
            manualContactAddressPage.form.submit()
            assertThat(manualContactAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualContactAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualContactAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }
}
