package uk.gov.communities.prsdb.webapp.integration

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.microsoft.playwright.Response
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.assertIsPage
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages.EmailFormPageLandlordRegistration
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.landlordRegistrationJourneyPages.PhoneNumberFormPageLandlordRegistration

@Sql("/data-local.sql")
class LandlordRegistrationJourneyTests : IntegrationTest() {
    final val journeyUrlSegment = REGISTER_LANDLORD_JOURNEY_URL
    final val nameUrlSegment = LandlordRegistrationStepId.Name.urlPathSegment
    final val emailUrlSegment = LandlordRegistrationStepId.Email.urlPathSegment
    final val phoneNumberUrlSegment = LandlordRegistrationStepId.PhoneNumber.urlPathSegment
    final val initialStepUrl = nameUrlSegment
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    @Nested
    inner class LandlordRegistrationStepName {
        @Test
        fun `Submitting a valid name redirects to the next step`() {
            val formPage = navigator.goToLandlordRegistrationNameFormPage()
            formPage.fillInput("Arthur Dent")
            val nextPage = formPage.submit()
            val emailPage = assertIsPage(nextPage, EmailFormPageLandlordRegistration::class)
            assertThat(emailPage.fieldSetHeading).containsText("What is your email address?")
        }

        @Test
        fun `Submitting an empty name returns an error`() {
            val formPage = navigator.goToLandlordRegistrationNameFormPage()
            formPage.fillInput("")
            formPage.submitUnsuccessfully()
            assertThat(
                formPage.inputFormErrorMessage,
            ).containsText("You must enter your full name")
        }
    }

    @Nested
    inner class LandlordRegistrationStepEmail {
        @Test
        fun `Redirects to the first step in the journey if session data is not valid for step`() {
            val formResponse: Response? = navigator.navigate("$journeyUrlSegment/$emailUrlSegment")
            assertThat(formResponse?.url()).contains("/$journeyUrlSegment/$initialStepUrl")
        }

        @Nested
        inner class LandlordRegistrationStepEmailWithPreviousStepsFulfilled {
            @BeforeEach
            fun setUp() {
                val formPage = navigator.goToLandlordRegistrationNameFormPage()
                formPage.fillInput("Arthur Dent")
                formPage.submit()
            }

            @Test
            fun `Does not redirect away from step if session data is valid for step`() {
                val formResponse: Response? = navigator.navigate("/$journeyUrlSegment/$emailUrlSegment")
                assertThat(formResponse?.url()).contains("/$journeyUrlSegment/$emailUrlSegment")
            }

            @Test
            fun `Submitting a valid email address redirects to the next step`() {
                val formPage = navigator.goToLandlordRegistrationEmailFormPage()
                formPage.fillInput("test@example.com")
                val nextPage = formPage.submit()
                val phoneNumberPage =
                    assertIsPage(nextPage, PhoneNumberFormPageLandlordRegistration::class)
                assertThat(phoneNumberPage.fieldSetHeading).containsText("What is your phone number?")
            }

            @Test
            fun `Submitting an empty e-mail address returns an error`() {
                val formPage = navigator.goToLandlordRegistrationEmailFormPage()
                formPage.fillInput("")
                formPage.submitUnsuccessfully()
                assertThat(
                    formPage.inputFormErrorMessage,
                ).containsText("Enter a valid email address to continue. An email is required for contact purposes.")
            }

            @Test
            fun `Submitting an invalid e-mail address returns an error`() {
                val formPage = navigator.goToLandlordRegistrationEmailFormPage()
                formPage.fillInput("notAnEmail")
                formPage.submitUnsuccessfully()
                assertThat(
                    formPage.inputFormErrorMessage,
                ).containsText("Enter an email address in the right format")
            }
        }
    }

    @Nested
    inner class LandlordRegistrationStepPhoneNumber {
        @Test
        fun `Redirects to the first step in the journey if session data is not valid for step`() {
            val formResponse: Response? = navigator.navigate("$journeyUrlSegment/$phoneNumberUrlSegment")
            assertThat(formResponse?.url()).contains("/$journeyUrlSegment/$initialStepUrl")
        }

        @Nested
        inner class LandlordRegistrationStepPhoneNumberWithPreviousStepsFulfilled {
            @BeforeEach
            fun setUp() {
                val formPage = navigator.goToLandlordRegistrationNameFormPage()
                formPage.fillInput("Arthur Dent")
                val nextStep = BasePage.createValid(formPage.submit(), EmailFormPageLandlordRegistration::class)
                nextStep.fillInput("test@example.com")
                nextStep.submit()
            }

            @Test
            fun `Does not redirect away from step if session data is valid for step`() {
                val formResponse: Response? = navigator.navigate("/$journeyUrlSegment/$phoneNumberUrlSegment")
                assertThat(formResponse?.url()).contains("/$journeyUrlSegment/$phoneNumberUrlSegment")
            }

            @Test
            fun `Submitting correct UK numbers without a country code redirects to the next step`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                val number = phoneNumberUtil.getExampleNumber("GB")
                formPage.fillInput("${number.countryCode}${number.nationalNumber}")
                val nextPage = formPage.submit()
                // TODO: Assert next page is CountryOfResidenceFormPageLandlordRegistration
//                val emailPage = assertIsPage(nextPage, EmailFormPageLandlordRegistration::class)
//                assertThat(emailPage.fieldSetHeading).containsText("What is your email address?")
            }

            @ParameterizedTest
            @ValueSource(
                strings = ["GB", "US", "ES", "SN", "AU", "VG"],
            )
            fun `Submitting correct UK and international numbers with country codes redirects to the next step`(regionCode: String) {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                val number = phoneNumberUtil.getExampleNumber(regionCode)
                formPage.fillInput("+${number.countryCode}${number.nationalNumber}")
                val nextPage = formPage.submit()
                // TODO: Assert next page is CountryOfResidenceFormPageLandlordRegistration
//                val emailPage = assertIsPage(nextPage, EmailFormPageLandlordRegistration::class)
//                assertThat(emailPage.fieldSetHeading).containsText("What is your email address?")
            }

            @Test
            fun `Submitting an empty phone number returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillInput("")
                formPage.submitUnsuccessfully()
                assertThat(
                    formPage.inputFormErrorMessage,
                ).containsText("Enter a phone number")
            }

            @Test
            fun `Submitting an invalid phone number returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillInput("notAPhoneNumber")
                formPage.submitUnsuccessfully()
                assertThat(
                    formPage.inputFormErrorMessage,
                ).containsText("Enter a phone number including the country code for international numbers")
            }

            @Test
            fun `Submitting incorrect number returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillInput("0")
                formPage.submitUnsuccessfully()
                assertThat(
                    formPage.inputFormErrorMessage,
                ).containsText("Enter a phone number including the country code for international numbers")
            }

            @Test
            fun `Submitting an international phone number without a country code returns an error`() {
                val formPage = navigator.goToLandlordRegistrationPhoneNumberFormPage()
                formPage.fillInput("0355501234")
                formPage.submitUnsuccessfully()
                assertThat(
                    formPage.inputFormErrorMessage,
                ).containsText("Enter a phone number including the country code for international numbers")
            }
        }
    }
}
