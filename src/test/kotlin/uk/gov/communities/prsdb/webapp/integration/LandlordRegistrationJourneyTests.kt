package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Response
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class LandlordRegistrationJourneyTests : IntegrationTest() {
    final val journeyUrl = "register-as-a-landlord"
    val initialStepUrl = "email"
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Submitting a valid email address redirects to the next step`() {
            val formPage = navigator.goToLandlordRegistrationEmailFormPage()
            formPage.fillEmail("test@example.com")
            formPage.submit()
        }

        @Test
        fun `Submitting an empty e-mail address returns an error`() {
            val formPage = navigator.goToLandlordRegistrationEmailFormPage()
            formPage.fillEmail("")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter a valid email address to continue. An email is required for contact purposes.")
        }

        @Test
        fun `Submitting an invalid e-mail address returns an error`() {
            val formPage = navigator.goToLandlordRegistrationEmailFormPage()
            formPage.fillEmail("notAnEmail")
            formPage.submitUnsuccessfully()
            formPage.assertEmailFormErrorContains("Enter an email address in the right format")
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @Test
        fun `Redirects to the first step in the journey if session data is not valid for step`() {
            val formResponse: Response? = navigator.navigate("$journeyUrl/phone-number")
            assertThat(formResponse?.url()).contains("$journeyUrl/$initialStepUrl")
        }

        @Nested
        inner class LandlordRegistrationStepPhoneNumberWithPreviousStepsFulfilled {
            @BeforeEach
            fun setUp() {
                val formPage = navigator.goToLandlordRegistrationEmailFormPage()
                formPage.fillEmail("test@example.com")
                formPage.submitWithoutLoadingPage()
            }

            @Test
            fun `Does not redirect away from step if session data is valid for step`() {
                val formResponse: Response? = navigator.navigate("$journeyUrl/phone-number")
                assertThat(formResponse?.url()).contains("$journeyUrl/phone-number")
            }

            @Test
            fun `Submitting correct UK numbers without a country code redirects to the next step`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                val number = phoneNumberUtil.getExampleNumber("GB")
                formPage.fillPhoneNumber("${number.countryCode}${number.nationalNumber}")
                formPage.submit()
            }

            @ParameterizedTest
            @ValueSource(
                strings = ["GB", "US", "ES", "SN", "AU", "VG"],
            )
            fun `Submitting correct UK and international numbers with country codes redirects to the next step`(regionCode: String) {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                val number = phoneNumberUtil.getExampleNumber(regionCode)
                formPage.fillPhoneNumber("+${number.countryCode}${number.nationalNumber}")
                formPage.submit()
            }

            @Test
            fun `Submitting an empty phone number returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillPhoneNumber("")
                formPage.submitUnsuccessfully()
                formPage.assertPhoneNumberFormErrorContains("Enter a phone number")
            }

            @Test
            fun `Submitting an invalid phone number returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillPhoneNumber("notAPhoneNumber")
                formPage.submitUnsuccessfully()
                formPage.assertPhoneNumberFormErrorContains("Enter a phone number including the country code for international numbers")
            }

            @Test
            fun `Submitting incorrect number returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillPhoneNumber("0")
                formPage.submitUnsuccessfully()
                formPage.assertPhoneNumberFormErrorContains("Enter a phone number including the country code for international numbers")
            }

            @Test
            fun `Submitting an international phone number without a country code returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillPhoneNumber("0355501234")
                formPage.submitUnsuccessfully()
                formPage.assertPhoneNumberFormErrorContains("Enter a phone number including the country code for international numbers")
            }
        }
    }
}
