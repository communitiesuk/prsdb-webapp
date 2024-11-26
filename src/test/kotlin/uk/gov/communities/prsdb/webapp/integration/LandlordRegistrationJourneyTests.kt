package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.CountryOfResidenceFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration

@Sql("/data-local.sql")
class LandlordRegistrationJourneyTests : IntegrationTest() {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @BeforeEach
    fun setup() {
        whenever(identityService.getVerifiedIdentityData(any())).thenReturn(null)
    }

    @Nested
    inner class LandlordRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`(page: Page) {
            val namePage = navigator.goToLandlordRegistrationNameFormPage()
            namePage.nameInput.fill("name")
            namePage.form.submit()
            assertPageIs(page, EmailFormPageLandlordRegistration::class)
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
