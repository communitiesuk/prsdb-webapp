package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class LandlordRegistrationJourneyTests : IntegrationTest() {
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @Nested
    inner class LandlordRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val namePage = navigator.goToLandlordRegistrationNameFormPage()
            namePage.nameInput.fill("name")
            namePage.submitFormAndAssertNextPage()
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val namePage = navigator.goToLandlordRegistrationNameFormPage()
            namePage.nameInput.fill("")
            namePage.submitForm()
            assertThat(namePage.form.getErrorMessage()).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting a valid email address redirects to the next step`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.emailInput.fill("test@example.com")
            emailPage.submitFormAndAssertNextPage()
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.submitForm()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val emailPage = navigator.goToLandlordRegistrationEmailFormPage()
            emailPage.emailInput.fill("")
            emailPage.submitForm()
            assertThat(
                emailPage.form.getErrorMessage(),
            ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @Test
        fun `Submitting correct UK numbers without a country code redirects to the next step`() {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            val number = phoneNumberUtil.getExampleNumber("GB")
            phoneNumPage.phoneNumberInput.fill("${number.countryCode}${number.nationalNumber}")
            phoneNumPage.submitFormAndAssertNextPage()
        }

        @ParameterizedTest
        @ValueSource(
            strings = ["GB", "US", "ES", "SN", "AU", "VG"],
        )
        fun `Submitting correct UK and international numbers with country codes redirects to the next step`(regionCode: String) {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            val number = phoneNumberUtil.getExampleNumber(regionCode)
            phoneNumPage.phoneNumberInput.fill("+${number.countryCode}${number.nationalNumber}")
            phoneNumPage.submitFormAndAssertNextPage()
        }

        @Test
        fun `Submitting an empty phone number returns an error`() {
            val phoneNumPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
            phoneNumPage.phoneNumberInput.fill("")
            phoneNumPage.submitForm()
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
            phoneNumPage.submitForm()
            assertThat(
                phoneNumPage.form.getErrorMessage(),
            ).containsText("Enter a phone number including the country code for international numbers")
        }
    }
}
